package com.github.natche.cyderutils.audio.ffmpeg;

/**
 * The supported print formats for FFmpeg.
 */
public enum FfmpegPrintFormat {
    /**
     * Output format in JavaScript Object Notation (JSON).
     */
    JSON,

    /**
     * Output format in Extensible Markup Language (XML).
     */
    XML,

    /**
     * Output format in INI file format (simple key-value pair format).
     */
    INI,

    /**
     * Output format in Comma-Separated Values (CSV).
     */
    CSV,

    /**
     * Output format in a flat structure, suitable for simple parsing.
     */
    FLAT;

    /**
     * Returns the print format name.
     *
     * @return the print format name
     */
    public String getFormatName() {
        return this.name().toLowerCase();
    }
}
