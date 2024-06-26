package com.github.natche.cyderutils.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.github.natche.cyderutils.enumerations.SystemPropertyKey;
import com.github.natche.cyderutils.exceptions.FatalException;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.strings.StringUtil;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Optional;

/** Utilities related to the JVM. */
public final class JvmUtil {
    /** The expected main method which Cyder should be launched from. */
    public static final String EXPECTED_MAIN_METHOD = "cyder.meta.Cyder";

    /** The bin string. */
    private static final String BIN = "bin";

    /** The thread name for the jvm args logger. */
    private static final String JVM_ARGS_LOGGER_THREAD_NAME = "JVM Args Logger";

    /**
     * The name of the javaw.exe executable. The difference between java and javaw
     * is that javaw does not open up a console window or use a console to display output on.
     */
    private static final String JAVAW = "javaw.exe";

    /** The name of the java.exe executable. */
    private static final String JAVA = "java.exe";

    /**
     * The string to look for when analyzing the runtime mx bean for whether the current JVM
     * has been launched in "debug mode" and may be suspended. This is identifiable by looking
     * for the agent lib command line argument referencing the Java Debug Wire Protocol.
     */
    private static final String IN_DEBUG_MODE_KEY_PHRASE = "-agentlib:jdwp";

    /**
     * The classpath argument.
     * Note that not using a double dash is correct here.
     */
    private static final String CLASSPATH_ARGUMENT = "-classpath";

    /** Whether the current jvm session is in debug mode meaning threads could be externally suspended. */
    private static final boolean JVM_LAUNCHED_IN_DEBUG_MODE = ManagementFactory.getRuntimeMXBean()
            .getInputArguments().toString().contains(IN_DEBUG_MODE_KEY_PHRASE);

    /** The name to use for the temporary directory cleaning exit hook. */
    private static final String REMOVE_TEMP_DIRECTORY_HOOK_NAME = "cyder-temporary-directory-cleaner-exit-hook";

    /** The list of shutdown hooks to be added to this instance of Cyder. */
    private static final ImmutableList<Thread> shutdownHooks = ImmutableList.of(
            // todo
//            CyderThreadRunner.createThread(() ->
//                            OsUtil.deleteFile(Dynamic.buildDynamic(Dynamic.TEMP.getFileName()), false),
//                    REMOVE_TEMP_DIRECTORY_HOOK_NAME)
    );

    /** The JVM args passed to the main method. */
    private static ImmutableList<String> jvmMainMethodArgs;

    /** The arguments parsed from {@link #jvmMainMethodArgs}. */
    private static ImmutableMap<String, String> parsedArgs = ImmutableMap.of();

    /** Suppress default constructor. */
    private JvmUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Sets the main method JVM args and parses them into a map.
     *
     * @param jvmMainMethodArgs the main method JVM args
     */
    public static void setAndParseJvmMainMethodArgs(ImmutableList<String> jvmMainMethodArgs) {
        Preconditions.checkNotNull(jvmMainMethodArgs);
        Preconditions.checkState(JvmUtil.jvmMainMethodArgs == null);

        JvmUtil.jvmMainMethodArgs = ImmutableList.copyOf(jvmMainMethodArgs);

        parseArgsMapFromArgs();
    }

    /** Parses the {@link #parsedArgs} from the {@link #jvmMainMethodArgs}. */
    private static void parseArgsMapFromArgs() {
        ImmutableMap.Builder<String, String> args = new ImmutableMap.Builder<>();

        String currentArgument = null;
        for (String argument : jvmMainMethodArgs) {
            if (isArgument(argument)) {
                if (currentArgument != null) args.put(currentArgument, "");
                currentArgument = removeLeadingDashes(argument);
                continue;
            }

            if (currentArgument == null) throw new FatalException("Failed to parse at argument: " + argument);
            args.put(currentArgument, argument);
            currentArgument = null;
        }

        if (currentArgument != null) args.put(currentArgument, "");
        parsedArgs = args.build();
    }

    /**
     * Returns whether the provided string is an argument as opposed to a parameter.
     *
     * @param string the string
     * @return whether the provided string is an argument as opposed to a parameter
     */
    private static boolean isArgument(String string) {
        Preconditions.checkNotNull(string);
        Preconditions.checkArgument(!string.isEmpty());

        return string.startsWith("--") || string.startsWith("-");
    }

    /**
     * Removes all leading dashes from the provided string.
     *
     * @param string the string
     * @return the string with all leading dashes removed
     */
    private static String removeLeadingDashes(String string) {
        Preconditions.checkNotNull(string);
        Preconditions.checkArgument(!string.isEmpty());

        return string.replaceAll("^-+", "");
    }

    /**
     * Returns the parameter corresponding to the provided argument if present. Empty optional else.
     *
     * @param argument the argument
     * @return the parameter corresponding to the provided argument if present. Empty optional else
     */
    public static Optional<String> getArgumentParam(String argument) {
        Preconditions.checkNotNull(argument);
        Preconditions.checkArgument(!argument.isEmpty());

        if (parsedArgs.containsKey(argument)) {
            return Optional.ofNullable(parsedArgs.get(argument));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns whether the parsed arguments contained the provided argument.
     *
     * @param argument the argument
     * @return whether the parsed arguments contained the provided argument
     */
    public static boolean mainMethodArgumentPresent(String argument) {
        Preconditions.checkNotNull(argument);
        Preconditions.checkArgument(!argument.isEmpty());

        return parsedArgs.containsKey(argument);
    }

    /**
     * Returns the main method JVM args.
     *
     * @return the main method JVM args
     */
    public static ImmutableList<String> getJvmMainMethodArgs() {
        Preconditions.checkNotNull(jvmMainMethodArgs);

        return jvmMainMethodArgs;
    }

    /**
     * Returns whether the current jvm session is in debug mode meaning
     * threads could be externally suspended.
     *
     * @return whether the current jvm session is in debug mode meaning
     * threads could be externally suspended
     */
    public static boolean currentInstanceLaunchedWithDebug() {
        return JVM_LAUNCHED_IN_DEBUG_MODE;
    }

    /**
     * Returns the total number of classes that have been loaded
     * since the Java virtual machine has started execution.
     *
     * @return the total number of classes that have been loaded
     * since the Java virtual machine has started execution
     */
    public static long getCurrentTotalLoadedClassCount() {
        return ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount();
    }

    /**
     * Returns a file reference to the java home of the JVM which is running the current instance of Cyder.
     *
     * @return a file reference to the java home of the JVM which is running the current instance of Cyder
     * @throws FatalException if the found file does not exist or is not a directory
     */
    public static File getCurrentJavaHome() {
        String javaHomePath = SystemPropertyKey.JAVA_HOME.getProperty();
        File javaHome = new File(javaHomePath);

        if (!javaHome.exists()) {
            throw new FatalException("Found java home does not exist: " + javaHome.getAbsolutePath());
        }
        if (!javaHome.isDirectory()) {
            throw new FatalException("Found java home is not a directory: " + javaHome.getAbsolutePath());
        }

        return javaHome;
    }

    /**
     * Returns the JVM name, that of the vm name, vm vendor, and vm version.
     *
     * @return the jvm name
     */
    public static String getJvmName() {
        return ManagementFactory.getRuntimeMXBean().getVmName()
                + " " + ManagementFactory.getRuntimeMXBean().getVmVendor()
                + " " + ManagementFactory.getRuntimeMXBean().getVmVersion();
    }

    /**
     * Returns a file reference to the java home bin of the JVM which is running the current instance of Cyder.
     *
     * @return a file reference to the java home bin of the JVM which is running the current instance of Cyder
     * @throws FatalException if the found file does not exist or is not a directory
     */
    public static File getCurrentJavaBin() {
        File bin = new File(getCurrentJavaHome(), BIN);

        if (!bin.exists()) {
            throw new FatalException("Found bin does not exist: " + bin.getAbsolutePath());
        }
        if (!bin.isDirectory()) {
            throw new FatalException("Found bin is not a directory: " + bin.getAbsolutePath());
        }

        return bin;
    }

    /**
     * Returns a file reference to the java home javaw.exe of the JVM which is running the current instance of Cyder.
     *
     * @return a file reference to the java home javaw.exe of the JVM which is running the current instance of Cyder
     * @throws FatalException if the found file does not exist or is not a file
     */
    public static File getCurrentJavaWExe() {
        File javaw = new File(getCurrentJavaBin(), JAVAW);

        if (!javaw.exists()) {
            throw new FatalException("Found javaw file does not exist: " + javaw.getAbsolutePath());
        }
        if (!javaw.isFile()) {
            throw new FatalException("Found javaw file is not a file: " + javaw.getAbsolutePath());
        }

        return javaw;
    }

    /**
     * Returns a file reference to the java home java.exe of the JVM which is running the current instance of Cyder.
     *
     * @return a file reference to the java home java.exe of the JVM which is running the current instance of Cyder
     * @throws FatalException if the found file does not exist or is not a file
     */
    public static File getCurrentJavaExe() {
        File java = new File(getCurrentJavaBin(), JAVA);

        if (!java.exists()) {
            throw new FatalException("Found java file does not exist: " + java.getAbsolutePath());
        }
        if (!java.isFile()) {
            throw new FatalException("Found java file is not a file: " + java.getAbsolutePath());
        }

        return java;
    }

    /**
     * Returns an {@link ImmutableList} of the {@link ManagementFactory}'s runtime MX bean input
     * arguments to the JVM not including the arguments passed to the main method of Cyder.
     *
     * @return an immutable list of JVM arguments not including the arguments passed to the main method
     */
    public static ImmutableList<String> getNonMainInputArguments() {
        return ImmutableList.copyOf(ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    /**
     * Returns the Java class path that is used by the system class loader to search for class files
     *
     * @return the Java class path
     */
    public static String getClassPath() {
        return SystemPropertyKey.JAVA_CLASS_PATH.getProperty();
    }

    /**
     * Returns the full command used to invoke the current JVM instance.
     * This includes java.exe path, input arguments from the runtime MX bean,
     * the class path, and the main method arguments.
     *
     * @return the full command used to invoke the current JVM instance.
     */
    public static String getFullJvmInvocationCommand() {
        StringBuilder inputArgumentsBuilder = new StringBuilder();
        getNonMainInputArguments().forEach(arg -> inputArgumentsBuilder
                .append("\"")
                .append(arg)
                .append("\"")
                .append(" "));
        String safeInputArguments = inputArgumentsBuilder.toString().trim();

        StringBuilder mainMethodArgumentsBuilder = new StringBuilder();
        getJvmMainMethodArgs().forEach(arg -> mainMethodArgumentsBuilder
                .append("\"")
                .append(arg)
                .append("\"")
                .append(" "));
        String mainMethodArgs = mainMethodArgumentsBuilder.toString().trim();

        String executablePath = StringUtil.escapeQuotes(getCurrentJavaExe().getAbsolutePath());
        String classpath = StringUtil.escapeQuotes(getClassPath());
        String sunJavaCommand = StringUtil.escapeQuotes(getMainMethodClass());

        return "\"" + executablePath + "\"" + " "
                + safeInputArguments + " "
                + CLASSPATH_ARGUMENT + " "
                + "\"" + classpath + "\"" + " "
                + "\"" + sunJavaCommand + "\"" + " "
                + mainMethodArgs;
    }

    /**
     * Returns the class responsible for starting Cyder.
     * Typically for Cyder this is "cyder.meta.Cyder".
     *
     * @return the class responsible for starting Cyder
     */
    public static String getMainMethodClass() {
        return SystemPropertyKey.SUN_JAVA_COMMAND.getProperty();
    }

    /**
     * Returns whether Cyder was started from the expected main method,
     * that of the one located in "cyder.meta.Cyder.java".
     *
     * @return whether Cyder was started from the expected main method
     */
    public static boolean cyderStartedFromExpectedMainMethod() {
        return getMainMethodClass().equals(EXPECTED_MAIN_METHOD);
    }

    /**
     * Calculates the run time of the JVM running Cyder.
     *
     * @return the run time of Cyder starting from JVM entry
     */
    public static long getRuntime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    /** Adds the exit hooks to this JVM. */
    public static void addExitHooks() {
        shutdownHooks.forEach(hook -> Runtime.getRuntime().addShutdownHook(hook));
    }
}
