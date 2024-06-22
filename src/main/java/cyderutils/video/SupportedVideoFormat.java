package cyderutils.video;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyderutils.files.FileUtil;

import java.io.File;
import java.util.Arrays;

/**
 * The video formats supported by Cyder.
 */
public enum SupportedVideoFormat {
    /**
     * The MP4 video format (MPEG-4).
     */
    MP4(".mp4", ImmutableList.of(0x66, 0x74, 0x79, 0x70));

    private final String extension;
    private final ImmutableList<Integer> signature;

    SupportedVideoFormat(String extension, ImmutableList<Integer> signature) {
        this.extension = extension;
        this.signature = signature;
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

        return Arrays.stream(values()).anyMatch(supportedVideoFormat -> {
            boolean extensionMatches = FileUtil.getExtension(file).equals(supportedVideoFormat.extension);
            boolean signatureMatches = FileUtil.fileMatchesSignature(file, supportedVideoFormat.signature);
            return extensionMatches && signatureMatches;
        });
    }
}
