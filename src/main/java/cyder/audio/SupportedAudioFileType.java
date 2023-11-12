package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.files.FileUtil;

import java.io.File;
import java.util.Arrays;

/**
 * Audio file formats supported by Cyder.
 */
public enum SupportedAudioFileType {
    /**
     * The MP3 audio file type.
     */
    MP3(".mp3", ImmutableList.of(0x49, 0x44, 0x33)),

    /**
     * The wave audio file type.
     */
    WAVE(".wav", ImmutableList.of(0x52, 0x49, 0x46, 0x46)),

    /**
     * The OGG audio file type.
     */
    OGG(".ogg", ImmutableList.of(0x4F, 0x67, 0x67, 0x53)),

    /**
     * The M4A audio file type.
     * Note this audio file type has no specific byte signature.
     * To determine validity of a provided {@link File}, see {@link AudioValidationUtil#isValidM4aFile(File)}.
     */
    M4A(".m4a", ImmutableList.of());

    private final String extension;
    private final ImmutableList<Integer> signature;

    SupportedAudioFileType(String extension, ImmutableList<Integer> signature) {
        this.extension = extension;
        this.signature = signature;
    }

    /**
     * Returns this audio file type's extension.
     *
     * @return this audio file type's extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Returns this audio file type's byte signature.
     *
     * @return this audio file type's byte signature
     */
    public ImmutableList<Integer> getSignature() {
        return signature;
    }

    /**
     * Returns whether the provided file is supported.
     *
     * @param audioFile the audio file
     * @return whether the provided file is supported
     * @throws NullPointerException if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist or is not a file
     */
    public static boolean isSupported(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());

        return Arrays.stream(values()).anyMatch(supportedAudioFileType -> {
            boolean extensionMatches = FileUtil.validateExtension(audioFile, supportedAudioFileType.getExtension());
            boolean fileSignatureMatches = FileUtil.fileMatchesSignature(audioFile, supportedAudioFileType.getSignature());
            return extensionMatches && fileSignatureMatches;
        });
    }

    /**
     * Returns whether the provided file is of this type.
     *
     * @param audioFile the audio file
     * @return whether the provided file is of this type
     * @throws NullPointerException if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist or is not a file
     */
    public boolean isAudioTypeofFile(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());

        boolean extensionMatches = FileUtil.validateExtension(audioFile, extension);
        boolean fileSignatureMatches = FileUtil.fileMatchesSignature(audioFile, signature);
        return extensionMatches && fileSignatureMatches;
    }
}