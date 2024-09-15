package com.github.natche.cyderutils.process;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.strings.StringUtil;
import com.github.natche.cyderutils.threads.CyderThreadFactory;
import com.github.natche.cyderutils.utils.ArrayUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Utilities related to processes and the Java {@link Process} API. */
public final class ProcessUtil {
    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private ProcessUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the output as a result of the running the provided command using a {@link Process}.
     *
     * @param command the command string array to run
     * @return the process result
     * @throws NullPointerException     if the provided command array is null or contains a null element
     * @throws IllegalArgumentException if the provided command array is empty
     */
    @CanIgnoreReturnValue
    public static Future<ProcessResult> getProcessOutput(String[] command) {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!ArrayUtil.isEmpty(command));
        if (ImmutableList.copyOf(command).stream().anyMatch((cmd) -> cmd == null)) throw new IllegalArgumentException();

        return getProcessOutput(ArrayUtil.toList(command));
    }

    /**
     * Returns the output as a result of the running the provided command using a {@link Process}.
     *
     * @param command the command list
     * @return the process result
     * @throws NullPointerException     if the provided command list is null or contains a null element
     * @throws IllegalArgumentException if the provided command array is empty
     */
    @CanIgnoreReturnValue
    public static Future<ProcessResult> getProcessOutput(List<String> command) {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!command.isEmpty());
        if (command.stream().anyMatch((cmd) -> cmd == null)) throw new IllegalArgumentException();

        return getProcessOutput(StringUtil.joinParts(command, " "));
    }

    /**
     * Returns the output as a result of the running the provided command using a {@link Process}.
     *
     * @param command the command to run
     * @return the process result
     * @throws NullPointerException     if the provided command is null
     * @throws IllegalArgumentException if the provided command is empty
     * @throws CyderProcessException    if an exception occurs when opening/reading from the process' std or error streams
     */
    @CanIgnoreReturnValue
    public static Future<ProcessResult> getProcessOutput(String command) throws CyderProcessException {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!command.trim().isEmpty());

        String threadName = "getProcessOutput, command: " + StringUtil.surroundWithQuotes(command);
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
            ArrayList<String> standardOutput = new ArrayList<>();
            ArrayList<String> errorOutput = new ArrayList<>();

            try {
                Process process = Runtime.getRuntime().exec(command);
                process.getOutputStream().close();

                String outputLine;
                BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((outputLine = outReader.readLine()) != null) standardOutput.add(outputLine);
                outReader.close();

                String errorLine;
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((errorLine = errorReader.readLine()) != null) errorOutput.add(errorLine);
                errorReader.close();
            } catch (IOException e) {
                throw new CyderProcessException(e);
            }

            return ProcessResult.from(standardOutput, errorOutput);
        });
    }

    /**
     * Executes the provided process and returns the standard output.
     * Note that this process is executed on the current thread so callers should invoke this method
     * in a separate thread if blocking is to be avoided.
     *
     * @param builder the process builder to run
     * @return the output
     * @throws NullPointerException     if the provided builder is null
     * @throws IllegalArgumentException if the provided builder's command args list is empty
     * @throws CyderProcessException    if an {@link IOException} occurs when reading from the process's stream(s)
     */
    public static ImmutableList<String> runProcess(ProcessBuilder builder) {
        Preconditions.checkNotNull(builder);
        Preconditions.checkArgument(!builder.command().isEmpty());

        ArrayList<String> ret = new ArrayList<>();

        try {
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) ret.add(line);
        } catch (IOException e) {
            throw new CyderProcessException(e);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Executes the provided processes successively and returns the standard output.
     *
     * @param builders the process builders to run
     * @return the output
     * @throws NullPointerException     if the provided builders list is null or contains a null element
     * @throws IllegalArgumentException if the provided builders list is empty
     */
    public static ImmutableList<String> runProcesses(ImmutableList<ProcessBuilder> builders) {
        Preconditions.checkNotNull(builders);
        Preconditions.checkArgument(builders.stream().noneMatch((builder) -> builder == null));
        Preconditions.checkArgument(!builders.isEmpty());

        ArrayList<String> ret = new ArrayList<>();
        builders.forEach((processBuilder -> ret.addAll(runProcess(processBuilder))));
        return ImmutableList.copyOf(ret);
    }

    /**
     * Runs the provided command using the Java process API and
     * invokes {@link Process#waitFor()} after starting the process.
     * <p>
     * Note, this method will ignore and suppress any {@link InterruptedException}
     * that is thrown from the {@link Process#wait()} invocation.
     *
     * @param command the command to run
     * @throws NullPointerException     if the provided command is null
     * @throws IllegalArgumentException if the provided command is empty or contains only whitespace
     * @throws CyderProcessException    if an {@link IOException} occurs when running the process created
     *                                  from the provide command
     */
    public static void runAndWaitForProcess(String command) {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!command.trim().isEmpty());

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new CyderProcessException(e);
        }
    }
}
