package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.files.FileUtil;

import java.io.File;
import java.util.LinkedList;

/**
 * Utilities related to playing general and system audio.
 */
public enum GeneralAudioPlayer {
    /**
     * The general audio player instance.
     */
    INSTANCE;

    /**
     * The player used to play general audio that may be user terminated.
     */
    private CPlayer generalPlayer;

    /**
     * The list of system players which are currently playing audio.
     */
    private final LinkedList<CPlayer> systemPlayers = new LinkedList<>();

    /**
     * Plays the provided audio file as a general audio file.
     *
     * @param audioFile the audio file to play
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist,
     *                                  is not a file, or is not a supported audio extension
     * @throws IllegalStateException    if the general audio player is already playing a file
     */
    public void playGeneralAudio(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));
        Preconditions.checkState(!generalPlayer.isPlaying());

        generalPlayer = new CPlayer(audioFile);
        generalPlayer.play();
    }

    /**
     * Plays the provided audio file as a system audio file.
     *
     * @param audioFile the audio file to play
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist,
     *                                  is not a file, or is not a supported audio extension
     * @return the constructed system audio player which is now playing the requested audio
     */
    @CanIgnoreReturnValue
    public CPlayer playSystemAudio(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));

        CPlayer newSystemPlayer = new CPlayer(audioFile);
        systemPlayers.add(newSystemPlayer);
        newSystemPlayer.addOnCompletionCallback(() -> systemPlayers.remove(newSystemPlayer));
        newSystemPlayer.play();
        return newSystemPlayer;
    }

    /**
     * Stops the general audio player if playing.
     *
     * @throws IllegalStateException if the general audio player is not playing.
     */
    public void stopGeneralAudio() {
        Preconditions.checkState(!generalPlayer.isPlaying());
        generalPlayer.stopPlaying();
    }

    /**
     * Stops any and all system audio players if playing.
     *
     * @throws IllegalStateException if no system audio is playing
     */
    public void stopSystemAudio() {
        Preconditions.checkState(systemPlayers.size() > 0);
    }

    /**
     * Returns whether any system audio player are playing.
     *
     * @return whether any system audio player are playing
     */
    public boolean isSystemAudioPlaying() {
        return systemPlayers.stream().anyMatch(CPlayer::isPlaying);
    }

    /**
     * Returns whether any system audio players are playing the provided file.
     *
     * @param audioFile the audio file
     * @return whether any system audio players are playing the provided file
     * @throws NullPointerException if the provided audio file is null
     */
    public boolean isSystemAudioPlaying(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        return systemPlayers.stream().anyMatch(player -> player.isUsing(audioFile) && player.isPlaying());
    }

    /**
     * Returns whether the general audio player isp laying.
     *
     * @return whether the general audio player isp laying
     */
    public boolean isGeneralAudioPlaying() {
        return generalPlayer.isPlaying();
    }

    /**
     * Returns whether the general audio player is playing the provided file.
     *
     * @param audioFile the audio file
     * @return whether the general audio player is playing the provided file
     * @throws NullPointerException if the provided file is null
     */
    public boolean isGeneralAudioPlaying(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        return generalPlayer.isUsing(audioFile) && isGeneralAudioPlaying();
    }

    /**
     * Returns whether {@link #isSystemAudioPlaying()} or {@link #isGeneralAudioPlaying()} return true.
     *
     * @return whether {@link #isSystemAudioPlaying()} or {@link #isGeneralAudioPlaying()} return true
     */
    public boolean isAudioPlaying() {
        return isSystemAudioPlaying() || isGeneralAudioPlaying();
    }
}