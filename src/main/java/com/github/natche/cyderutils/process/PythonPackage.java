package com.github.natche.cyderutils.process;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.github.natche.cyderutils.annotations.Blocking;
import com.github.natche.cyderutils.snakes.PythonUtil;

import java.util.Optional;
import java.util.concurrent.Future;

// todo this should be dynamic since it seems like a cool API feature to load from a YML

/**
 * Python packages utilized by Cyder.
 */
public enum PythonPackage {
    /**
     * Pillow package for image utilities.
     */
    PILLOW("Pillow"),

    /**
     * Mutagen package for audio metadata parsing.
     */
    MUTAGEN("Mutagen");

    /**
     * The package name for this python package.
     */
    private final String packageName;

    PythonPackage(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Returns the package name for this dependency.
     *
     * @return the package name for this dependency
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Installs this python package using pip if not already present.
     *
     * @return whether the package is installed following completion of the installation request
     */
    @Blocking
    @CanIgnoreReturnValue
    public Future<Boolean> install() {
        Future<ProcessResult> result = PythonUtil.installPipDependency(this);
        while (!result.isDone()) Thread.onSpinWait();
        return isInstalled();
    }

    /**
     * Returns whether the python package is installed.
     *
     * @return whether the python package is installed
     */
    public Future<Boolean> isInstalled() {
        return PythonUtil.isPipDependencyPresent(this);
    }

    /**
     * Returns the installed version of the python package.
     *
     * @return the installed version of the python package
     */
    public Future<Optional<String>> getInstalledVersion() {
        return PythonUtil.getPipDependencyVersion(this);
    }
}
