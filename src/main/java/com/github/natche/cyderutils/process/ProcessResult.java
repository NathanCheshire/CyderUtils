package com.github.natche.cyderutils.process;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;

import java.util.Collection;

/** The result of a {@link ProcessUtil#getProcessOutput(String) invocation}. */
public class ProcessResult {
    /** The standard output of the process. */
    private final ImmutableList<String> standardOutput;

    /** The error output of the process. */
    private final ImmutableList<String> errorOutput;

    /** Suppress default constructor. */
    private ProcessResult() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Constructs a new process result.
     *
     * @param standardOutput the standard output
     * @param errorOutput    the error output
     * @throws NullPointerException if either of the provided collections is null
     */
    public ProcessResult(Collection<String> standardOutput, Collection<String> errorOutput) {
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
    public boolean hasErrors() {
        return !errorOutput.isEmpty();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = standardOutput.hashCode();
        ret = 31 * ret + errorOutput.hashCode();
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ProcessResult{"
                + "standardOutput=" + standardOutput
                + ", errorOutput=" + errorOutput
                + "}";
    }
}
