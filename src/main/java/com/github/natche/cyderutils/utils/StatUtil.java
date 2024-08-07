package com.github.natche.cyderutils.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.github.natche.cyderutils.constants.CyderRegexPatterns;
import com.github.natche.cyderutils.enumerations.Extension;
import com.github.natche.cyderutils.enumerations.SystemPropertyKey;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.files.FileUtil;
import com.github.natche.cyderutils.image.CyderImage;
import com.github.natche.cyderutils.network.NetworkUtil;
import com.github.natche.cyderutils.network.ipdataco.IpDataManager;
import com.github.natche.cyderutils.network.ipdataco.models.IpData;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.threads.CyderThreadFactory;
import com.github.natche.cyderutils.time.TimeUtil;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/** Utilities for general statistics related to Cyder. */
public final class StatUtil {
    /** If I hit it one time ima pipe her. */
    private static final String NO_LIMIT = "no limit";

    /** The name of the executor service which gets the debug props. */
    private static final String DEBUG_PROPS_EXECUTOR_THREAD_NAME = "Debug Props Getter";

    /** The starting prefix for a block comment. */
    private static final String blockCommentStart = "/*";

    /** The ending suffix for a block comment. */
    private static final String blockCommentEnd = "*/";

    /** A record type to hold the stats returned by {@link StatUtil#getDebugProps(String)}. */
    public record DebugStats(ImmutableList<String> lines, ImageIcon countryFlag) {}

    /** A record to associate a file name with its size. */
    public record FileSize(String name, long size) {}

    /** Suppress default constructor. */
    private StatUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns an immutable list detailing the java system properties of the current JVM.
     *
     * @return an immutable list detailing the java system properties of the current JVM
     */
    public static ImmutableList<String> getSystemProperties() {
        ArrayList<String> ret = new ArrayList<>();

        Arrays.stream(SystemPropertyKey.values()).forEach(systemPropertyKey ->
                ret.add(systemPropertyKey.getDescription() + ", key: " + systemPropertyKey.getKey()
                        + ", value: " + systemPropertyKey.getProperty()));

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns an immutable list detailing the found computer memory spaces.
     * <p>
     * Note: invocation of this method should be done in a separate thread
     * since computation of free memory may take some time.
     *
     * @return an immutable list detailing the found computer properties
     */
    public static ImmutableList<String> getComputerMemorySpaces() {
        ArrayList<String> ret = new ArrayList<>();

        ret.add("Available processors (cores): " + Runtime.getRuntime().availableProcessors());
        ret.add("Free memory: " + OsUtil.formatBytes(Runtime.getRuntime().freeMemory()));

        long maxMemory = Runtime.getRuntime().maxMemory();

        ret.add("Maximum memory: " +
                (maxMemory == Long.MAX_VALUE ? NO_LIMIT : OsUtil.formatBytes(maxMemory)));
        ret.add("Total memory available to JVM: " + OsUtil.formatBytes(Runtime.getRuntime().totalMemory()));

        Arrays.stream(File.listRoots()).forEach(root -> {
            ret.add("File system root: " + root.getAbsolutePath());
            ret.add("Total space (root): " + OsUtil.formatBytes(root.getTotalSpace()));
            ret.add("Free space (root): " + OsUtil.formatBytes(root.getFreeSpace()));
            ret.add("Usable space (root): " + OsUtil.formatBytes(root.getUsableSpace()));
        });

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns a debug object containing the found user flag and some common debug details.
     *
     * @return a debug object containing the found user flag and some common debug details
     */
    public static Future<DebugStats> getDebugProps(String ipDataKey) {
        // todo Preconditions.checkArgument(!NetworkUtil.isHighLatency());

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(DEBUG_PROPS_EXECUTOR_THREAD_NAME)).submit(() -> {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface netIn = NetworkInterface.getByInetAddress(address);

            IpDataManager ipDataManager = new IpDataManager(ipDataKey);
            IpData data = ipDataManager.getIpData();
            // todo kill manager

            CyderImage image = CyderImage.fromUrl(data.getFlag());
            image.resizeImage(image.getWidth() * 2, image.getHeight() * 2);

            return new DebugStats(
                    ImmutableList.of(
                            "Time requested: " + TimeUtil.weatherTime(),
                            "ISP: " + data.getAsn().getName(),
                            "IP: " + data.getIp(),
                            "Postal Code: " + data.getPostal(),
                            "City: " + data.getCity(),
                            "State: " + data.getRegion(),
                            "Country: " + data.getCountryName()
                                    + " (" + data.getContinentCode() + ")",
                            "Latitude: " + data.getLatitude() + " Degrees N",
                            "Longitude: " + data.getLongitude() + " Degrees W",
                            //todo "latency: " + LatencyManager.INSTANCE.getLatency(10000) + " ms",
                            "Google Reachable: " + NetworkUtil.urlReachable("https://www.google.com"),
                            "YouTube Reachable: " + NetworkUtil.urlReachable("https://www.youtube.com"),
                            "Apple Reachable: " + NetworkUtil.urlReachable("https://www.apple.com"),
                            "Microsoft Reachable: " + NetworkUtil.urlReachable("https://www.microsoft.com"),
                            "User Name: " + OsUtil.getOsUsername(),
                            "Computer Name: " + OsUtil.getComputerName(),
                            "Available Cores: " + Runtime.getRuntime().availableProcessors(),
                            "Available Memory: " + OsUtil.formatBytes(Runtime.getRuntime().freeMemory()),
                            "Operating System: " + OsUtil.OPERATING_SYSTEM_NAME,
                            "Java Version: " + SystemPropertyKey.JAVA_VERSION.getProperty(),
                            "Network Interface Name: " + netIn.getName(),
                            "Network Interface Display Name: " + netIn.getDisplayName(),
                            "Network MTU: " + netIn.getMTU(),
                            "Host Address: " + address.getHostAddress(),
                            "Local Host Address: " + InetAddress.getLocalHost(),
                            "Loopback Address: " + InetAddress.getLoopbackAddress()),
                    image.getImageIcon());
        });
    }

    /**
     * Returns a string representing statistics found about all .java files found from the starting directory such as
     * comment lines, total lines, and blank lines.
     *
     * @param startDir the directory to start from
     * @return a string representing statistics found about all .java files found from the starting directory such as
     * * comment lines, total lines, and blank lines
     */
    public static String fileByFileAnalyze(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        StringBuilder ret = new StringBuilder("Numbers in order represent: "
                + "code lines, comment lines, and blank lines respectively" + CyderStrings.newline);

        FileUtil.getFiles(startDir, true, Extension.JAVA.getExtension()).forEach(javaFile ->
                ret.append(javaFile.getName().replace(Extension.JAVA.getExtension(), ""))
                        .append(": ").append(totalLines(javaFile)).append(",")
                        .append(totalComments(javaFile)).append(",")
                        .append(totalBlankLines(javaFile)).append(CyderStrings.newline));

        return ret.toString();
    }

    /**
     * Finds the total number of Java lines found within .java files
     * and recursive directories within the provided starting directory
     *
     * @param startDir the directory to begin recursing from
     * @return the total number of java code lines found
     */
    public static int totalJavaLines(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        AtomicInteger ret = new AtomicInteger();

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                Arrays.stream(files).forEach(file -> ret.addAndGet(totalJavaLines(file)));
            }
        } else if (startDir.getName().endsWith(Extension.JAVA.getExtension())) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line;
                int localRet = 0;

                while ((line = lineReader.readLine()) != null) {
                    if (isCodeLine(line)) {
                        localRet++;
                    }
                }

                return localRet;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return ret.get();
    }

    /**
     * Returns whether the provided line is a code line meaning it is not blank or a comment line.
     *
     * @param line the line
     * @return whether the provided line is a code line
     */
    private static boolean isCodeLine(String line) {
        Preconditions.checkNotNull(line);

        return !line.trim().isEmpty() && !isComment(line.trim());
    }

    /**
     * Finds the total number of lines found within each java file provided the starting directory
     * to begin recursing from
     *
     * @param startDir the directory to begin recursing from
     * @return the total number of lines found
     */
    public static int totalLines(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    ret += totalLines(f);
                }
            }
        } else if (startDir.getName().endsWith(Extension.JAVA.getExtension())) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                int localRet = 0;

                while (lineReader.readLine() != null)
                    localRet++;

                return localRet;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Finds the number of java comments associated with all .java files
     * within the directory and recursively located directories provided
     *
     * @param startDir the directory to begin recursing from
     * @return the raw number of comments found
     */
    public static int totalComments(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    ret += totalComments(f);
                }
            }
        } else if (startDir.getName().endsWith(Extension.JAVA.getExtension())) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line;
                int localRet = 0;

                boolean blockComment = false;

                while ((line = lineReader.readLine()) != null) {
                    if (line.trim().startsWith(blockCommentStart) && line.trim().endsWith(blockCommentEnd)) {
                        localRet++;
                        continue;
                    }

                    if (line.trim().startsWith(blockCommentStart)) {
                        blockComment = true;
                    } else if (line.trim().endsWith(blockCommentEnd)) {
                        blockComment = false;
                    }

                    if (blockComment) {
                        localRet++;
                    } else if (!line.trim().isEmpty() && (isComment(line))) {
                        localRet++;
                    }
                }

                return localRet;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Determines if the provided line is a comment line
     *
     * @param line the string in question to possibly be a comment
     * @return whether the line is a comment
     */
    public static boolean isComment(String line) {
        Preconditions.checkNotNull(line);

        return line.matches(CyderRegexPatterns.commentPattern.pattern());
    }

    /**
     * Finds the number of blank lines associated with .java files within the provided start directory
     *
     * @param startDir the directory to begin recursing from to find .java files
     * @return the number of blank lines found in the provided directory and subdirectories
     */
    public static int totalBlankLines(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    ret += totalBlankLines(f);
                }
            }
        } else if (startDir.getName().endsWith(Extension.JAVA.getExtension())) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line;
                int localRet = 0;

                while ((line = lineReader.readLine()) != null)
                    if (line.trim().isEmpty())
                        localRet++;

                return localRet;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    private static ArrayList<FileSize> innerFileSizes(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        ArrayList<FileSize> ret = new ArrayList<>();

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                Arrays.stream(files).forEach(file -> ret.addAll(innerFileSizes(file)));
            }
        } else {
            ret.add(new FileSize(startDir.getName(), startDir.length()));
        }

        return ret;
    }

    /** The file comparator used for comparing files by their sizes in bytes. */
    private static final class FileComparator implements Comparator<FileSize> {
        public int compare(FileSize fs1, FileSize fs2) {
            if (fs1.size() < fs2.size()) {
                return 1;
            } else if (fs1.size() > fs2.size()) {
                return -1;
            }

            return 0;
        }
    }
}