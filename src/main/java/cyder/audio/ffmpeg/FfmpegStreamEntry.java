package cyder.audio.ffmpeg;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * The supported stream entry values by FFmpeg.
 */
public enum FfmpegStreamEntry {
    DURATION,
    CODEC_NAME,
    CODEC_TYPE,
    WIDTH,
    HEIGHT;

    /**
     * Returns the name of this stream entry.
     *
     * @return the name of this stream entry
     */
    public String getStreamEntryName() {
        return this.name().toLowerCase();
    }

    /**
     * Returns a "stream=" FFmpeg command using this entry.
     *
     * @return a "stream=" FFmpeg command using this entry
     */
    public String getStreamCommand() {
        return of(this);
    }

    /**
     * Returns a "stream=" FFmpeg command using the provided entry.
     * For example, providing {@link #DURATION} will return "stream=duration".
     *
     * @param entry the entry for this stream command
     * @return an FFmpeg stream command for the provided entry
     * @throws NullPointerException if the entry is null
     */
    public static String of(FfmpegStreamEntry entry) {
        Preconditions.checkNotNull(entry);

        return "stream=" + entry.getStreamEntryName();
    }

    /**
     * Returns a "stream=" FFmpeg command using the provided entries.
     * For example, providing {@link #DURATION} will return "stream=duration".
     *
     * @param entries the entries for this stream command
     * @return an FFmpeg stream command for the provided entries
     * @throws NullPointerException if the provided collection is null
     * @throws IllegalArgumentException if the provided collection is empty
     */
    public static String of(Collection<FfmpegStreamEntry> entries) {
        Preconditions.checkNotNull(entries);
        Preconditions.checkArgument(!entries.isEmpty());

        return "stream=" + entries.stream()
                .map(FfmpegStreamEntry::getStreamEntryName)
                .collect(Collectors.joining(","));
    }
}
