package cyder.audio.validation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.audio.AudioValidationUtil;
import cyder.audio.ffmpeg.FfmpegArgument;
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
    MP3(".mp3",
            ImmutableList.of(0x49, 0x44, 0x33),
            ImmutableList.of(
                    FfmpegArgument.AUDIO_QUALITY.getArgument(),
                    "0",
                    FfmpegArgument.MAP.getArgument(),
                    "a"
            )
    ),

    /**
     * The wave audio file type.
     */
    WAVE(".wav",
            ImmutableList.of(0x52, 0x49, 0x46, 0x46),
            ImmutableList.of(
                    FfmpegArgument.MAP.getArgument(),
                    "a"
            )
    ),

    /**
     * The OGG audio file type.
     */
    OGG(".ogg",
            ImmutableList.of(0x4F, 0x67, 0x67, 0x53),
            ImmutableList.of(FfmpegArgument.AUDIO_CODE_C.getArgument(),
                    "libvorbis",
                    FfmpegArgument.AUDIO_QUALITY.getArgument(),
                    "4",
                    FfmpegArgument.MAP.getArgument(),
                    "a"
            )
    ),

    /**
     * The M4A audio file type.
     * Note this audio file type has no specific byte signature.
     * To determine validity of a provided {@link File}, see {@link AudioValidationUtil#isValidM4aFile(File)}.
     */
    M4A(".m4a", ImmutableList.of(),
            ImmutableList.of(
                    FfmpegArgument.AUDIO_CODE_C.getArgument(),
                    "acc",
                    FfmpegArgument.AUDIO_QUALITY.getArgument(),
                    "100",
                    FfmpegArgument.MAP.getArgument(),
                    "a"
            )
    );

    private final String extension;
    private final ImmutableList<Integer> signature;
    private final ImmutableList<String> conversionArguments;

    SupportedAudioFileType(String extension, ImmutableList<Integer> signature,
                           ImmutableList<String> conversionArguments) {
        this.extension = extension;
        this.signature = signature;
        this.conversionArguments = conversionArguments;
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
     * Returns this file type's conversion argument.
     *
     * @return this file type's conversion argument
     */
    public ImmutableList<String> getConversionArguments() {
        return conversionArguments;
    }

    /**
     * Returns whether the provided file is supported.
     *
     * @param audioFile the audio file
     * @return whether the provided file is supported
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist or is not a file
     */
    public static boolean isSupported(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());

        return Arrays.stream(values()).anyMatch(supportedAudioFileType -> {
            boolean extensionMatches = FileUtil.validateExtension(audioFile, supportedAudioFileType.getExtension());
            boolean fileSignatureMatches =
                    FileUtil.fileMatchesSignature(audioFile, supportedAudioFileType.getSignature());
            return extensionMatches && fileSignatureMatches;
        });
    }

    /**
     * Returns whether the provided file is of this type.
     *
     * @param audioFile the audio file
     * @return whether the provided file is of this type
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist or is not a file
     */
    public boolean isAudioTypeOfFile(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());

        boolean extensionMatches = FileUtil.validateExtension(audioFile, extension);
        boolean fileSignatureMatches = FileUtil.fileMatchesSignature(audioFile, signature);
        return extensionMatches && fileSignatureMatches;
    }
}
