package cyder.audio.ffmpeg;

/**
 * The FFmpeg command and arguments supported by Cyder.
 */
public enum FfmpegArgument {
    FFMPEG("ffmpeg"),
    LOG_LEVEL("v"),
    PRINT_FORMAT("print_format"),
    SHOW_STREAMS("show_streams"),
    SHOW_ENTRIES("show_entries");

    private final String argumentName;

    FfmpegArgument(String argumentName) {
        this.argumentName = argumentName;
    }

    /**
     * Returns the name for this argument.
     *
     * @return the name for this argument
     */
    public String getArgumentName() {
        return argumentName;
    }

    /**
     * Returns the argument prefixed with a dash.
     *
     * @return the argument prefixed with a dash
     */
    public String getArgument() {
        return "-" + argumentName;
    }
}
