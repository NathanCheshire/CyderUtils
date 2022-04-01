package cyder.handlers.internal;

import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderShare;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.objects.InformBuilder;
import cyder.utilities.UserUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class ExceptionHandler {
    /**
     * Restrict default instantiation.
     */
    private ExceptionHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * This method takes an exception, prints it to a string, and then passes the
     * error to the SessionLogger to be logged
     * @param e the exception we are handling and possibly informing the user of
     */
    public static void handle(Exception e) {
        try {
            Optional<String> write = getPrintableException(e);

            if (write.isPresent() && !write.get().trim().isEmpty())
                Logger.log(LoggerTag.EXCEPTION, write.get());

            //if the user has show errors configured, then we open the file
            if (ConsoleFrame.INSTANCE.getUUID() != null &&
                    !ConsoleFrame.INSTANCE.isClosed() &&
                    UserUtil.getCyderUser().getSilenceerrors().equals("0")) {
                silentHandleWithoutLogging(e);
            }
        }

        //uh oh; error was thrown inside here, so we'll just generic inform the user of it
        catch (Exception ex) {
            silentHandleWithoutLogging(ex);
        }
    }

    /**
     * This method handles an exception the same way as {@link ExceptionHandler#handle(Exception)} (String)}
     * except it does so without informing the user/developer/etc.
     * @param e the exception to be silently handled
     */
    public static void silentHandle(Exception e) {
        try {
            Optional<String> write = getPrintableException(e);

            if (write.isPresent() && !write.get().trim().isEmpty())
                Logger.log(LoggerTag.EXCEPTION, write.get());
        } catch (Exception ex) {
            silentHandleWithoutLogging(ex);
        }
    }

    // todo remove need for, isn't the point to log everything? maybe you mean to debug print
    /**
     * This method handles an exception the same way as {@link ExceptionHandler#handle(Exception)} (String)}
     * except it does so without logging the exception.
     * @param e the exception to be handled without logging
     */
    public static void handleWithoutLogging(Exception e) {
        try {
            Optional<String> exception = getPrintableException(e);

            if (exception.isPresent() && !exception.get().trim().isEmpty())
                System.out.println(exception.get());
        } catch (Exception ex) {
            silentHandleWithoutLogging(ex);
        }
    }

    /**
     * Handles the exception by displaying a CyderFrame with the exception on it
     * (does not log the message. As such, this method should only be used in rare scenarios)
     * @param e the exception to be displayed
     */
    private static void silentHandleWithoutLogging(Exception e) {
       Logger.Debug(getPrintableException(e));
    }

    /**
     * Generates a printable version of the exception.
     *
     * @param e the exception to return a printable version of
     * @return Optional String possibly containing exception details and trace
     */
    public static Optional<String> getPrintableException(Exception e) {
        //should be highly unlikely if not impossible
        if (e == null)
            return Optional.empty();

        //init streams to get information from the Exception
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        //print to the stream
        e.printStackTrace(pw);

        //full stack trace
        int lineNumber = e.getStackTrace()[0].getLineNumber();
        String stackTrace = sw.toString();

        //one or more white space, "at" literal, one or more white space
        String[] stackSplit = stackTrace.split("\\s+at\\s+");

        StringBuilder exceptionPrintBuilder = new StringBuilder();

        if (stackSplit.length > 1) {
            exceptionPrintBuilder.append("\nException origin: ").append(stackSplit[1]);
        } else {
            exceptionPrintBuilder.append("\nException origin not found");
        }

        //line number
        if (lineNumber != 0)
            exceptionPrintBuilder.append("\nFrom line: ").append(lineNumber);
        else
            exceptionPrintBuilder.append("\nThrowing line not found");

        //trace
        exceptionPrintBuilder.append("\nTrace: ").append(stackTrace);

        return Optional.of(exceptionPrintBuilder.toString());
    }

    /**
     * Shows a popup with the provided error message. When the opened popup frame is disposed,
     * Cyder exits.
     *
     * @param message the message of the popup
     * @param title the title of the popup
     * @param condition the exit condition to log when exiting
     */
    public static void exceptionExit(String message, String title, ExitCondition condition) {
        InformBuilder builder = new InformBuilder(message);
        builder.setTitle(title);
        builder.setPostCloseAction(() -> CyderShare.exit(condition));
        InformHandler.inform(builder);
    }
}