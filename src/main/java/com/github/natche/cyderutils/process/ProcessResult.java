package com.github.natche.cyderutils.process;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

/**
 * The result of a {@link Process} run. Commonly used throughout Cyder
 * and returned by classes such as {@link ProcessUtil}.
 */
public final class ProcessResult {
    /** The standard output of the process. */
    private final ImmutableList<String> standardOutput;

    /** The error output of the process. */
    private final ImmutableList<String> errorOutput;

    /**
     * Constructs and returns a new process result.
     *
     * @param standardOutput the standard output
     * @throws NullPointerException if the provided collections is null
     */
    public static ProcessResult fromStandardOutput(Collection<String> standardOutput) {
        Preconditions.checkNotNull(standardOutput);

        return new ProcessResult(standardOutput, ImmutableList.of());
    }

    /**
     * Constructs and returns a new process result.
     *
     * @param errorOutput the error output
     * @throws NullPointerException if the provided collection is null
     */
    public static ProcessResult fromErrorOutput(Collection<String> errorOutput) {
        Preconditions.checkNotNull(errorOutput);

        return new ProcessResult(ImmutableList.of(), errorOutput);
    }

    /**
     * Constructs and returns a new process result.
     *
     * @param standardOutput the standard output
     * @param errorOutput    the error output
     * @throws NullPointerException if either of the provided collections is null
     */
    public static ProcessResult from(Collection<String> standardOutput, Collection<String> errorOutput) {
        Preconditions.checkNotNull(standardOutput);
        Preconditions.checkNotNull(errorOutput);

        return new ProcessResult(standardOutput, errorOutput);
    }

    private ProcessResult(Collection<String> standardOutput, Collection<String> errorOutput) {
        Preconditions.checkNotNull(standardOutput);
        Preconditions.checkNotNull(errorOutput);

        this.standardOutput = ImmutableList.copyOf(standardOutput);
        this.errorOutput = ImmutableList.copyOf(errorOutput);
    }

    /**
     * Returns the standard output of the process.
     *
     * @return the standard output of the process
     */
    public ImmutableList<String> getStandardOutput() {
        return standardOutput;
    }

    /**
     * Returns the error output of the process.
     *
     * @return the error output of the process
     */
    public ImmutableList<String> getErrorOutput() {
        return errorOutput;
    }

    /**
     * Returns whether the error output contains strings.
     *
     * @return whether the error output contains strings
     */
    public boolean containsErrors() {
        return !errorOutput.isEmpty();
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
        } else if (!(o instanceof ProcessResult)) {
            return false;
        }

        ProcessResult other = (ProcessResult) o;

        return other.standardOutput.equals(standardOutput)
                && other.errorOutput.equals(errorOutput);
    }

    /**
     * Returns a hashcode representation of this object.
     *
     * @return a hashcode representation of this object
     */
    @Override
    public int hashCode() {
        int ret = standardOutput.hashCode();
        ret = 31 * ret + errorOutput.hashCode();
        return ret;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "ProcessResult{"
                + "standardOutput=" + standardOutput
                + ", errorOutput=" + errorOutput
                + "}";
    }
}
