package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.audio.player.AudioPlayer;
import cyder.console.Console;
import cyder.enumerations.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.strings.CyderStrings;
import javazoom.jl.player.Player;

import java.io.File;

/**
 * Utilities related to playing general and system audio.
 */
public final class GeneralAudioPlayer {
    /**
     * Player used to play general audio files that may be user terminated.
     */
    private static CPlayer generalAudioPlayer;

    /**
     * Suppress default constructor.
     */
    private GeneralAudioPlayer() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Plays the requested audio file using the general {@link CPlayer} player
     * which can be terminated by via an API call or the Console audio menu controls.
     *
     * @param file the audio file to play
     */
    public static void playGeneralAudio(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(file));

        stopGeneralAudio();
        Console.INSTANCE.showAudioButton();

        generalAudioPlayer = new CPlayer(file);
        generalAudioPlayer.play();
    }

    /**
     * Plays the requested audio file using the general
     * {@link Player} player which can be terminated by the user.
     *
     * @param file                 the audio file to play
     * @param onCompletionCallback the callback to invoke upon completion of playing the audio file
     */
    public static void playGeneralAudioWithCompletionCallback(File file, Runnable onCompletionCallback) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(file));
        Preconditions.checkNotNull(onCompletionCallback);

        stopGeneralAudio();
        Console.INSTANCE.showAudioButton();

        generalAudioPlayer = new CPlayer(file).setOnCompletionCallback(onCompletionCallback);
        generalAudioPlayer.play();
    }

    /**
     * Returns whether general audio is playing.
     *
     * @return whether general audio is playing
     */
    public static boolean isGeneralAudioPlaying() {
        return generalAudioPlayer != null && generalAudioPlayer.isPlaying();
    }

    /**
     * Returns whether general audio or {@link AudioPlayer} audio is currently playing.
     *
     * @return whether general audio or {@link AudioPlayer} audio is currently playing
     */
    public static boolean generalOrAudioPlayerAudioPlaying() {
        return isGeneralAudioPlaying() || AudioPlayer.isAudioPlaying();
    }

    /**
     * Stops the current general audio if the provided file is playing.
     *
     * @param audioFile the audio file
     * @return whether any audio was stopped.
     */
    @CanIgnoreReturnValue
    public static boolean stopAudio(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(audioFile, Extension.MP3.getExtension()));

        if (generalAudioPlayer != null && audioFile.equals(generalAudioPlayer.getAudioFile())) {
            stopGeneralAudio();
            return true;
        }

        return false;
    }

    /**
     * Plays the requested audio file using a new {@link CPlayer} object
     * (this cannot be stopped util the MPEG is finished).
     *
     * @param file the audio file to play
     */
    public static void playSystemAudio(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(file));

        new CPlayer(file).play();
    }

    /**
     * Stops the audio currently playing. Note that this does not include
     * any system audio or AudioPlayer widget audio.
     */
    public static void stopGeneralAudio() {
        if (generalAudioPlayer != null) generalAudioPlayer.stop();
        Console.INSTANCE.revalidateAudioMenuVisibility();
    }

    /**
     * Stops any and all audio playing either through the audio player or the general player.
     */
    public static void stopAllAudio() {
        if (AudioPlayer.isAudioPlaying()) AudioPlayer.handlePlayPauseButtonClick();
        if (isGeneralAudioPlaying()) stopGeneralAudio();
    }
}