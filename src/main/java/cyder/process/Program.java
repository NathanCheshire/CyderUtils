package cyder.process;

import cyder.utils.OsUtil;

/**
 * Common external programs/binaries utilized by Cyder.
 */
public enum Program { // todo rename to python programs and then need python package
    PIP("pip", "pip.exe"),
    PYTHON("python", "python.exe");

    /**
     * The name of this program.
     */
    private final String programName;

    /**
     * The filename of this program, extension included even though it's likely an exe.
     */
    private final String filename;

    Program(String programName, String filename) {
        this.programName = programName;
        this.filename = filename;
    }

    /**
     * Returns the name of this program.
     *
     * @return the name of this program
     */
    public String getProgramName() {
        return programName;
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
        return OsUtil.isBinaryInstalled(programName);
    }
}
