package cyder.network;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility methods revolving around networking, urls, servers, etc.
 */
@SuppressWarnings("unused") /* Response codes */
public final class NetworkUtil {
    /**
     * The local host string.
     */
    public static final String LOCALHOST = "localhost";

    /**
     * The range a general computer port must fall into.
     */
    public static final Range<Integer> portRange = Range.closed(1024, 65535);

    /**
     * The string used to represent a space in a URL.
     */
    public static final String URL_SPACE = "%20";

    /**
     * The time in ms to wait for a local port to bind to determine whether it is available.
     */
    private static final Duration LOCAL_PORT_AVAILABLE_TIMEOUT = Duration.ofMillis(400);

    /**
     * Suppress default constructor.
     */
    private NetworkUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Opens the provided url using the native browser.
     *
     * @param url the url to open
     * @return a pointer to the {@link Process} instance that opened the url if successful. Empty optional else
     * @throws NullPointerException     if the provided url is null
     * @throws IllegalArgumentException if the provided url is empty or invalid
     * @throws IOException              if an exception occurs after invocation of {@link ProcessBuilder#start()}
     */
    @CheckReturnValue
    @CanIgnoreReturnValue
    public static Optional<Process> openUrl(String url) throws IOException {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());
        Preconditions.checkArgument(isValidUrl(url));

        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "start", url);
        Process process = builder.start();
        return Optional.of(process);
    }

    /**
     * The timeout value when determining if a site is reachable.
     */
    public static final int SITE_PING_TIMEOUT = (int) (TimeUtil.millisInSecond * 5);

    /**
     * The slash-slash for urls.
     */
    private static final String slashSlash = "//";

    /**
     * So no head?
     */
    private static final String HEAD = "HEAD";

    /**
     * The minimum HTTP response code indicating a successful response.
     */
    public static final int MIN_SUCCESSFUL_RESPONSE_CODE = 200;

    /**
     * The maximum HTTP response code indicating a successful response.
     */
    public static final int MAX_SUCCESSFUL_RESPONSE_CODE = 299;

    /**
     * The minimum HTTP response code indicating a successful redirection.
     */
    public static final int MIN_REDIRECTED_RESPONSE_CODE = 300;

    /**
     * The maximum HTTP response code indicating a successful redirection.
     */
    public static final int MAX_REDIRECTED_RESPONSE_CODE = 399;

    /**
     * The range of response codes that indicate a website as reachable/readable.
     */
    public static final Range<Integer> SITE_REACHABLE_RESPONSE_CODE_RANGE
            = Range.closed(MIN_SUCCESSFUL_RESPONSE_CODE, MAX_REDIRECTED_RESPONSE_CODE);

    /**
     * The prefix for https urls.
     */
    private static final String HTTPS = "https";

    /**
     * The prefix for http urls.
     */
    private static final String HTTP = "http";

    /**
     * Pings an HTTP URL. This effectively sends a HEAD request and returns {@code true}
     * if the response code is contained by {@link #SITE_REACHABLE_RESPONSE_CODE_RANGE}.
     *
     * @param url The HTTP URL to be pinged
     * @return whether the given HTTP URL has returned response code 200-399 on a HEAD request within the
     * given timeout
     */
    public static boolean urlReachable(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        if (url.startsWith(HTTPS)) {
            url = url.replaceAll("^" + HTTPS, HTTP);
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(SITE_PING_TIMEOUT);
            connection.setReadTimeout(SITE_PING_TIMEOUT);
            connection.setRequestMethod(HEAD);

            int responseCode = connection.getResponseCode();
            return SITE_REACHABLE_RESPONSE_CODE_RANGE.contains(responseCode);
        } catch (Exception ignored) {}

        return false;
    }

    /**
     * Reads from the provided url and returns the response.
     *
     * @param urlString the string of the url to ping and get contents from
     * @return the resulting url response
     */
    @CanIgnoreReturnValue /* Can be used to ensure a URL is valid as a Precondition */
    public static String readUrl(String urlString) {
        Preconditions.checkNotNull(urlString);
        Preconditions.checkArgument(!urlString.isEmpty());

        try {
            URL url = new URL(urlString);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            int read;
            char[] chars = new char[DOWNLOAD_RESOURCE_BUFFER_SIZE];

            while ((read = reader.read(chars)) != -1) {
                sb.append(chars, 0, read);
            }

            reader.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new FatalException("Error reading from url: " + urlString);
    }

    /**
     * Returns the title of the provided url according to {@link Jsoup}.
     *
     * @param url the url to get the title of
     * @return the title of the provided url
     */
    public static Optional<String> getUrlTitle(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        String ret = null;

        try {
            Document document = Jsoup.connect(url).get();
            return Optional.of(document.title());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Returns whether the provided url is constructed properly
     *
     * @param url the url to check for proper form
     * @return whether the provided url is of a valid form
     */
    public static boolean isValidUrl(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        return CyderRegexPatterns.urlFormationPattern.matcher(url).matches();
    }

    /**
     * The size of the buffer when downloading resources from a URL or reading a URL.
     */
    public static final int DOWNLOAD_RESOURCE_BUFFER_SIZE = 1024;

    /**
     * Downloads the resource at the provided link and save it to the provided file.
     * Note this method is blocking, invocation of it should be in a
     * surrounding thread as to not block the primary thread.
     *
     * @param urlResource   the link to download the file from
     * @param referenceFile the file to save the resource to
     * @return whether the downloading concluded without errors
     */
    public static boolean downloadResource(String urlResource, File referenceFile) throws IOException {
        Preconditions.checkNotNull(urlResource);
        Preconditions.checkArgument(!urlResource.isEmpty());
        Preconditions.checkArgument(isValidUrl(urlResource));
        Preconditions.checkNotNull(referenceFile);
        Preconditions.checkArgument(!referenceFile.exists());

        boolean created = false;

        if (!referenceFile.exists()) {
            try {
                created = referenceFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        if (!created) {
            throw new IOException("Could not create reference file: " + referenceFile);
        }

        try (BufferedInputStream in = new BufferedInputStream(new URL(urlResource).openStream()) ;
             FileOutputStream fileOutputStream = new FileOutputStream(referenceFile)) {

            byte[] dataBuffer = new byte[DOWNLOAD_RESOURCE_BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = in.read(dataBuffer, 0, DOWNLOAD_RESOURCE_BUFFER_SIZE)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Returns the ip of the user's computer if found.
     *
     * @return the ip of the user's computer if found
     */
    public static Optional<String> getIp() {
        try {
            return Optional.of(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    // todo need a unit test for this one

    /**
     * Returns the latency of the host system to the provided ip:port.
     *
     * @param ip      the ip to ping
     * @param port    the port to ping on the ip
     * @param timeout the time in ms to wait before timing out
     * @return the latency in ms between the host system and the latency ip
     * @throws NullPointerException     if the provided ip is null
     * @throws IllegalArgumentException if the provided ip is empty or invalid or the port
     *                                  is out of range or the timeout is less than or equal to zero
     * @throws IOException              if an exception occurs when attempting to connect to the latency ip
     */
    public static Future<Long> getLatency(String ip, int port, int timeout) throws IOException {
        Preconditions.checkNotNull(ip);
        Preconditions.checkArgument(!ip.trim().isEmpty());
        Preconditions.checkArgument(CyderRegexPatterns.ipv4Pattern.matcher(ip).matches());
        Preconditions.checkArgument(Port.portRange.contains(port));
        Preconditions.checkArgument(timeout > 0);

        Callable<Long> task = () -> {
            try (Socket socket = new Socket()) {
                SocketAddress address = new InetSocketAddress(ip, port);
                Stopwatch stopwatch = Stopwatch.createStarted();
                socket.connect(address, timeout);
                stopwatch.stop();
                return stopwatch.elapsed().toMillis();
            }
        };

        return Executors.newSingleThreadExecutor().submit(task);
    }

    /**
     * Returns whether the local port is available for binding.
     * Note this method blocks for approximately {@link #LOCAL_PORT_AVAILABLE_TIMEOUT}ms.
     *
     * @param port the local port
     * @return whether the local port is available for binding
     */
    public static boolean localPortAvailable(int port) {
        Preconditions.checkArgument(portRange.contains(port));

        AtomicBoolean ret = new AtomicBoolean(false);

        CyderThreadRunner.submit(() -> {
            try {
                ServerSocket socket = new ServerSocket(port);
                ret.set(true);
                socket.close();
            } catch (Exception ignored) {}
        }, "Local Port Available Finder, port: " + port);

        ThreadUtil.sleep(LOCAL_PORT_AVAILABLE_TIMEOUT.toMillis());

        return ret.get();
    }
}
