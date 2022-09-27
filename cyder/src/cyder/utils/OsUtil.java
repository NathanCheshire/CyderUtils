package cyder.utils;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.exceptions.UnsupportedOsException;
import cyder.genesis.Cyder;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.user.UserUtil;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper methods to sort out differences between operating systems Cyder might be running on.
 */
public final class OsUtil {
    /**
     * A list of the restricted windows filenames due to backwards compatibility
     * and the nature of "APIs are forever".
     */
    public static final ImmutableList<String> invalidWindowsFilenames = ImmutableList.of(
            "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8",
            "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    );

    /**
     * Whether Cyder is being run as a compiled JAR file.
     */
    public static final boolean JAR_MODE;

    static {
        JAR_MODE = Objects.requireNonNull(
                Cyder.class.getResource("Cyder.class")).toString().startsWith("jar:");

        Logger.log(LogTag.DEBUG, "Jar mode set as: " + String.valueOf(JAR_MODE).toUpperCase());
    }

    /**
     * Suppress default constructor.
     */
    private OsUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns whether the provided filename is valid for the operating system
     * Cyder is currently running on.
     *
     * @param filename the desired filename
     * @return whether the provided filename is valid for the operating system
     * Cyder is currently running on
     */
    public static boolean isValidFilename(String filename) {
        filename = filename.trim();

        switch (OPERATING_SYSTEM) {
            case OSX:
                return filename.contains("/") || filename.contains("\0");
            case WINDOWS:
                // invalid chars for Windows in a filename
                if (filename.matches(CyderRegexPatterns.windowsInvalidFilenameChars.pattern()))
                    return false;

                // invalid filenames for windows, reserved names for backwards compatibility reasons
                for (String invalidName : invalidWindowsFilenames) {
                    if (filename.equalsIgnoreCase(invalidName)) {
                        return false;
                    }
                }

                if (filename.contains(".")) {
                    String[] parts = filename.split("\\.");

                    for (String part : parts) {
                        for (String invalidName : invalidWindowsFilenames) {
                            if (part.equalsIgnoreCase(invalidName)) {
                                return false;
                            }
                        }
                    }
                }

                return !filename.endsWith(".");
            case UNIX:
                //root dir
                if (filename.contains("/") || filename.contains(">") || filename.contains("<")
                        || filename.contains("|") || filename.contains(":") || filename.contains("&"))
                    return false;

                break;
            case UNKNOWN:
                throw new UnsupportedOsException("Unknown operating system: " + OPERATING_SYSTEM_NAME);
        }

        return true;
    }

    /**
     * The raw operating system name.
     */
    public static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name");

    /**
     * Controlled program exit that calls System.exit which will also invoke the shutdown hook.
     *
     * @param exitCondition the exiting code to describe why the program exited (0 is standard
     *                      but for this program, the key/value pairs in {@link ExitCondition} are followed)
     */
    public static void exit(ExitCondition exitCondition) {
        try {
            //ensures IO finishes and is not invoked again
            UserUtil.blockFutureIo();

            //log exit
            Logger.log(LogTag.EXIT, exitCondition);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        System.exit(exitCondition.getCode());
    }

    /**
     * The three primary operating systems.
     */
    public enum OperatingSystem {
        OSX, WINDOWS, UNIX, UNKNOWN
    }

    /**
     * The standard operating system enum.
     */
    public static final OperatingSystem OPERATING_SYSTEM;

    static {
        if (isWindows()) {
            OPERATING_SYSTEM = OperatingSystem.WINDOWS;
        } else if (isOsx()) {
            OPERATING_SYSTEM = OperatingSystem.OSX;
        } else if (isUnix()) {
            OPERATING_SYSTEM = OperatingSystem.UNIX;
        } else {
            OPERATING_SYSTEM = OperatingSystem.UNKNOWN;
        }
    }

    /**
     * The file separator character used for this operating system.
     */
    public static final String FILE_SEP = System.getProperty("file.separator");

    /**
     * The maximum number of times something should be attempted to be deleted.
     */
    public static final int MAX_FILE_DELETION_ATTEMPTS = 500;

    /**
     * The maximum number of times something should be attempted to be created.
     */
    public static final int MAX_FILE_CREATION_ATTEMPTS = 500;

    /**
     * The default user directory.
     */
    public static final String USER_DIR = System.getProperty("user.dir");

    /**
     * The root of the Windows file system.
     */
    public static final String WINDOWS_ROOT = "c:/";

    /**
     * The prefix to determine if an operating system is Windows based.
     */
    private static final String WINDOWS_PREFIX = "win";

    /**
     * Returns whether the operating system is windows.
     *
     * @return whether the operating system is windows
     */
    public static boolean isWindows() {
        return OPERATING_SYSTEM_NAME.toLowerCase().contains(WINDOWS_PREFIX);
    }

    /**
     * Returns whether the operating system is OSX.
     *
     * @return whether the operating system is OSX
     */
    public static boolean isOsx() {
        return OPERATING_SYSTEM_NAME.toLowerCase().contains("mac");
    }

    /**
     * Returns whether the operating system is unix based.
     * (yes this includes OSX systems too. If you need to test for
     * OSX specifically then call {@link OsUtil#isOsx()})
     *
     * @return whether the operating system is unix based
     */
    public static boolean isUnix() {
        return OPERATING_SYSTEM_NAME.toLowerCase().contains("nix")
                || OPERATING_SYSTEM_NAME.toLowerCase().contains("nux")
                || OPERATING_SYSTEM_NAME.toLowerCase().contains("aix");
    }

    /**
     * The prefix used to determine if an operating system is Solaris.
     */
    private static final String SOLARIS_PREFIX = "sunos";

    /**
     * Returns whether the operating system is Solaris.
     *
     * @return whether the operating system is Solaris
     */
    public static boolean isSolaris() {
        return OPERATING_SYSTEM_NAME.toLowerCase().contains(SOLARIS_PREFIX);
    }

    //end base operating system name/type setup logic

    /**
     * Opens the command shell for the operating system.
     */
    public static void openShell() {
        try {
            switch (OPERATING_SYSTEM) {
                case WINDOWS -> Runtime.getRuntime().exec("cmd");
                case UNIX, OSX -> {
                    String[] args = {"/bin/bash", "-c"};
                    new ProcessBuilder(args).start();
                }
                case UNKNOWN, default -> throw new UnsupportedOsException(
                        "Unknown operating system type: " + OPERATING_SYSTEM);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the name of the shell this operating system defaults to.
     *
     * @return the name of the shell this operating system defaults to
     */
    public static String getShellName() {
        return switch (OPERATING_SYSTEM) {
            case OSX -> "Terminal (Bash)";
            case WINDOWS -> "Command prompt";
            case UNIX -> "Bash";
            case UNKNOWN -> "Unknown Shell";
        };
    }

    /**
     * Builds the provided strings into a filepath by inserting the OS' path separators.
     * Example: ["alpha","beta","gamma","delta.txt"] on Windows would return
     * alpha\beta\gamma\delta.txt
     *
     * @param directories the names of directories to add one after the other
     * @return the formatted path
     */
    public static String buildPath(String... directories) {
        checkNotNull(directories);
        checkArgument(directories.length > 0);

        StringBuilder ret = new StringBuilder();

        for (int i = 0 ; i < directories.length ; i++) {
            ret.append(directories[i]);

            if (i != directories.length - 1) {
                ret.append(FILE_SEP);
            }
        }

        return ret.toString();
    }

    /**
     * Builds the provided strings into a file by inserting the OS' path
     * separators between the provided path strings.
     * Example: buildFile("alpha","beta","gamma","delta.txt") on Windows would be equivalent to typing:
     * new File("alpha\beta\gamma\delta.txt")
     *
     * @param directories the names of directories to add one after the other
     * @return a reference to a file which may or may not exist
     */
    public static File buildFile(String... directories) {
        return new File(buildPath(directories));
    }

    /**
     * Returns the username of the operating system user.
     *
     * @return the username of the operating system user
     */
    public static String getOsUsername() {
        return System.getProperty("user.name");
    }

    /**
     * Returns the name of the computer Cyder is currently running on.
     *
     * @return the name of the computer Cyder is currently running on
     */
    public static String getComputerName() {
        String name = CyderStrings.NOT_AVAILABLE;

        try {
            InetAddress address = InetAddress.getLocalHost();
            name = address.getHostName();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
        return name;
    }

    /**
     * Sets the location of the mouse on screen.
     *
     * @param x the x value to set the mouse to
     * @param y the y value to set the mouse to
     */
    public static void setMouseLoc(int x, int y) {
        try {
            Console.INSTANCE.getInputHandler().getRobot().mouseMove(x, y);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Sets the mouse to the middle of the provided component.
     *
     * @param c the component to move the mouse to the center of
     */
    public static void setMouseLoc(Component c) {
        checkNotNull(c);

        try {
            Point topLeft = c.getLocationOnScreen();

            int x = (int) (topLeft.getX() + c.getWidth() / 2);
            int y = (int) (topLeft.getY() + c.getHeight() / 2);

            setMouseLoc(x, y);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Deletes the provided file/folder recursively.
     *
     * @param folder the folder/file to delete
     * @return whether the folder/file was successfully deleted
     */
    @CanIgnoreReturnValue
    public static boolean deleteFile(File folder) {
        checkNotNull(folder);

        return deleteFile(folder, true);
    }

    /**
     * Deletes the provided file/folder recursively.
     *
     * @param fileOrFolder the folder/file to delete
     * @param log          whether to log the delete operation. Ideally this is
     *                     always true but some rare cases require logging to be skipped
     * @return whether the folder/file was successfully deleted
     */
    @CanIgnoreReturnValue
    public static boolean deleteFile(File fileOrFolder, boolean log) {
        checkNotNull(fileOrFolder);

        if (log) {
            Logger.log(LogTag.SYSTEM_IO, "Requested deletion of: " + fileOrFolder.getAbsolutePath());
        }

        // directory means recursive case to delete contents
        if (fileOrFolder.isDirectory()) {
            File[] files = fileOrFolder.listFiles();

            if (files != null && files.length != 0) {
                for (File file : files) {
                    deleteFile(file, log);
                }
            }
        }

        // contents deleted so now can delete as if it was a file if it isn't
        int inc = 0;
        while (inc < MAX_FILE_DELETION_ATTEMPTS) {
            if (fileOrFolder.delete()) {
                return true;
            }

            inc++;
        }

        if (fileOrFolder.exists() && log) {
            Logger.log(LogTag.SYSTEM_IO, "[DELETION FAILED] file: "
                    + fileOrFolder.getAbsolutePath());
        }

        return false;
    }

    /**
     * Creates the provided file/folder if possible.
     *
     * @param file   the file/folder to attempt to create
     * @param isFile whether to treat the file as a directory or as a file
     * @return whether the file/folder could be created
     */
    public static boolean createFile(File file, boolean isFile) {
        checkNotNull(file);

        try {
            int inc = 0;
            while (inc < MAX_FILE_CREATION_ATTEMPTS) {
                boolean created;

                if (isFile) {
                    created = file.createNewFile();
                } else {
                    created = file.mkdirs();
                }

                // success
                if (file.exists() && created) {
                    return true;
                }

                inc++;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return false;
    }

    /**
     * Returns a string representation of all the network devices connected to the host.
     *
     * @return a string representation of all the network devices connected to the host
     */
    public static String getNetworkDevicesString() {
        StringBuilder sb = new StringBuilder();

        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface networkInterface : Collections.list(nets)) {
                sb.append("Display name:").append(networkInterface.getDisplayName()).append("\n");
                sb.append("Name:").append(networkInterface.getName()).append("\n");
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return sb.toString();
    }

    /**
     * Returns a string representation of all the monitors connected to the host.
     *
     * @return a string representation of all the monitors connected to the host
     */
    public static String getMonitorStatsString() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();

        StringBuilder sb = new StringBuilder();

        for (int i = 0 ; i < gs.length ; i++) {
            DisplayMode dm = gs[i].getDisplayMode();
            sb.append(i);
            sb.append(", width: ");
            sb.append(dm.getWidth());
            sb.append(", height: ");
            sb.append(dm.getHeight());
            sb.append(", bit depth: ");
            sb.append(dm.getBitDepth());
            sb.append(", refresh rate: ");
            sb.append(dm.getRefreshRate());
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Sets the operating system's clipboard to the provided String.
     *
     * @param clipboardContents the String to set the operating system's clipboard to
     */
    public static void setClipboard(String clipboardContents) {
        checkNotNull(clipboardContents);

        StringSelection selection = new StringSelection(clipboardContents);
        java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    /**
     * Executes the provided process and prints the output to the provided input handler.
     * <p>
     * Note that this is executed on the current thread so surround invocation of this method
     * with a new thread to avoid blocking the calling thread.
     *
     * @param pipeTo  the input handle to print the output to
     * @param builder the process builder to run
     */
    public static void runAndPrintProcess(BaseInputHandler pipeTo, ProcessBuilder builder) {
        checkNotNull(pipeTo);
        checkNotNull(builder);

        try {
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                pipeTo.println(line);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Executes the provided processes successively and prints the output to the provided input handler.
     *
     * @param pipeTo   the input handle to print the output to
     * @param builders the process builders to run
     */
    public static void runAndPrintProcessesSequential(BaseInputHandler pipeTo, ProcessBuilder... builders) {
        checkNotNull(pipeTo, "pipeTo is null");
        checkNotNull(builders, "builders are null");
        checkArgument(builders.length > 0, "must be at least one builder");

        CyderThreadRunner.submit(() -> {
            for (ProcessBuilder builder : builders) {
                runAndPrintProcess(pipeTo, builder);
            }
        }, "Successive Process Runner, pipeTo = " + pipeTo + ", builders.length() = " + builders.length);
    }

    /**
     * Ensures the dynamic directory and all subdirectories are created.
     */
    public static void ensureDynamicsCreated() {
        File dynamic = new File(Dynamic.PATH);

        boolean dynamicExists = dynamic.exists();

        if (!dynamicExists) {
            dynamicExists = dynamic.mkdir();
        }

        if (!dynamicExists) {
            throw new FatalException("Could nto create dynamic directory");
        }

        for (Dynamic dynamicDirectory : Dynamic.values()) {
            File currentDynamic = buildFile(Dynamic.PATH, dynamicDirectory.getDirectoryName());

            if (dynamicDirectory == Dynamic.TEMP) {
                deleteFile(currentDynamic);
            }

            if (!currentDynamic.exists() && !createFile(currentDynamic, false)) {
                throw new FatalException("Failed to create dynamic directory: "
                        + currentDynamic.getName() + " at location: " + currentDynamic.getAbsolutePath());
            }
        }
    }

    /**
     * Returns whether the provided binary could be found by invoking
     * the base command in the native shell.
     *
     * @param invokeCommand the invoke command such as ffmpeg for ffmpeg
     * @return whether the binary could be located
     */
    public static boolean isBinaryInstalled(String invokeCommand) {
        checkNotNull(invokeCommand);
        checkArgument(!invokeCommand.isEmpty());

        try {
            Runtime.getRuntime().exec(invokeCommand);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Returns whether the provided exe exist in the dynamic/exes directory.
     *
     * @param filename the filename of the exe such as ffmpeg.exe
     * @return whether the file could be located
     */
    public static boolean isBinaryInExes(String filename) {
        checkNotNull(filename);
        checkArgument(!filename.isEmpty());

        File exes = buildFile(Dynamic.PATH, Dynamic.EXES.getDirectoryName());

        if (exes.exists()) {
            File[] exeFiles = exes.listFiles();

            if (exeFiles != null && exeFiles.length > 0) {
                for (File exe : exeFiles) {
                    if (exe.getName().equalsIgnoreCase(filename)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * The decimal formatter used to format file byte numbers.
     */
    private static final DecimalFormat BYTE_FORMATTER = new DecimalFormat("##.###");

    /**
     * The amount necessary to turn said many lower units into the next unit up.
     */
    public static final float coalesceSpace = 1024.0f;

    /**
     * The prefix for a terabyte.
     */
    public static final String TERABYTE_PREFIX = "TB";

    /**
     * The prefix for a gigabyte.
     */
    public static final String GIGABYTE_PREFIX = "GB";

    /**
     * The prefix for a megabyte.
     */
    public static final String MEGABYTE_PREFIX = "MB";

    /**
     * The prefix for a kilobyte.
     */
    public static final String KILOBYTE_PREFIX = "KB";

    /**
     * The bytes word.
     */
    public static final String BYTES = "bytes";

    /**
     * Formats the provided number of bytes in a human-readable format.
     * Example: passing 1024MB would return 1GB.
     *
     * @param bytes the raw number of bytes to coalesce
     * @return a formatted string detailing the number of bytes provided
     */
    public static String formatBytes(float bytes) {
        boolean negative = bytes < 0.0f;
        if (negative) bytes *= -1.0f;
        String sign = negative ? "-" : "";

        if (bytes >= coalesceSpace) {
            float kilo = bytes / coalesceSpace;

            if (kilo >= coalesceSpace) {
                float mega = kilo / coalesceSpace;

                if (mega >= coalesceSpace) {
                    float giga = mega / coalesceSpace;

                    if (giga >= coalesceSpace) {
                        float tera = giga / coalesceSpace;
                        return sign + (BYTE_FORMATTER.format(tera) + TERABYTE_PREFIX);
                    } else
                        return sign + (BYTE_FORMATTER.format(giga) + GIGABYTE_PREFIX);
                } else
                    return sign + (BYTE_FORMATTER.format(mega) + MEGABYTE_PREFIX);
            } else
                return sign + (BYTE_FORMATTER.format(kilo) + KILOBYTE_PREFIX);
        } else {
            return sign + bytes + " " + BYTES;
        }
    }
}