package cyder.utilities;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StatUtil {
    private StatUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    public static void javaProperties() {
        ArrayList<String> PropertiesList = new ArrayList<>();
        Properties Props = System.getProperties();

        Enumeration<?> keys = Props.keys();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) Props.get(key);
            PropertiesList.add(key + ": " + value);
        }

        ConsoleFrame.INSTANCE.getInputHandler().println("Java Properties:\n------------------------");

        for (String s : PropertiesList) {
            ConsoleFrame.INSTANCE.getInputHandler().println(s);
        }
    }

    public static void systemProperties() {
        ArrayList<String> arrayLines = new ArrayList<>();
        arrayLines.add("File Separator: " + System.getProperty("file.separator"));
        arrayLines.add("Class Path: " + System.getProperty("java.class.path"));
        arrayLines.add("Java Home: " + System.getProperty("java.home"));
        arrayLines.add("Java Vendor: " + System.getProperty("java.vendor"));
        arrayLines.add("Java Vendor URL: " + System.getProperty("java.vendor.url"));
        arrayLines.add("Java Version: " + System.getProperty("java.version"));
        arrayLines.add("Line Separator: " + System.getProperty("line.separator"));
        arrayLines.add("OS Architecture: " + System.getProperty("os.arch"));
        arrayLines.add("OS Name: " + System.getProperty("os.name"));
        arrayLines.add("OS Version: " + System.getProperty("os.version"));
        arrayLines.add("OS Path Separator: " + System.getProperty("path.separator"));
        arrayLines.add("User Directory: " + OSUtil.USER_DIR);
        arrayLines.add("User Home: " + System.getProperty("user.home"));
        arrayLines.add("Computer Username: " + System.getProperty("user.name"));

        for (String arrayLine : arrayLines)
            ConsoleFrame.INSTANCE.getInputHandler().println(arrayLine);
    }

    public static void computerProperties() {
        ArrayList<String> arrayLines = new ArrayList<>();

        arrayLines.add("Available processors (cores): " + Runtime.getRuntime().availableProcessors());
        arrayLines.add("Free memory (bytes): " + Runtime.getRuntime().freeMemory());

        long maxMemory = Runtime.getRuntime().maxMemory();

        arrayLines.add("Maximum memory (bytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));
        arrayLines.add("Total memory available to JVM (bytes): " + Runtime.getRuntime().totalMemory());

        File[] roots = File.listRoots();

        for (File root : roots) {
            arrayLines.add("File system root: " + root.getAbsolutePath());
            arrayLines.add("Total space (bytes): " + root.getTotalSpace());
            arrayLines.add("Free space (bytes): " + root.getFreeSpace());
            arrayLines.add("Usable space (bytes): " + root.getUsableSpace());
        }

        for (String arrayLine : arrayLines)
            ConsoleFrame.INSTANCE.getInputHandler().println(arrayLine);
    }

    public static void allStats() {
        getDebugProps();
        computerProperties();
        javaProperties();
        systemProperties();
    }

    /**
     * A record type to hold the stats returned by {@link StatUtil#getDebugProps()}.
     */
    public record DebugStats(ImmutableList<String> lines, ImageIcon countryFlag) {
    }

    public static Future<DebugStats> getDebugProps() {
        Preconditions.checkArgument(!NetworkUtil.isHighLatency());

        return Executors.newSingleThreadExecutor(new CyderThreadFactory("test")).submit(() -> {
            DecimalFormat gByteFormatter = new DecimalFormat("##.###");
            double gBytes = Double.parseDouble(gByteFormatter
                    .format((((double) Runtime.getRuntime().freeMemory()) / 1024 / 1024 / 1024)));
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface netIn = NetworkInterface.getByInetAddress(address);

            BufferedImage flag = ImageIO.read(new URL(IPUtil.getIpdata().getFlag()));

            double x = flag.getWidth();
            double y = flag.getHeight();

            ImageIcon resized = new ImageIcon(ImageUtil.resizeImage(flag, 1, (int) (2 * x), (int) (2 * y)));

            return new DebugStats(ImmutableList.of(
                    "Time requested: " + TimeUtil.weatherTime(),
                    "ISP: " + IPUtil.getIpdata().getAsn().getName(),
                    "IP: " + IPUtil.getIpdata().getIp(),
                    "Postal Code: " + IPUtil.getIpdata().getPostal(),
                    "City: " + IPUtil.getIpdata().getCity(),
                    "State: " + IPUtil.getIpdata().getRegion(),
                    "Country: " + IPUtil.getIpdata().getCountry_name() + " ("
                            + IPUtil.getIpdata().getCountry_code() + ")",
                    "Latitude: " + IPUtil.getIpdata().getLatitude() + " Degrees N",
                    "Longitude: " + IPUtil.getIpdata().getLongitude() + " Degrees W",
                    "latency: " + NetworkUtil.latency(10000) + " ms",
                    "Google Reachable: " + NetworkUtil.siteReachable(CyderUrls.GOOGLE),
                    "YouTube Reachable: " + NetworkUtil.siteReachable(CyderUrls.YOUTUBE),
                    "Apple Reachable: " + NetworkUtil.siteReachable(CyderUrls.APPLE),
                    "Microsoft Reachable: " + NetworkUtil.siteReachable(CyderUrls.MICROSOFT),
                    "User Name: " + OSUtil.getSystemUsername(),
                    "Computer Name: " + OSUtil.getComputerName(),
                    "Available Cores: " + Runtime.getRuntime().availableProcessors(),
                    "Available Memory: " + gBytes + " GigaBytes",
                    "Operating System: " + OSUtil.OPERATING_SYSTEM_NAME,
                    "Java Version: " + System.getProperty("java.version"),
                    "Network Interface Name: " + netIn.getName(),
                    "Network Interface Display Name: " + netIn.getDisplayName(),
                    "Network MTU: " + netIn.getMTU(),
                    "Host Address: " + address.getHostAddress(),
                    "Local Host Address: " + InetAddress.getLocalHost(),
                    "Loopback Address: " + InetAddress.getLoopbackAddress()), resized);
        });
    }

    public static String fileByFileAnalyze(File startDir) {
        StringBuilder ret = new StringBuilder(
                "Numbers in order represent: code lines, comment lines, and blank lines respectively\n");

        ArrayList<File> javaFiles = OSUtil.getFiles(startDir, ".java");

        for (File f : javaFiles) {
            ret.append(f.getName().replace(".java", ""))
                    .append(": ").append(totalLines(f)).append(",")
                    .append(totalComments(f)).append(",")
                    .append(totalBlankLines(f)).append("\n");
        }

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
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    ret += totalJavaLines(f);
                }
            }
        } else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line;
                int localRet = 0;

                while ((line = lineReader.readLine()) != null)
                    //not blank and not a comment means a code line
                    if (!line.trim().isEmpty() && !isComment(line.trim()))
                        localRet++;

                return localRet;
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        return ret;
    }

    /**
     * Finds the total number of lines found within each java file provided the starting directory
     * to begin recursing from
     *
     * @param startDir the directory to begin recursing from
     * @return the total number of lines found
     */
    public static int totalLines(File startDir) {
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    ret += totalLines(f);
                }
            }
        } else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                int localRet = 0;

                while (lineReader.readLine() != null)
                    localRet++;

                return localRet;
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
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
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    ret += totalComments(f);
                }
            }
        } else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line;
                int localRet = 0;

                boolean blockComment = false;

                while ((line = lineReader.readLine()) != null) {
                    //rare case of this happening but needed to not trigger a long block comment
                    if (line.trim().startsWith("/*") && line.trim().endsWith("*/")) {
                        localRet++;
                        continue;
                    }

                    //start of a block comment
                    if (line.trim().startsWith("/*")) {
                        blockComment = true;
                    }
                    //end of a block comment
                    else if (line.trim().endsWith("*/")) {
                        blockComment = false;
                    }

                    //if we've activated block comment or still on, increment line count
                    if (blockComment)
                        localRet++;
                        //otherwise if the line has text and is a comment inc
                    else if (!line.trim().isEmpty() && (isComment(line)))
                        localRet++;
                }

                return localRet;
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
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
        return line.matches(CyderRegexPatterns.commentPattern.pattern());
    }

    /**
     * Finds the number of blank lines associated with .java files within the provided start directory
     *
     * @param startDir the directory to begin recursing from to find .java files
     * @return the number of blank lines found in the provided directory and subdirectories
     */
    public static int totalBlankLines(File startDir) {
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    ret += totalBlankLines(f);
                }
            }
        } else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line;
                int localRet = 0;

                while ((line = lineReader.readLine()) != null)
                    if (line.trim().isEmpty())
                        localRet++;

                return localRet;
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        return ret;
    }

    public static void fileSizes() {
        LinkedList<FileSize> prints = innerFileSizes(new File("../Cyder"));

        prints.sort(new FileComparator());

        for (FileSize print : prints) {
            ConsoleFrame.INSTANCE.getInputHandler().println(print.getName() + ": " + formatBytes(print.getSize()));
        }
    }

    private static LinkedList<FileSize> innerFileSizes(File startDir) {
        LinkedList<FileSize> ret = new LinkedList<>();

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    ret.addAll(innerFileSizes(f));
                }
            }
        } else {
            ret.add(new FileSize(startDir.getName(), startDir.length()));
        }

        return ret;
    }

    private static final class FileComparator implements Comparator<FileSize> {
        public int compare(FileSize fs1, FileSize fs2) {
            if (fs1.getSize() < fs2.getSize())
                return 1;
            else if (fs1.getSize() > fs2.getSize())
                return -1;
            return 0;
        }
    }

    private static String formatBytes(float bytes) {
        DecimalFormat formatter = new DecimalFormat("##.###");

        float coalesceSpace = 1024.0f;

        if (bytes >= coalesceSpace) {
            float kilo = bytes / coalesceSpace;

            if (kilo >= coalesceSpace) {
                float mega = kilo / coalesceSpace;

                if (mega >= coalesceSpace) {
                    float giga = mega / coalesceSpace;

                    if (giga >= coalesceSpace) {
                        float tera = giga / coalesceSpace;
                        return (formatter.format(tera) + "TB");
                    } else
                        return (formatter.format(giga) + "GB");
                } else
                    return (formatter.format(mega) + "MB");
            } else
                return (formatter.format(kilo) + "KB");
        } else
            return (bytes + " bytes");
    }

    public static void findBadWords() {
        innerFindBadWords(new File("cyder"));
    }

    private static void innerFindBadWords(File startDir) {
        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    innerFindBadWords(f);
                }
            }
        } else if (startDir.isFile() && !FileUtil.getFilename(startDir.getName()).equals("v.txt")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line;

                while ((line = lineReader.readLine()) != null) {
                    if (isComment(line) && StringUtil.containsBlockedWords(line, false)) {
                        ConsoleFrame.INSTANCE.getInputHandler().println(
                                FileUtil.getFilename(startDir.getName()) + ": " + line.trim());
                    }
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }
    }

    /**
     * Associated name of a file and it's size.
     */
    public static final class FileSize {
        /**
         * The size of the file.
         */
        private long size;

        /**
         * The name of the file.
         */
        private String name;

        /**
         * Creates a new file size object.
         *
         * @param name the name of the file
         * @param size the size of the file in bytes
         */
        public FileSize(String name, long size) {
            this.size = size;
            this.name = name;

            Logger.log(Logger.Tag.OBJECT_CREATION, this);
        }

        /**
         * Returns the size of the file in bytes.
         *
         * @return the size of the file in bytes
         */
        public long getSize() {
            return size;
        }

        /**
         * Sets the size of the file in bytes.
         *
         * @param size the size of the file in bytes
         */
        public void setSize(long size) {
            this.size = size;
        }

        /**
         * Returns the name of the file.
         *
         * @return the name of the file
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name of the file.
         *
         * @param name the name of the file
         */
        public void setName(String name) {
            this.name = name;
        }
    }
}