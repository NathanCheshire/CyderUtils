package main.java.cyder.logging;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import main.java.cyder.enums.Dynamic;
import main.java.cyder.enums.ExitCondition;
import main.java.cyder.enums.Extension;
import main.java.cyder.exceptions.FatalException;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.files.FileUtil;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.props.Props;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.strings.StringUtil;
import main.java.cyder.threads.CyderThreadRunner;
import main.java.cyder.threads.IgnoreThread;
import main.java.cyder.threads.ThreadUtil;
import main.java.cyder.time.TimeUtil;
import main.java.cyder.utils.ColorUtil;
import main.java.cyder.utils.JvmUtil;
import main.java.cyder.utils.OsUtil;
import main.java.cyder.utils.ReflectionUtil;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;
import static main.java.cyder.logging.LoggingConstants.*;
import static main.java.cyder.logging.LoggingUtil.*;
import static main.java.cyder.strings.CyderStrings.*;

/**
 * Logger class used to log useful information about any Cyder instance from beginning at
 * runtime to exit at JVM termination.
 */
public final class Logger {
    /**
     * The counter used to log the number of objects created each deltaT seconds.
     */
    private static final AtomicInteger objectCreationCounter = new AtomicInteger();

    /**
     * The counter used to log the number of exceptions thrown
     * and handled during this session of Cyder.
     */
    private static final AtomicInteger exceptionsCounter = new AtomicInteger();

    /**
     * Whether the logger has been initialized.
     */
    private static final AtomicBoolean logStarted = new AtomicBoolean();

    /**
     * Whether the object creation logger has been started.
     */
    private static final AtomicBoolean objectCreationLoggerStarted = new AtomicBoolean();

    /**
     * Whether the logger has been initialized already.
     */
    private static final AtomicBoolean loggerInitialized = new AtomicBoolean();

    /**
     * The log calls that were requested to be logged before the logger was initialized
     * and are awaiting logger initialization.
     */
    private static final ArrayList<String> awaitingLogCalls = new ArrayList<>();

    /**
     * The total number of objects created for an instance of Cyder.
     */
    private static int totalObjectsCreated = 0;

    /**
     * Whether the current log should not be written to again.
     */
    private static boolean logConcluded;

    /**
     * The file that is currently being written to on log calls.
     */
    private static File currentLog;

    /**
     * Suppress default constructor.
     */
    private Logger() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the current log file.
     *
     * @return the log file associated with the current session
     */
    public static File getCurrentLogFile() {
        return currentLog;
    }

    /**
     * Initializes the logger for logging by invoking the following actions:
     *
     * <ul>
     *     <li>Wiping past logs if enabled</li>
     *     <li>Generating and setting the current log file</li>
     *     <li>Writing the Cyder Ascii art to the generated log file</li>
     *     <li>Logging the JVM entry with the OS username</li>
     *     <li>Starting the object creation logger</li>
     *     <li>Concluding past logs which may have ended abruptly</li>
     *     <li>Consolidating past log lines</li>
     *     <li>Zipping past logs directories</li>
     * </ul>
     */
    public static void initialize() {
        Preconditions.checkState(!loggerInitialized.get());
        loggerInitialized.set(true);

        if (Props.wipeLogsOnStart.getValue()) {
            OsUtil.deleteFile(Dynamic.buildDynamic(Dynamic.LOGS.getFileName()));
        }

        generateAndSetLogFile();
        writeCyderAsciiArtToFile(currentLog);
        log(LogTag.JVM_ENTRY, OsUtil.getOsUsername());
        startObjectCreationLogger();
        concludeLogs();
        consolidateLogLines();
        zipPastLogs();
    }

    // todo use me on start from boostrap

    /**
     * Initializes the logger for logging by invoking the following actions:
     *
     * <ul>
     *     <li>Wiping past logs if enabled</li>
     *     <li>Validating the provided log file</li>
     *     <li>Writing the Cyder Ascii art to the generated log file</li>
     *     <li>Logging the JVM entry with the OS username</li>
     *     <li>Starting the object creation logger</li>
     *     <li>Concluding past logs which may have ended abruptly</li>
     *     <li>Consolidating past log lines</li>
     *     <li>Zipping past logs directories</li>
     * </ul>
     *
     * @param logFile the log file to use
     */
    public static void initializeWithLogFile(File logFile) {
        Preconditions.checkState(!loggerInitialized.get());
        Preconditions.checkNotNull(logFile);
        Preconditions.checkArgument(logFile.exists());
        Preconditions.checkArgument(logFile.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(logFile, Extension.LOG.getExtension()));
        Preconditions.checkArgument(logFile.getParentFile().getParentFile().getAbsolutePath().equals(
                Dynamic.buildDynamic(Dynamic.LOGS.getFileName()).getAbsolutePath()));

        loggerInitialized.set(true);

        if (Props.wipeLogsOnStart.getValue()) {
            OsUtil.deleteFile(Dynamic.buildDynamic(Dynamic.LOGS.getFileName()));
        }

        currentLog = logFile;
        writeCyderAsciiArtToFile(currentLog);
        log(LogTag.JVM_ENTRY, OsUtil.getOsUsername());
        startObjectCreationLogger();
        concludeLogs();
        consolidateLogLines();
        zipPastLogs();
    }

    /**
     * Creates the top level logs directory, the log sub-directory for today,
     * and the log file for this session if it is not generated or set.
     */
    private static void generateAndSetLogFile() {
        try {
            File logsDir = Dynamic.buildDynamic(Dynamic.LOGS.getFileName());
            if (!logsDir.exists() && !logsDir.mkdir()) {
                throw new FatalException("Failed to create logs directory");
            }

            String logSubDirName = TimeUtil.logSubDirTime();
            File logSubDir = Dynamic.buildDynamic(Dynamic.LOGS.getFileName(), logSubDirName);
            if (!logSubDir.exists() && !logSubDir.mkdir()) {
                throw new FatalException("Failed to create log directory for current day");
            }

            File proposedLogFile = new File(TimeUtil.logTime() + Extension.LOG.getExtension());
            String uniqueFilename = FileUtil.constructUniqueName(proposedLogFile, logSubDir);
            File logFile = Dynamic.buildDynamic(
                    Dynamic.LOGS.getFileName(), logSubDirName, uniqueFilename);

            if (OsUtil.createFile(logFile, true)) {
                currentLog = logFile;
            } else {
                throw new FatalException("Log file not created");
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Logs the provided statement to the log file, using the calling class name as the tag.
     *
     * @param statement the statement to log preceding the tags
     * @param <T>       the type of the statement
     */
    public static <T> void log(T statement) {
        log(ReflectionUtil.getBottomLevelClass(StackWalker.getInstance().getCallerClass()), statement);
    }

    /**
     * Logs the provided statement to the log file.
     *
     * @param tag       the primary log tag
     * @param statement the statement to log preceding the tags
     * @param <T>       the type of the statement
     */
    public static <T> void log(String tag, T statement) {
        constructLogLinesAndLog(ImmutableList.of(tag), statement.toString());
    }

    /**
     * The main log method to log an action associated with a type tag.
     *
     * @param tag       the type of data we are logging
     * @param statement the statement to log preceding the tags
     * @param <T>       the type of the statement
     */
    public static <T> void log(LogTag tag, T statement) {
        Preconditions.checkNotNull(tag);
        Preconditions.checkNotNull(statement);

        if (statement instanceof String string && StringUtil.isNullOrEmpty(string)) return;

        ArrayList<String> tags = new ArrayList<>();
        StringBuilder logBuilder = new StringBuilder();

        // Unique tags have a case statement, default ones do not
        switch (tag) {
            case CONSOLE_OUT:
                tags.add(LogTag.CONSOLE_OUT.getLogName());
                switch (statement) {
                    case String string -> {
                        tags.add(ConsoleOutType.STRING.toString());
                        logBuilder.append(string);
                    }
                    case ImageIcon icon -> {
                        tags.add(ConsoleOutType.IMAGE.toString());
                        logBuilder.append("dimensions=")
                                .append(icon.getIconWidth()).append(CyderStrings.X).append(icon.getIconHeight())
                                .append(comma)
                                .append(space)
                                .append("dominant color")
                                .append(colon)
                                .append(space)
                                .append(ColorUtil.getDominantColor(icon));
                    }
                    case JComponent jComponent -> {
                        tags.add(ConsoleOutType.J_COMPONENT.toString());
                        logBuilder.append(jComponent);
                    }
                    case default -> {
                        tags.add(constructTagsPrepend(StringUtil.capsFirstWords(
                                ReflectionUtil.getBottomLevelClass(statement.getClass()))));
                        logBuilder.append(statement);
                    }
                }
                break;
            case EXCEPTION:
                tags.add(LogTag.EXCEPTION.getLogName());
                logBuilder.append(statement);

                exceptionsCounter.getAndIncrement();
                break;
            case LINK:
                tags.add(LogTag.LINK.getLogName());

                if (statement instanceof File file) {
                    tags.add(FileUtil.getExtension(file));
                    logBuilder.append(file.getAbsolutePath());
                } else {
                    logBuilder.append(statement);
                }

                break;
            case LOGOUT:
                tags.add(LogTag.LOGIN_OUTPUT.getLogName());
                tags.add(USER);
                logBuilder.append(statement);
                break;
            case JVM_ENTRY:
                tags.add(LogTag.JVM_ENTRY.getLogName());
                logBuilder.append(statement);

                logStarted.set(true);

                break;
            case PROGRAM_EXIT:
                logConcluded = true;

                if (statement instanceof ExitCondition exitCondition) {
                    concludeLog(currentLog,
                            exitCondition,
                            JvmUtil.getRuntime(),
                            exceptionsCounter.get(),
                            totalObjectsCreated,
                            CyderThreadRunner.getThreadsRan());
                } else {
                    throw new FatalException("Provided statement is not of type ExitCondition, statement: "
                            + statement + ", class: " + ReflectionUtil.getBottomLevelClass(statement.getClass()));
                }

                return;
            case PREFERENCE:
                tags.add(LogTag.PREFERENCE.getLogName());
                tags.add("Key");
                logBuilder.append(statement);
                break;
            case OBJECT_CREATION:
                if (statement instanceof String) {
                    tags.add(LogTag.OBJECT_CREATION.getLogName());
                    logBuilder.append(statement);
                } else {
                    objectCreationCounter.incrementAndGet();
                    return;
                }

                break;
            default:
                tags.add(tag.getLogName());
                logBuilder.append(statement);
                break;
        }

        constructLogLinesAndLog(tags, logBuilder.toString());
    }

    /**
     * Constructs lines from the tags and line and writes them to the current log file.
     * The provided tags and translated into proper tags with the time tag preceding all tags.
     * If the line exceeds that of {@link LoggingConstants#maxLogLineLength}
     * then the line is split where convenient.
     *
     * @param tags the tags
     * @param line the line
     */
    private static void constructLogLinesAndLog(List<String> tags, String line) {
        Preconditions.checkNotNull(tags);
        Preconditions.checkArgument(!tags.isEmpty());
        Preconditions.checkNotNull(line);
        Preconditions.checkArgument(!line.isEmpty());

        if (logStarted.get() && currentLog == null) {
            generateAndSetLogFile();
            writeCyderAsciiArtToFile(currentLog);
            awaitingLogCalls.addAll(checkLogLineLength(getLogRecoveryDebugLine()));
        }

        boolean isException = tags.contains(LogTag.EXCEPTION.getLogName());
        String prepend = constructTagsPrepend(tags);
        String rawWriteLine = prepend + line;

        ImmutableList<String> lengthCheckedLines = isException
                ? ImmutableList.of(rawWriteLine)
                : checkLogLineLength(rawWriteLine);

        ArrayList<String> prefixedLines = new ArrayList<>();

        for (int i = 0 ; i < lengthCheckedLines.size() ; i++) {
            String prefixSpacing = "";
            if (i != 0 && !isException) {
                prefixSpacing = StringUtil.generateSpaces(prepend.length());
            }

            String writeLine = prefixSpacing + lengthCheckedLines.get(i);
            prefixedLines.add(writeLine);
        }

        writeRawLinesToCurrentLogFile(ImmutableList.copyOf(prefixedLines));
    }

    /**
     * Writes the provided lines directly to the current log file without any processing
     *
     * @param lines the raw lines to write directory to the current log file
     */
    private static void writeRawLinesToCurrentLogFile(ImmutableList<String> lines) {
        Preconditions.checkNotNull(lines);

        if (!logStarted.get()) {
            awaitingLogCalls.addAll(lines);
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentLog, true))) {
            if (!awaitingLogCalls.isEmpty()) {
                for (String awaitingLogLine : awaitingLogCalls) {
                    out.println(awaitingLogLine);
                    writer.write(awaitingLogLine);
                    writer.newLine();
                }

                awaitingLogCalls.clear();
            }

            for (String line : lines) {
                if (!logConcluded) {
                    out.println(line);
                    writer.write(line);
                    writer.newLine();
                } else {
                    out.println("Log call after log completed: " + line);
                }
            }
        } catch (Exception e) {
            log(LogTag.EXCEPTION, ExceptionHandler.getPrintableException(e));
        }
    }

    /**
     * Zips the log files of the past.
     */
    private static void zipPastLogs() {
        File topLevelLogsDir = Dynamic.buildDynamic(Dynamic.LOGS.getFileName());

        if (!topLevelLogsDir.exists()) {
            if (!topLevelLogsDir.mkdir()) {
                throw new FatalException("Failed to create logs dir");
            }

            return;
        }

        File[] subLogDirs = topLevelLogsDir.listFiles();
        if (subLogDirs == null || subLogDirs.length == 0) return;

        for (File subLogDir : subLogDirs) {
            // Skip current log parent directory
            if (subLogDir.getAbsolutePath().equals(getCurrentLogFile().getParentFile().getAbsolutePath())) continue;
            if (FileUtil.getExtension(subLogDir).equals(Extension.ZIP.getExtension())) continue;

            String destinationZipPath = subLogDir.getAbsolutePath() + Extension.ZIP.getExtension();
            File destinationZip = new File(destinationZipPath);

            if (!destinationZip.exists()) {
                Logger.log(LogTag.DEBUG, "Zipping past sub log dir: " + subLogDir.getAbsolutePath());

                FileUtil.zip(subLogDir.getAbsolutePath(), destinationZipPath);
            }

            OsUtil.deleteFile(subLogDir);
        }
    }

    /**
     * Consolidates the lines of all non-zipped files within the logs/SubLogDir directory.
     */
    private static void consolidateLogLines() {
        File logsDir = Dynamic.buildDynamic(Dynamic.LOGS.getFileName());

        if (!logsDir.exists()) return;

        File[] subLogDirs = logsDir.listFiles();

        if (subLogDirs == null || subLogDirs.length == 0) return;

        for (File subLogDir : subLogDirs) {
            if (FileUtil.getExtension(subLogDir).equalsIgnoreCase(Extension.ZIP.getExtension())) continue;

            File[] logFiles = subLogDir.listFiles();

            if (logFiles == null || logFiles.length == 0) continue;

            for (File logFile : logFiles) {
                log(LogTag.DEBUG, "Consolidating lines of file: " + logFile.getName());
                consolidateLines(logFile);
            }
        }
    }

    /**
     * Consolidates duplicate lines next to each other of the provided log file.
     *
     * @param logFile the file to consolidate duplicate lines of
     */
    private static void consolidateLines(File logFile) {
        Preconditions.checkNotNull(logFile);
        Preconditions.checkArgument(logFile.exists());
        Preconditions.checkArgument(logFile.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(logFile, Extension.LOG.getExtension()));

        ImmutableList<String> logLines = getLogLinesFromLog(logFile);

        // If there's only one line, consolidating doesn't make sense now does it?
        if (logLines.size() < 2) return;

        ArrayList<String> writeLines = new ArrayList<>();

        String lastLine;
        String currentLine = "";
        int currentCount = 1;

        for (int i = 1 ; i < logLines.size() ; i++) {
            lastLine = logLines.get(i - 1);
            currentLine = logLines.get(i);

            if (areLogLinesEquivalent(lastLine, currentLine)) {
                currentCount++;
            } else {
                if (currentCount > 1) {
                    writeLines.add(generateConsolidationLine(lastLine, currentCount));
                } else {
                    writeLines.add(lastLine);
                }

                currentCount = 1;
            }
        }

        // Last read line hasn't been added yet
        if (currentCount > 1) {
            writeLines.add(generateConsolidationLine(currentLine, currentCount));
        } else {
            writeLines.add(currentLine);
        }

        writeCyderAsciiArtToFile(logFile);
        FileUtil.writeLinesToFile(logFile, writeLines, true);
    }

    /**
     * Fixes any logs lacking/not ending in an "End Of Log" tag.
     */
    private static void concludeLogs() {
        try {
            File logDir = Dynamic.buildDynamic(Dynamic.LOGS.getFileName());
            if (!logDir.exists()) return;

            File[] subLogDirs = logDir.listFiles();
            if (subLogDirs == null || subLogDirs.length == 0) return;

            for (File subLogDir : subLogDirs) {
                if (!subLogDir.isDirectory()) continue;

                File[] logFiles = subLogDir.listFiles();
                if (logFiles == null || logFiles.length == 0) return;

                for (File logFile : logFiles) {
                    if (logFile.equals(getCurrentLogFile())) continue;

                    if (countTags(logFile, EOL) < 1) {
                        concludeLog(logFile,
                                ExitCondition.TrueExternalStop,
                                getRuntimeFromLog(logFile),
                                countExceptions(logFile),
                                countObjectsCreatedFromLog(logFile),
                                countThreadsRan(logFile));
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Concludes the provided log file using the provided parameters.
     *
     * @param file           the log file to conclude
     * @param condition      the exit condition
     * @param runtime        the runtime in ms of the log
     * @param exceptions     the exceptions thrown in the log
     * @param objectsCreated the objects created during the log
     * @param threadsRan     the number of threads ran during the log
     */
    private static void concludeLog(File file,
                                    ExitCondition condition,
                                    long runtime,
                                    int exceptions,
                                    long objectsCreated,
                                    int threadsRan) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.LOG.getExtension()));
        Preconditions.checkNotNull(condition);
        Preconditions.checkArgument(runtime >= 0);
        Preconditions.checkArgument(exceptions >= 0);
        Preconditions.checkArgument(objectsCreated >= 0);
        Preconditions.checkArgument(threadsRan >= 0);

        String write = constructTagsPrepend(EOL)
                + END_OF_LOG
                + newline
                + constructTagsPrepend(EXIT_CONDITION)
                + condition.getCode()
                + comma
                + space
                + condition.getDescription()
                + newline
                + constructTagsPrepend(RUNTIME)
                + TimeUtil.formatMillis(runtime)
                + newline
                + constructTagsPrepend(StringUtil.getPlural(exceptions, EXCEPTION))
                + exceptions
                + newline
                + constructTagsPrepend(OBJECTS_CREATED)
                + objectsCreated
                + newline
                + constructTagsPrepend(THREADS_RAN)
                + threadsRan;
        out.println(write);
        FileUtil.writeLinesToFile(file, ImmutableList.of(write), true);
    }

    /**
     * Starts the object creation logger to log object creation calls every deltaT seconds.
     */
    private static void startObjectCreationLogger() {
        Preconditions.checkState(!objectCreationLoggerStarted.get());

        objectCreationLoggerStarted.set(true);

        CyderThreadRunner.submit(() -> {
            try {
                ThreadUtil.sleep(INITIAL_OBJECT_CREATION_LOGGER_TIMEOUT);

                while (true) {
                    int objectsCreated = objectCreationCounter.getAndSet(0);
                    if (objectsCreated > 0) {
                        logObjectsCreated(objectsCreated);
                    }

                    ThreadUtil.sleep(objectCreationLogFrequency);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, IgnoreThread.ObjectCreationLogger.getName());
    }

    /**
     * Logs the number of objects created since the last delta.
     *
     * @param objectsCreated the number of objects created since the last delta
     */
    private static void logObjectsCreated(int objectsCreated) {
        totalObjectsCreated += objectsCreated;

        String line = objectsCreatedSinceLastDelta
                + space + openingParenthesis + objectCreationLogFrequency
                + TimeUtil.MILLISECOND_ABBREVIATION + closingParenthesis
                + colon + space + objectsCreated;

        log(LogTag.OBJECT_CREATION, line);
    }
}