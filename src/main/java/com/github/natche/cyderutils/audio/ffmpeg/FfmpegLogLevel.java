package com.github.natche.cyderutils.audio.ffmpeg;

/** The supported verbosity levels (log levels) for FFmpeg. */
public enum FfmpegLogLevel {
    /** No output. */
    QUIET,

    /** Only show fatal errors which could lead the process to crash. */
    PANIC,

    /**
     * Only show fatal errors. These are errors after which the process
     * absolutely cannot continue.
     */
    FATAL,

    /** Show all errors, including ones that can be recovered from. */
    ERROR,

    /**
     * Show all warnings and errors. Any message related to possibly incorrect
     * or unexpected events will be shown.
     */
    WARNING,

    /** Standard informational messages will be shown. */
    INFO,

    /** Show verbose output, including informational messages. */
    VERBOSE,

    /** Show all informational messages, including debug information. */
    DEBUG,

    /** Show everything, including debugging information. */
    TRACE;

    /**
     * Returns the log level name.
     *
     * @return the log level name
     */
    public String getLogLevelName() {
        return this.name().toLowerCase();
    }
}

