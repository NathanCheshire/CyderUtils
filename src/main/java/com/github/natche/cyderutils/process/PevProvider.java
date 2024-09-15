package com.github.natche.cyderutils.process;

import com.google.common.base.Preconditions;

import java.io.File;

/** The standard {@link PythonVirtualEnvironment} provider for use throughout Cyder. */
public enum PevProvider {
    /** The {@link PythonVirtualEnvironment} provider instance. */
    INSTANCE;

    /**
     * The environment variable used to register the default {@link PythonVirtualEnvironment}
     * used by Cyder. This environment variable must point to a valid executable for this
     * provider to not throw when {@link #getProvider()} is invoked.
     */
    public static final String CYDER_PYTHON_EXECUTABLE_PATH = "CYDER_PYTHON_EXECUTABLE_PATH";

    /** The encapsulated provider instance. */
    private final PythonVirtualEnvironment provider;

    PevProvider() {
        String cyderPythonExecutablePath = System.getenv(CYDER_PYTHON_EXECUTABLE_PATH);
        File file = new File(cyderPythonExecutablePath);

        PythonVirtualEnvironment localProvider;

        try {
            localProvider = PythonVirtualEnvironment.from(file);
        } catch (Exception ignored) {
            localProvider = null;
        }

        this.provider = localProvider;
    }

    /**
     * Returns the provider instance.
     * This object is created at runtime via the environment variable pointed to be
     * {@link #CYDER_PYTHON_EXECUTABLE_PATH}. This method will throw if this variable is not
     * present in the system or points to an invalid binary/executable.
     *
     * @return the provider instance
     * @throws CyderProcessException if the provider is not available
     */
    public PythonVirtualEnvironment getProvider() {
        Preconditions.checkState(provider != null);
        return provider;
    }
}
