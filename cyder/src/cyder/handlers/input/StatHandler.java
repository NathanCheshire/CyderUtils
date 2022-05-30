package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.common.WidgetDescription;
import cyder.constants.CyderStrings;
import cyder.enums.DynamicDirectory;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.*;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.Future;

/**
 * A handler for finding and printing statistics.
 */
public class StatHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private StatHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle({"debug", "countlogs", "computerproperties", "systemproperties", "tests",
            "networkaddresses", "filesizes", "badwords", "widgets", "analyzecode", "javaproperties",
            "threads", "daemonthreads"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("debug")) {
            CyderThreadRunner.submit(() -> {
                try {
                    for (String prop : StatUtil.getComputerMemorySpaces()) {
                        getInputHandler().println(prop);
                    }

                    for (String prop : StatUtil.getJavaProperties()) {
                        getInputHandler().println(prop);
                    }

                    for (String prop : StatUtil.getSystemProperties()) {
                        getInputHandler().println(prop);
                    }

                    Future<StatUtil.DebugStats> futureStats = StatUtil.getDebugProps();

                    while (!futureStats.isDone()) {
                        Thread.onSpinWait();
                    }

                    StatUtil.DebugStats stats = futureStats.get();

                    for (String line : stats.lines()) {
                        getInputHandler().println(line);
                    }

                    getInputHandler().println(stats.countryFlag());
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "Debug Stat Finder");
        } else if (getInputHandler().inputWithoutSpacesIs("computerproperties")) {
            getInputHandler().println("This may take a second since this feature counts your PC's free memory");

            CyderThreadRunner.submit(() -> {
                for (String prop : StatUtil.getComputerMemorySpaces()) {
                    getInputHandler().println(prop);
                }
            }, "Computer Memory Computer");
        } else if (getInputHandler().inputWithoutSpacesIs("systemproperties")) {
            for (String prop : StatUtil.getSystemProperties()) {
                getInputHandler().println(prop);
            }
        } else if (getInputHandler().commandIs("countlogs")) {
            File[] logDirs = new File(OSUtil.buildPath(
                    DynamicDirectory.DYNAMIC_PATH, DynamicDirectory.LOGS.getDirectoryName())).listFiles();
            int count = 0;
            int days = 0;

            if (logDirs != null && logDirs.length > 0) {
                for (File logDir : logDirs) {
                    days++;

                    File[] logDirFiles = logDir.listFiles();

                    if (logDirFiles != null && logDirFiles.length > 0) {
                        for (File log : logDirFiles) {
                            if (FileUtil.getExtension(log).equals(".log")
                                    && !logDir.equals(Logger.getCurrentLog())) {
                                count++;
                            }
                        }
                    }
                }
            }

            getInputHandler().println("Number of log dirs: " + days);
            getInputHandler().println("Number of logs: " + count);
        } else if (getInputHandler().commandIs("tests")) {
            getInputHandler().println("Valid tests to call:\n");
            getInputHandler().printlns(ReflectionUtil.getManualTests());
        } else if (getInputHandler().commandIs("networkaddresses")) {
            try {
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

                for (NetworkInterface netInterface : Collections.list(nets)) {
                    getInputHandler().println("Display name: " + netInterface.getDisplayName());
                    getInputHandler().println("Name: " + netInterface.getName());

                    Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
                    for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                        getInputHandler().println("InetAddress: " + inetAddress);
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        } else if (getInputHandler().commandIs("filesizes")) {
            for (StatUtil.FileSize fileSize : StatUtil.fileSizes()) {
                getInputHandler().println(fileSize.name() + ": " + OSUtil.formatBytes(fileSize.size()));
            }
        } else if (getInputHandler().commandIs("badwords")) {
            if (OSUtil.JAR_MODE) {
                getInputHandler().println("Bad words not available in jar mode");
            } else {
                CyderThreadRunner.submit(() -> {
                    getInputHandler().println("Finding bad words:");

                    for (String line : StatUtil.findBadWords()) {
                        getInputHandler().println(line);
                    }

                    getInputHandler().println("Concluded");
                }, "Bad Word Finder");
            }
        } else if (getInputHandler().commandIs("widgets")) {
            ArrayList<WidgetDescription> descriptions = ReflectionUtil.getWidgetDescriptions();

            getInputHandler().println("Found " + descriptions.size() + " widgets:");
            getInputHandler().println("-------------------------------------");

            for (WidgetDescription description : descriptions) {
                StringBuilder triggers = new StringBuilder();

                for (int i = 0 ; i < description.triggers().length ; i++) {
                    triggers.append(description.triggers()[i]);

                    if (i != description.triggers().length - 1)
                        triggers.append(", ");
                }

                getInputHandler().println("Name: " + description.name());
                getInputHandler().println("Description: " + description.description() + "\nTriggers: ["
                        + triggers.toString().trim() + "]");
                getInputHandler().println("-------------------------------------");
            }
        } else if (getInputHandler().commandIs("analyzecode")) {
            if (OSUtil.JAR_MODE) {
                getInputHandler().println("Code analyzing is not available when in Jar mode");
            } else {
                if (getInputHandler().checkArgsLength(0)
                        || getInputHandler().checkArgsLength(1)) {
                    File startDir = new File("cyder");

                    if (getInputHandler().checkArgsLength(1)) {
                        startDir = new File(getInputHandler().getArg(0));

                        if (!startDir.exists()) {
                            getInputHandler().println("Invalid root directory");
                            startDir = new File("cyder");
                        }
                    }

                    File finalStartDir = startDir;

                    CyderThreadRunner.submit(() -> {
                        int codeLines = StatUtil.totalJavaLines(finalStartDir);
                        int commentLines = StatUtil.totalComments(finalStartDir);

                        getInputHandler().println("Total lines: " + StatUtil.totalLines(finalStartDir));
                        getInputHandler().println("Code lines: " + codeLines);
                        getInputHandler().println("Blank lines: " + StatUtil.totalBlankLines(finalStartDir));
                        getInputHandler().println("Comment lines: " + commentLines);
                        getInputHandler().println("Classes: " + ReflectionUtil.CYDER_CLASSES.size());

                        float ratio = ((float) codeLines / (float) commentLines);
                        getInputHandler().println("Code to comment ratio: " + new DecimalFormat("#0.00").format(ratio));
                    }, "Code Analyzer");
                } else {
                    getInputHandler().println("analyzecode usage: analyzecode [path/to/the/root/directory] " +
                            "(leave path blank to analyze Cyder)");
                }
            }
        } else if (getInputHandler().commandIs("javaproperties")) {
            for (String prop : StatUtil.getJavaProperties()) {
                getInputHandler().println(prop);
            }
        } else if (getInputHandler().commandIs("threads")) {
            getInputHandler().printlns(ThreadUtil.getThreads());
        } else if (getInputHandler().commandIs("daemonthreads")) {
            getInputHandler().printlns(ThreadUtil.getDaemonThreads());
        } else {
            ret = false;
        }

        return ret;
    }
}