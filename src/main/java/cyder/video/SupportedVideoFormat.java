package cyder.video;

import com.google.common.base.Preconditions;
import cyder.files.FileUtil;

import java.io.File;
import java.util.Arrays;

/**
 * The video formats supported by Cyder.
 */
public enum SupportedVideoFormat {
    /**
     * The MP4 video format (MPEG-4).
     */
    MP4(".mp4");

    private final String extension;

    SupportedVideoFormat(String extension) {
        this.extension = extension;
    }

    /**
     * Returns whether the provided file is a supported video file.
     *
     * @param file the file
     * @return whether the provided file is a supported video file
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist or is not a file
     */
    public static boolean isSupportedVideoFormat(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());

        return Arrays.stream(values()).anyMatch(supportedVideoFormat
                -> FileUtil.getExtension(file).equals(supportedVideoFormat.extension));
    }
}
