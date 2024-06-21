package cyder.process;

import com.google.common.collect.ImmutableList;
import cyder.threads.CyderThreadFactory;
import cyder.utils.OsUtil;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Common Python programs/binaries utilized by Cyder.
 */
public enum PythonProgram {
    PIP(ImmutableList.of("pip"), "pip.exe"),
    PYTHON(ImmutableList.of("python", "python3"), "python.exe");

    /**
     * The name of this program.
     */
    private final ImmutableList<String> programNames;

    /**
     * The filename of this program, extension included even though it's likely an exe.
     */
    private final String filename;

    PythonProgram(ImmutableList<String> programNames, String filename) {
        this.programNames = programNames;
        this.filename = filename;
    }

    /**
     * Returns the names of this program.
     *
     * @return the names of this program
     */
    public ImmutableList<String> getProgramNames() {
        return programNames;
    }

    /**
     * Returns the first program name.
     *
     * @return the first program name
     */
    public String getFirstProgramName() {
        return programNames.get(0);
    }

    /**
     * Returns the filename for this program. For example, ffmpeg would return "ffmpeg.exe"
     *
     * @return the filename for this program
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Returns whether this program is installed.
     *
     * @return whether this program is installed
     */
    public boolean isInstalled() {
        return programNames.stream().anyMatch(OsUtil::isBinaryInstalled);
    }

    /**
     * Returns the first working command of this program.
     *
     * @return the first working command of this program, empty optional else
     */
    public Future<Optional<String>> getFirstWorkingProgramName() {
        String threadName = "PythonProgram." + this + "getFirstWorkingProgramName thread";
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
            for (String programName : programNames) {
                boolean isInstalled = OsUtil.isBinaryInstalled(programName);
                if (isInstalled) return Optional.of(programName);
            }

            return Optional.empty();
        });
    }
}
