package cyder.handlers.internal;

import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.FileUtil;
import cyder.utilities.OSUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.TimeUtil;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

// todo if log line is greater than a certain char limit, need to split and add two tabs at beginning of broken lines

/**
 * Logger class used to log useful information about any Cyder instance from beginning at
 * runtime to exit at JVM termination.
 */
public class Logger {
    /**
     * Instances of Logger not allowed.
     */
    private Logger() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The counter used to log the number of objects created each deltaT seconds.
     */
    private static final AtomicInteger objectCreationCounter = new AtomicInteger();

    /**
     * The rate at which to log the amount of objects created since the last log.
     */
    public static final int deltaT = 5;

    /**
     * Whether the current log should not be written to again.
     */
    private static boolean logConcluded;

    /**
     * The file that is currently being written to on log calls.
     */
    private static File currentLog;

    /**
     * The absolute start time of Cyder, initialized at runtime.
     */
    public static final long start = System.currentTimeMillis();

    /**
     * Calls string.valueOf on the provided generic and prints to the debug console
     * using the debug tag.
     *
     * @param representation the object to debug print
     */
    public static <T> void Debug(T representation) {
        log(LoggerTag.DEBUG, String.valueOf(representation));
    }

    /**
     * The main log method to log an action associated with a type tag.
     * @param tag the type of data we are logging
     * @param representation the representation of the object
     * @param <T> the object instance of representation
     */
    public static <T> void log(LoggerTag tag, T representation) {
        // ignore weird calls to here even after an EXIT tag was provided
        // don't throw since that would cause an exception and a log call
        if (logConcluded)
            return;

        String initialTimeTag = "[" + TimeUtil.logTime() + "] ";
        StringBuilder logBuilder = new StringBuilder(initialTimeTag);

        switch (tag) {
            case CLIENT:
                //user inputs to the console
                logBuilder.append("[CLIENT]: ");
                logBuilder.append(representation);
                break;
            case CONSOLE_OUT:
                logBuilder.append("[CONSOLE_OUT]: ");
                if (representation instanceof String) {
                    logBuilder.append("[STRING] ");
                    logBuilder.append(representation);
                } else if (representation instanceof ImageIcon) {
                    logBuilder.append("[ICON] ");
                    logBuilder.append(representation);
                }
                // JComponent prints
                else if (representation instanceof JComponent) {
                    logBuilder.append("[JCOMPONENT] ");
                    logBuilder.append(representation);
                }
                //other console prints
                else {
                    logBuilder.append("[UNKNOWN CONSOLE_OUT TYPE] ");
                    logBuilder.append(representation);
                }
                break;
            case EXCEPTION:
                //any exceptions thrown are passed from ExceptionHandler to here
                logBuilder.append("[EXCEPTION]: ");
                logBuilder.append(representation);

                break;
            case LINK:
                //files opened, links opened
                logBuilder.append("[LINK]: ");
                if (representation instanceof File) {
                    logBuilder.append("[").append(FileUtil.getExtension((File) representation)).append("] ");
                }
                logBuilder.append(representation);
                break;
            case SUGGESTION:
                logBuilder.append("[SUGGESTION]: ").append(representation);
                break;
            case SYSTEM_IO:
                // General System IO
                logBuilder.append("[SYSTEM_IO]: ");
                logBuilder.append(representation);
                break;
            case LOGIN:
                //user logged in using recognize method
                //[LOGIN]: [NATHAN] AutoCyphered (STD Login)
                logBuilder.append("[LOGIN]: [");
                logBuilder.append(representation);
                logBuilder.append("]");
                break;
            case LOGOUT:
                //[LOGOUT]: [NATHAN]
                logBuilder.append("[LOGOUT]: ");
                logBuilder.append(representation);
                break;
            case JVM_ARGS:
                //[JVM_ARGS]:
                logBuilder.append("[JVM ARGS]: ");
                logBuilder.append(representation);
                break;
            case JVM_ENTRY:
                logBuilder.append("[JVM_ENTRY]: [");
                logBuilder.append(representation);
                logBuilder.append("]");
                break;
            case EXIT:
                //right before CyderCommon exits
                //[EXIT]: [RUNTIME] 1h 24m 31s
                logBuilder.append("[EXIT]: [RUNTIME] ");
                logBuilder.append(getRuntime()).append("\n");

                //end log
                logBuilder.append("[").append(TimeUtil.logTime()).append("] [EOL]: Log completed, exiting Cyder with exit code: ");

                ExitCondition cond = (ExitCondition) representation;

                logBuilder.append(cond.getCode())
                        .append(" [").append(cond.getDescription()).append("], exceptions thrown: ")
                        .append(countExceptions());

                //write
                writeLine(logBuilder.toString());

                logConcluded = true;

                //return to caller to exit immediately
                return;
            case CORRUPTION:
                //before user corruption method is called
                logBuilder.append("[CORRUPTION]: ").append(representation);
                break;
            case DEBUG:
                logBuilder.append("[DEBUG]: ");
                logBuilder.append(representation);
                break;
            case HANDLE_METHOD:
                logBuilder.append("[HANDLE]: ");
                logBuilder.append(representation);
                break;
            case WIDGET_OPENED:
                logBuilder.append("[WIDGET OPENED]: ");
                logBuilder.append(representation);
                break;
            case PREFERENCE_REFRESH:
                logBuilder.append("[PREFERENCE REFRESH INVOKED]: ");
                logBuilder.append(representation);
                break;
            case THREAD:
                logBuilder.append("[THREAD STARTED]: ");
                logBuilder.append(representation);
                break;
            case OBJECT_CREATION:
                objectCreationCounter.incrementAndGet();

                // don't write so return
                return;
            case AUDIO:
                logBuilder.append("[AUDIO]: ").append(representation);
                break;
            case UI_ACTION:
                logBuilder.append("[UI ACTION]: ").append(representation);
                break;
            case CONSOLE_LOAD:
                logBuilder.append("[CONSOLE LOADED]: ").append(representation);
                break;
            default:
                //this is here and not UNKNOWN as the default so that we can detect if
                // a log tag was added but not implemented
                throw new IllegalArgumentException("Handle case not found; you're probably an " +
                        "idiot and added an enum type but forgot to handle the case in Logger: " + tag);
        }

        //write to log file
        if (logBuilder.toString().equalsIgnoreCase(initialTimeTag))
            throw new IllegalArgumentException("Attempting to write nothing to the log file");
        writeLine(logBuilder.toString());
    }

    /**
     * Constructor for the logger to create a file and write to for the current session.
     */
    public static void initialize() {
        generateAndSetLogFile();

        // first log call should always be a JVM_ENTRY tag
        log(LoggerTag.JVM_ENTRY, OSUtil.getSystemUsername());

        startObjectCreationLogger();
        concludeLogs();
        consolidateLines();
        zipPastLogs();
    }

    /**
     * Getter for current log file
     * @return the log file associated with the current session
     */
    public static File getCurrentLog() {
        return currentLog;
    }

    /**
     * Creates the log file if it is not set/DNE
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void generateAndSetLogFile() {
        try {
            File logsDir = new File("logs");
            logsDir.mkdir();

            String logSubDirName = TimeUtil.logSubDirTime();

            File logSubDir = new File("logs/" + logSubDirName);
            logSubDir.mkdir();

            String logFileName = TimeUtil.logTime();

            int number = 1;
            File logFile = new File("logs/" + logSubDirName + "/" + logFileName + "-" + number + ".log");

            while (logFile.exists()) {
                number++;
                logFile = new File("logs/" + logSubDirName + "/" + logFileName + "-" + number + ".log");
            }

            boolean success = logFile.createNewFile();

            if (success)
                currentLog = logFile;
            else
                throw new RuntimeException("Log file not created");

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

    }

    private static final Semaphore writingSemaphore = new Semaphore(1);

    /**
     * Writes the line to the current log file and releases resources once done.
     *
     * @param line the single line to write
     */
    private static synchronized void writeLine(String line) {
        //if we have to make a new line
        String recoveryLine = null;

        //if the current log doesn't exist, find a unique file name and make it
        if (!getCurrentLog().exists()) {
            generateAndSetLogFile();

            recoveryLine = "[log file/directory was deleted during runtime, recreating and restarting log: "
                    + TimeUtil.userTime() + "]";
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentLog,true))) {
            writingSemaphore.acquire();

            if (recoveryLine != null) {
                bw.write(recoveryLine.trim());
            }

            bw.write(line.trim());
            bw.newLine();

            writingSemaphore.release();
        } catch(Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            // print to standard output, the only System.out in the entire program that should exist
            System.out.println(line.trim());
        }
    }

    /**
     * Counts the exceptions in the current log folder. This is used when closing the log to provide
     *  an exceptions summary.
     *
     * @return the int number of exceptions thrown in this Cyder session
     */
    private static int countExceptions() {
        int ret = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(currentLog))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("[EXCEPTION]"))
                    ret++;
            }
        } catch(Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    private static String getRuntime() {
        long millis = System.currentTimeMillis() - start;
        int seconds = 0;
        int hours = 0;
        int minutes = 0;

        while (millis > 1000) {
            seconds++;
            millis -= 1000;
        }

        while (seconds > 60) {
            minutes++;
            seconds -= 60;
        }

        while (minutes > 60) {
            hours++;
            minutes -= 60;
        }

        StringBuilder ret = new StringBuilder();

        if (hours != 0)
            ret.append(hours).append("h ");
        if (minutes != 0)
            ret.append(minutes).append("m ");
        if (seconds != 0)
            ret.append(seconds).append("s ");

        String retString = ret.toString().trim();

        return retString.isEmpty() ? "s" : retString;
    }

    /**
     * Zips the log files of the past.
     */
    public static void zipPastLogs() {
        File topLevelLogsDir = new File("logs");

        if (!topLevelLogsDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            topLevelLogsDir.mkdir();
            return;
        }

        File[] subLogDirs = topLevelLogsDir.listFiles();

        if (subLogDirs == null || subLogDirs.length == 0)
            return;

        // for all sub log dirs
        for (File subLogDir : subLogDirs) {
            // if it's not the current log and is not a zip file
            if (!FileUtil.getFilename(subLogDir.getName()).equals(TimeUtil.logSubDirTime())
                    && !FileUtil.getExtension(subLogDir).equalsIgnoreCase(".zip")) {
                // if a zip file for the directory exists, delete the dir
                if (new File(subLogDir.getAbsolutePath() + ".zip").exists()) {
                    OSUtil.delete(subLogDir);
                } else {
                    OSUtil.zip(subLogDir.getAbsolutePath(), subLogDir.getAbsolutePath() + ".zip");
                }
            }
        }
    }

    /**
     * Consolidates the lines of all non-zipped files within the logs/SubLogDir directory.
     */
    public static void consolidateLines() {
        File logsDir = new File("logs");

        if (!logsDir.exists())
            return;

        File[] subLogDirs = logsDir.listFiles();

        if (subLogDirs == null || subLogDirs.length == 0)
            return;

        for (File subLogDir : subLogDirs) {
            if (FileUtil.getExtension(subLogDir).equalsIgnoreCase(".zip"))
                continue;

            File[] logFiles = subLogDir.listFiles();

            if (logFiles == null || logFiles.length == 0)
                continue;

            for (File logFile : logFiles)
                consolidateLines(logFile);
        }
    }

    /**
     * Consolidates duplicate lines next to each other of the provided file.
     *
     * @param file the file to consolidate duplicate lines of
     */
    private static void consolidateLines(File file) {
        if (!file.exists())
            throw new IllegalArgumentException("Provided file does not exist: " + file);
        else if (!FileUtil.getExtension(file).equalsIgnoreCase(".log"))
            throw new IllegalArgumentException("Provided file is not a log file: " + file);

        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null)
                lines.add(line);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (lines.size() < 2)
            return;

        ArrayList<String> writeLines = new ArrayList<>();

        String lastLine;
        String currentLine;
        int currentCount = 1;

        for (int i = 0 ; i < lines.size() - 1; i++) {
            lastLine = lines.get(i);
            currentLine = lines.get(i + 1);

            if (logLinesEquivalent(lastLine, currentLine)) {
                currentCount++;
            } else {
                if (currentCount > 1) {
                    writeLines.add(lastLine + " [" + currentCount + "x]");
                } else{
                    writeLines.add(lastLine);
                }

                currentCount = 1;
            }
        }

        if (currentCount > 1) {
            writeLines.add(lines.get(lines.size() - 1) + " [" + currentCount + "x]");
        } else {
            writeLines.add(lines.get(lines.size() - 1));
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file,false))) {
            for (String line : writeLines) {
                bw.write(line.trim());
                bw.newLine();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns whether the two log lines are equivalent.
     *
     * @param logLine1 the first log line
     * @param logLine2 the second log line
     * @return whether the two log lines are equivalent
     */
    public static boolean logLinesEquivalent(String logLine1, String logLine2) {
        logLine1 = logLine1.trim();
        logLine2 = logLine2.trim();

        if (!logLine1.startsWith("[") || !logLine1.contains("]")
            || !logLine2.startsWith("[") || !logLine2.contains("]"))
            return logLine1.equals(logLine2);

        String timeTag1 = logLine1.substring(logLine1.indexOf("["), logLine2.indexOf("]") + 1).trim();
        String timeTag2 = logLine2.substring(logLine2.indexOf("["), logLine2.indexOf("]") + 1).trim();

        logLine1 = logLine1.replace(timeTag1, "");
        logLine2 = logLine2.replace(timeTag2, "");

        return !StringUtil.isNull(logLine1) && !StringUtil.isNull(logLine2) && logLine1.equals(logLine2);
    }

    /**
     * Upon entry this method attempts to fix any user logs that ended abruptly (an exit code of -1 )
     * as a result of an IDE stop or OS Task Manager Stop.
     */
    public static void concludeLogs() {
        try {
            File logDir = new File("logs");

            if (!logDir.exists())
                return;

            File[] logDirs = logDir.listFiles();

            if (logDirs == null || logDirs.length == 0)
                return;

            for (File subLogDir : logDirs) {
                //for all directories of days of logs
                if (FileUtil.getExtension(subLogDir).equalsIgnoreCase(".zip"))
                    continue;

                File[] logs = subLogDir.listFiles();

                if (logs == null || logs.length == 0)
                    return;

                for (File log : logs) {
                    if (!log.equals(getCurrentLog())) {
                        BufferedReader br = new BufferedReader(new FileReader(log));
                        String line;
                        boolean containsEOL = false;

                        int exceptions = 0;

                        while ((line = br.readLine()) != null) {
                            if (line.contains("[EOL]") || line.contains("[EXTERNAL STOP]")) {
                                containsEOL = true;
                                break;
                            } else if (line.contains("[EXCEPTION]")) {
                                exceptions++;
                            }
                        }

                        br.close();

                        if (!containsEOL) {
                            //usually an IDE stop but sometimes the program exits,
                            // with exit condition 1 due to something failing on startup
                            // which is why this says "crashed unexpectedly"
                            String logBuilder = "[" + TimeUtil.logTime() + "] [EOL]: " +
                                    "Log completed, Cyder crashed unexpectedly: " +
                                    "exit code: " + ExitCondition.ExternalStop.getCode() +
                                    " " + ExitCondition.ExternalStop.getDescription() +
                                    ", exceptions thrown: " + exceptions;

                            Files.write(Paths.get(log.getAbsolutePath()),
                                    (logBuilder).getBytes(), StandardOpenOption.APPEND);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Starts the object creation logger to log object creation calls every deltaT seconds.
     */
    private static void startObjectCreationLogger() {
        CyderThreadRunner.submit(() -> {
            try {
                // initial timeout from program initialization
                Thread.sleep(3000);

                while (true) {
                    if (objectCreationCounter.get() > 0) {
                        // a less elegant solution but necessary
                        writeLine("[" + TimeUtil.logTime() + "] [OBJECT CREATION]: "
                                + "Objects created since last delta (" + deltaT + "s): "
                                + objectCreationCounter.getAndSet(0));
                    }

                    // no need to check in small increments here
                    Thread.sleep(deltaT * 1000);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Object Creation Logger");
    }
}
