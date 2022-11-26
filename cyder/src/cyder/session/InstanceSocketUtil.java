package cyder.session;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.network.NetworkUtil;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utilities related to the singular instance socket and API calls which use the instance socket.
 */
public final class InstanceSocketUtil {
    /**
     * The key for obtaining whether localhost shutdown requests
     * should be completed without a valid password from the props.
     */
    private static final String AUTO_COMPLY_TO_LOCALHOST_SHUTDOWN_REQUESTS =
            "auto_comply_to_localhost_shutdown_requests";

    /**
     * The key for obtaining the localhost shutdown request password from the props.
     */
    private static final String LOCALHOST_SHUTDOWN_REQUEST_PASSWORD = "localhost_shutdown_request_password";

    /**
     * The default instance socket port.
     */
    private static final int DEFAULT_INSTANCE_SOCKET_PORT = 8888;

    /**
     * The key to get the instance socket port from the props.
     */
    private static final String INSTANCE_SOCKET_PORT = "instance_socket_port";

    /**
     * The port to start the instance socket on.
     */
    private static final int instanceSocketPort;

    static {
        boolean propPresent = PropLoader.propExists(INSTANCE_SOCKET_PORT);

        if (propPresent) {
            int requestedPort = PropLoader.getInteger(INSTANCE_SOCKET_PORT);

            if (NetworkUtil.localPortAvailable(requestedPort)) {
                instanceSocketPort = requestedPort;
            } else {
                instanceSocketPort = DEFAULT_INSTANCE_SOCKET_PORT;
            }
        } else {
            instanceSocketPort = DEFAULT_INSTANCE_SOCKET_PORT;
        }
    }

    /**
     * The number of clients which can be waiting for the instance socket to free up and connect.
     */
    private static final int instanceSocketBacklog = 1;

    /**
     * Whether the instance socket bind was attempted.
     */
    private static final AtomicBoolean instanceSocketBindAttempted = new AtomicBoolean(false);

    /**
     * The instance socket.
     */
    private static ServerSocket instanceSocket;

    /**
     * Suppress default constructor.
     */
    private InstanceSocketUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the instance socket port this instance is using.
     *
     * @return the instance socket port this instance is using
     */
    public static int getInstanceSocketPort() {
        return instanceSocketPort;
    }

    /**
     * Returns whether the instance socket port is currently available.
     *
     * @return whether the instance socket port is currently available
     */
    public static boolean instanceSocketPortAvailable() {
        return NetworkUtil.localPortAvailable(instanceSocketPort);
    }

    /**
     * Binds the instance socket to the instance socket port and starts listening for a connection.
     *
     * @throws cyder.exceptions.FatalException if an exception occurs
     */
    public static void startListening() {
        Preconditions.checkState(instanceSocketPortAvailable());
        Preconditions.checkState(!instanceSocketBindAttempted.get());

        instanceSocketBindAttempted.set(true);

        CyderThreadRunner.submit(() -> {
            try {
                instanceSocket = new ServerSocket(instanceSocketPort, instanceSocketBacklog);

                while (true) {
                    try {
                        Socket client = instanceSocket.accept();

                        PrintWriter responseWriter = new PrintWriter(client.getOutputStream(), true);
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                        CyderCommunicationMessage receivedMessage = readInputMessage(inputReader);

                        onInstanceSocketMessageReceived(receivedMessage, responseWriter);
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }
            } catch (Exception e) {
                throw new FatalException(e);
            }
        }, IgnoreThread.InstanceSocket.getName());
    }

    /**
     * Reads a Cyder communication message from the provided buffered reader.
     *
     * @param inputReader the buffered reader
     * @return the communication message
     */
    private static CyderCommunicationMessage readInputMessage(BufferedReader inputReader) {
        Preconditions.checkNotNull(inputReader);

        try {
            String startAndEndHash = inputReader.readLine();
            StringBuilder inputBuilder = new StringBuilder();

            String line;
            while ((line = inputReader.readLine()) != null && !line.equals(startAndEndHash)) {
                inputBuilder.append(line);
            }

            return CyderCommunicationMessage.fromJson(inputBuilder.toString());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new FatalException("Failed to read communication message from reader: " + inputReader);
    }

    /**
     * The actions to invoke when a message is received in the instance socket.
     *
     * @param message        the message received
     * @param responseWriter the writer to use to send a response message
     */
    private static void onInstanceSocketMessageReceived(CyderCommunicationMessage message,
                                                        PrintWriter responseWriter) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(responseWriter);

        String messageType = message.getMessage();

        // todo will be instanceof ideally if we can figure it out, from working base
        if (messageType.equals(CyderRemoteShutdownMessage.MESSAGE)) {
            onInstanceSocketCyderRemoteShutdownMessageReceived(message, responseWriter);
        } else {
            throw new FatalException("Unknown CyderCommunicationMessage: " + messageType);
        }
    }

    /**
     * Results after determining whether a remote shutdown request should be denied or complied to.
     */
    private enum RemoteShutdownRequestResult {
        /**
         * The password was not found.
         */
        PASSWORD_NOT_FOUND(false),

        /**
         * The password was incorrect.
         */
        PASSWORD_INCORRECT(false),

        /**
         * The password was correct.
         */
        PASSWORD_CORRECT(true),

        /**
         * The auto compliance prop for remote shutdown requests is enabled.
         */
        AUTO_COMPLIANCE_ENABLED(true);

        /**
         * Whether this result indicates compliance.
         */
        private final boolean shouldComply;

        RemoteShutdownRequestResult(boolean shouldComply) {
            this.shouldComply = shouldComply;
        }

        /**
         * Returns whether this result is indicative of a compliance.
         *
         * @return whether this result is indicative of a compliance
         */
        public boolean isShouldComply() {
            return shouldComply;
        }
    }

    /**
     * Returns whether the shutdown request should be denied or complied to and the reasoning for the decision.
     *
     * @param receivedHash the hash received from the shutdown request message
     * @return whether the shutdown request should be denied or complied to and the reasoning
     */
    private static RemoteShutdownRequestResult determineRemoteShutdownRequestResult(String receivedHash) {
        if (PropLoader.getBoolean(AUTO_COMPLY_TO_LOCALHOST_SHUTDOWN_REQUESTS)) {
            return RemoteShutdownRequestResult.AUTO_COMPLIANCE_ENABLED;
        } else {
            boolean passwordExists = PropLoader.propExists(LOCALHOST_SHUTDOWN_REQUEST_PASSWORD);

            if (passwordExists) {
                String remoteShutdownPassword = PropLoader.getString(LOCALHOST_SHUTDOWN_REQUEST_PASSWORD);
                String hashedShutdownRequestPassword = SecurityUtil.toHexString(
                        SecurityUtil.getSha256(remoteShutdownPassword.toCharArray()));

                return receivedHash.equals(hashedShutdownRequestPassword)
                        ? RemoteShutdownRequestResult.PASSWORD_CORRECT
                        : RemoteShutdownRequestResult.PASSWORD_INCORRECT;
            } else {
                return RemoteShutdownRequestResult.PASSWORD_NOT_FOUND;
            }
        }
    }

    /**
     * The actions to invoke when a Cyder remote shutdown message is received.
     *
     * @param message        the message received
     * @param responseWriter the print writer to send a response to the sending client
     */
    private static void onInstanceSocketCyderRemoteShutdownMessageReceived(CyderCommunicationMessage message,
                                                                           PrintWriter responseWriter) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(responseWriter);

        Logger.log(LogTag.DEBUG, "Shutdown requested from instance: " + message.getSessionId());

        RemoteShutdownRequestResult result = determineRemoteShutdownRequestResult(message.getContent());

        String logRepresentation = switch (result) {
            case PASSWORD_NOT_FOUND -> "Shutdown request denied, password not found";
            case PASSWORD_INCORRECT -> "Shutdown request denied, password incorrect";
            case PASSWORD_CORRECT -> "Shutdown request accepted, password correct";
            case AUTO_COMPLIANCE_ENABLED -> "Shutdown request accepted, auto comply is enabled";
        };
        Logger.log(LogTag.DEBUG, logRepresentation);

        if (result.isShouldComply()) {
            try {
                instanceSocket.close();
                sendCommunicationMessage("Remote shutdown response", "Shutting down", responseWriter);
                responseWriter.close();
            } catch (Exception ignored) {} finally {
                OsUtil.exit(ExitCondition.RemoteShutdown);
            }
        }
    }

    /**
     * Sends a Cyder communication message using the provided print writer.
     *
     * @param message        the response message string for the {@link CyderCommunicationMessage}s content field.
     * @param responseWriter the print writer to use to send the response
     */
    private static void sendCommunicationMessage(String message, String content, PrintWriter responseWriter) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(content);
        Preconditions.checkNotNull(responseWriter);
        Preconditions.checkArgument(!message.isEmpty());
        Preconditions.checkArgument(!content.isEmpty());

        CyderCommunicationMessage responseShutdownMessage = new CyderCommunicationMessage(
                message, content, SessionManager.getSessionId());

        String sendHash = SecurityUtil.generateUuid();
        responseWriter.println(sendHash);
        responseWriter.println(responseShutdownMessage);
        responseWriter.println(sendHash);
    }

    public static void main(String[] args) throws Exception {
        int port = 8888;
        Future<CyderCommunicationMessage> futureResponse =
                sendRemoteShutdownRequest(NetworkUtil.LOCALHOST, port, "Vexento");
        while (!futureResponse.isDone()) Thread.onSpinWait();
        CyderCommunicationMessage response = futureResponse.get();
        System.out.println(response);

        while (!NetworkUtil.localPortAvailable(port)) Thread.onSpinWait();
        System.out.println("Port " + port + " is free continue new session normally");

        System.exit(0);
    }

    /**
     * Sends a remote shutdown request to the Cyder instance using the provided host and port.
     *
     * @param host             the host of the remote Cyder instance
     * @param port             the port of the remote Cyder instance
     * @param shutdownPassword the password to prove this instance has the
     *                         authority to request the remote instance to perform a shutdown
     * @return the response message from the remote instance
     */
    public static Future<CyderCommunicationMessage> sendRemoteShutdownRequest(String host, int port,
                                                                              String shutdownPassword) {
        Preconditions.checkNotNull(host);
        Preconditions.checkArgument(!host.isEmpty());
        Preconditions.checkArgument(NetworkUtil.portRange.contains(port));
        Preconditions.checkNotNull(shutdownPassword);
        Preconditions.checkArgument(!shutdownPassword.isEmpty());

        String executorName = "Remote Shutdown Request, host: " + host + ", port: " + port;
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(executorName)).submit(() -> {
            try {
                Socket clientSocket = new Socket(host, port);
                PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // todo this is common throughout, make method in SecurityUtil
                String hashedShutdownPassword = SecurityUtil.toHexString(
                        SecurityUtil.getSha256(shutdownPassword.toCharArray()));
                sendCommunicationMessage(CyderRemoteShutdownMessage.MESSAGE, hashedShutdownPassword, outputWriter);

                return readInputMessage(inputReader);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            throw new FatalException("Failed to send remote shutdown request");
        });
    }
}