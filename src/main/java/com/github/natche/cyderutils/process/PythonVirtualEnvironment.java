package com.github.natche.cyderutils.process;

import com.github.natche.cyderutils.files.FileUtil;
import com.github.natche.cyderutils.threads.CyderThreadFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class for encapsulating a particular Python virtual environment and performing operations
 * such as package installations, running scripts or commands through the venv,
 * and querying for package versions.
 */
public final class PythonVirtualEnvironment {
    /** A regex pattern to match the Python version. */
    private static final Pattern pythonVersionPattern = Pattern.compile("Python\\s(\\d+\\.\\d+\\.\\d+)");

    /** A regex pattern to match the version line from a pip show command. */
    private static final Pattern pipShowVersionPattern = Pattern.compile("^Version: (.+)$");

    /** The group to extract the Python or library version from. */
    private static final int versionGroup = 1;

    /** The File pointer to the Python executable. */
    private final File pythonExecutable;

    /** The version of Python pointed to by the executable. */
    private final String version;

    private PythonVirtualEnvironment(File pythonExecutable) {
        this.pythonExecutable = pythonExecutable;

        Future<ProcessResult> result = ProcessUtil.getProcessOutput(ImmutableList.of(
                pythonExecutable.getAbsolutePath(),
                "--version"
        ));
        while (!result.isDone()) Thread.onSpinWait();
        try {
            ImmutableList<String> versionResults = result.get().getStandardOutput();
            String firstVersionResult = versionResults.get(0);
            this.version = extractVersion(firstVersionResult);
        } catch (Exception e) {
            throw new CyderProcessException("Failed to find version for python executable");
        }
    }

    /**
     * Constructs and returns a new PythonVirtualEnvironment from the provided file.
     *
     * @param pythonExecutable the python executable File pointer
     * @return a new PythonVirtualEnvironment
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist, is not a file, or is not executable
     */
    public static PythonVirtualEnvironment from(File pythonExecutable) {
        Preconditions.checkNotNull(pythonExecutable);
        Preconditions.checkArgument(pythonExecutable.exists());
        Preconditions.checkArgument(pythonExecutable.isFile());
        Preconditions.checkArgument(pythonExecutable.canExecute());

        return new PythonVirtualEnvironment(pythonExecutable);
    }

    /**
     * Constructs and returns a new PythonVirtualEnvironment from a File
     * constructed from the provided path.
     *
     * @param path the path to the python executable
     * @return a new PythonVirtualEnvironment
     * @throws NullPointerException     if the provided path is null
     * @throws IllegalArgumentException if the provided path is empty
     */
    public static PythonVirtualEnvironment from(String path) {
        Preconditions.checkNotNull(path);
        Preconditions.checkArgument(!path.trim().isEmpty());

        return new PythonVirtualEnvironment(new File(path));
    }

    /**
     * Installs the provided package requirement and returns whether the installation was successful.
     *
     * @param packageName the name of the package to install
     * @return whether the installation was successful
     * @throws NullPointerException     if the provided package name
     * @throws IllegalArgumentException if the provided package name is empty
     */
    public Future<Boolean> installRequirement(String packageName) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkArgument(!packageName.trim().isEmpty());

        Callable<Boolean> installationRunnable = () -> {
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(ImmutableList.of(
                    pythonExecutable.getAbsolutePath(),
                    "-m",
                    "pip",
                    "install",
                    packageName
            ));
            while (!futureResult.isDone()) Thread.onSpinWait();
            ProcessResult result = futureResult.get();
            ImmutableList<String> outputLines = result.getStandardOutput();

            if (outputLines.isEmpty()) return false;
            if (result.containsErrors()) return false;
            String lastOutputLine = outputLines.get(outputLines.size() - 1);
            if (lastOutputLine.startsWith("Successfully installed")) return true;

            Future<Boolean> present = isPipDependencyPresent(packageName);
            while (!present.isDone()) Thread.onSpinWait();
            return present.get();
        };

        return submitRunnable(installationRunnable, "install \"" + packageName + "\"");
    }

    /**
     * Installs the provided requirements from the requirements.txt file and returns
     * whether the installations were successful.
     *
     * @param requirementsTxt the requirements.txt file
     * @return whether the installations were successful
     */
    public Future<Boolean> installRequirements(File requirementsTxt) {
        Preconditions.checkNotNull(requirementsTxt);
        Preconditions.checkArgument(requirementsTxt.exists());
        Preconditions.checkArgument(requirementsTxt.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(requirementsTxt, ".txt"));

        Callable<Boolean> installationRunnable = () -> {
            Future<ProcessResult> installationResult = ProcessUtil.getProcessOutput(ImmutableList.of(
                    pythonExecutable.getAbsolutePath(),
                    "-m",
                    "pip",
                    "install",
                    "-r",
                    requirementsTxt.getAbsolutePath()
            ));
            while (!installationResult.isDone()) Thread.onSpinWait();
            ProcessResult result = installationResult.get();
            if (result.containsErrors()) return false;

            Future<ProcessResult> futureFreezeResult = ProcessUtil.getProcessOutput(ImmutableList.of(
                    pythonExecutable.getAbsolutePath(),
                    "-m",
                    "pip",
                    "freeze"
            ));
            while (!futureFreezeResult.isDone()) Thread.onSpinWait();
            ProcessResult freezeResult = futureFreezeResult.get();
            if (freezeResult.containsErrors()) return false;
            ImmutableList<String> frozenLines = freezeResult.getStandardOutput();
            ImmutableList<String> installedPackages = frozenLines.stream()
                    .map(line -> line.split("==")[0])
                    .collect(ImmutableList.toImmutableList());

            ImmutableList<String> requiredPackages = FileUtil.getFileLines(requirementsTxt);
            return requiredPackages.stream()
                    .map(line -> line.split("==")[0].trim())
                    .allMatch(installedPackages::contains);
        };
        return submitRunnable(installationRunnable, "Install requirements from \""
                + requirementsTxt.getAbsolutePath() + "\"");
    }

    /**
     * Returns whether the provided Python package is present within this virtual environment.
     *
     * @param packageName the package name
     * @return whether the provided Python package is present within this virtual environment
     * @throws NullPointerException     if the provided package name is null
     * @throws IllegalArgumentException if the provided package name is empty
     */
    public Future<Boolean> isPipDependencyPresent(String packageName) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkArgument(!packageName.trim().isEmpty());

        Callable<Boolean> pipDependencyPresentRunnable = () -> {
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(ImmutableList.of(
                    pythonExecutable.getAbsolutePath(),
                    "-m",
                    "pip",
                    "freeze"
            ));
            while (!futureResult.isDone()) Thread.onSpinWait();
            ProcessResult result = futureResult.get();
            ImmutableList<String> outputLines = result.getStandardOutput();
            if (outputLines.isEmpty()) return false;
            String packageLine = outputLines.stream()
                    .filter(line -> line.matches("^" + packageName + "=="))
                    .collect(Collectors.joining()).trim();
            return packageLine.isEmpty();
        };
        return submitRunnable(pipDependencyPresentRunnable, "is pip dependency present \""
                + packageName + "\"");
    }

    /**
     * Returns the version of the provided package if installed.
     *
     * @param packageName the name of the Python package
     * @return the version of the provided package if installed
     * @throws NullPointerException     if the provided package name is null
     * @throws IllegalArgumentException if the provided package name is empty
     */
    public Future<String> getPackageVersion(String packageName) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkArgument(!packageName.trim().isEmpty());

        Callable<String> packageVersionGetterRunnable = () -> {
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(ImmutableList.of(
                    pythonExecutable.getAbsolutePath(),
                    "-m",
                    "pip",
                    "show",
                    packageName
            ));
            while (!futureResult.isDone()) Thread.onSpinWait();
            ProcessResult result = futureResult.get();
            ImmutableList<String> packageShowOutput = result.getStandardOutput();
            Optional<String> versionString = packageShowOutput.stream()
                    .map(pipShowVersionPattern::matcher)
                    .filter(Matcher::find)
                    .map(matcher -> matcher.group(1))
                    .findFirst();
            return versionString.orElseThrow(
                    () -> new CyderProcessException("Failed to find version for " + packageName));
        };
        return submitRunnable(packageVersionGetterRunnable, "get package version \"" + packageName + "\"");
    }

    /**
     * Executes the provided callable task.
     *
     * @param task      the task to execute
     * @param operation the name of the operation
     * @param <T>       the type returned by the callable task
     * @return the result of the callable
     */
    private <T> Future<T> submitRunnable(Callable<T> task, String operation) {
        CyderThreadFactory factory = new CyderThreadFactory(
                "PythonVirtualEnvironmentExecutorService{"
                        + "pythonExecutable=\"" + pythonExecutable.getAbsolutePath() + "\", "
                        + "operation=\"" + operation + "\""
                        + "}"
        );
        ExecutorService executorService = Executors.newCachedThreadPool(factory);

        try {
            return executorService.submit(task);
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * Returns the Python executable this environment uses.
     *
     * @return the Python executable this environment uses
     */
    public File getPythonExecutable() {
        return pythonExecutable;
    }

    /**
     * Extracts the Python version from the provided version output.
     *
     * @param versionOutput the output of a Python version command
     * @return the Python version
     * @throws CyderProcessException if an exception occurs extracting the version
     */
    private static String extractVersion(String versionOutput) {
        Matcher matcher = pythonVersionPattern.matcher(versionOutput);
        if (matcher.find()) return matcher.group(versionGroup);
        throw new CyderProcessException("Failed to find version for python executable");
    }

    /**
     * Returns a hashcode for this object.
     *
     * @return a hashcode for this object
     */
    @Override
    public int hashCode() {
        int ret = pythonExecutable.hashCode();
        ret = 31 * ret + version.hashCode();
        return ret;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "PythonVirtualEnvironment{"
                + "pythonExecutable=\"" + pythonExecutable.getAbsolutePath() + "\", "
                + "version=\"" + version + "\""
                + "}";
    }

    /**
     * Returns whether the provided object is equal to this.
     *
     * @param o the other object
     * @return whether the provided object is equal to this
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof PythonVirtualEnvironment)) {
            return false;
        }

        PythonVirtualEnvironment other = (PythonVirtualEnvironment) o;
        return other.pythonExecutable.equals(pythonExecutable)
                && other.version.equals(version);
    }
}
