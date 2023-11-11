package cyder.audio;

import com.google.common.base.Preconditions;
import cyder.files.FileUtil;
import cyder.strings.StringUtil;

import java.io.File;

/**
 * A Cyder wrapper class around a {@link java.io.File} of a supported audio type as defined by
 * {@link CyderAudioFile#isSupportedAudioExtension} for performing certain operations or mutations.
 */
public final class CyderAudioFile {

    /**
     * Returns whether the provided file is a supported audio file by validating
     * the file extension and the file byte signature.
     *
     * @param file the audio file
     * @return whether the provided file is a supported audio file
     * @throws NullPointerException if the provided file is null
     */
    public static boolean isSupportedAudioExtension(File file) {
        Preconditions.checkNotNull(file);

        String name = file.getName();
        if (StringUtil.isNullOrEmpty(name)) return false;

        String extension = FileUtil.getExtension(name);
        if (StringUtil.isNullOrEmpty(extension)) return false;

        return StringUtil.in(extension, true, SUPPORTED_AUDIO_EXTENSIONS)
                && (FileUtil.fileMatchesSignature(file, WAV_SIGNATURE) || fileMatchesSignature(file, MP3_SIGNATURE));
    }
}
