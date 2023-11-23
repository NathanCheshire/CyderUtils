package cyder.audio.ffmpeg;

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

    public String getArgumentName() {
        return argumentName;
    }

    public String getArgument() {
        return "-" + argumentName;
    }
}
