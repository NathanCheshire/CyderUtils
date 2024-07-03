package com.github.natche.cyderutils.audio;

import com.github.natche.cyderutils.audio.cplayer.CPlayer;
import com.github.natche.cyderutils.audio.validation.SupportedAudioFileType;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A manager class for playing "general" audio as well as "system" audio.
 * Only one general audio can be playing at a time whilst multiple
 * system audio files can be playing at any time.
 */
public final class AudioPlayerManager {
    /** The player used to play general audio that may be user terminated. */
    private CPlayer generalPlayer;

    /** The list of system players which are currently playing audio. */
    private final List<CPlayer> systemPlayers = new ArrayList<>();

    /** Constructs a new AudioPlayerManager. */
    public AudioPlayerManager() {}

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
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));
        Preconditions.checkState(!generalPlayer.isPlaying());

        generalPlayer = new CPlayer(audioFile);
        generalPlayer.play();
    }

    /**
     * Plays the provided audio file as a system audio file.
     *
     * @param audioFile the audio file to play
     * @return the constructed system audio player which is now playing the requested audio
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist,
     *                                  is not a file, or is not a supported audio extension
     */
    @CanIgnoreReturnValue
    public CPlayer playSystemAudio(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

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
        generalPlayer.stopPlaying();
    }

    /**
     * Stops any and all system audio players if playing.
     *
     * @throws IllegalStateException if no system audio is playing
     */
    public void stopSystemAudio() {
        systemPlayers.forEach(CPlayer::stopPlaying);
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
        return systemPlayers.stream().anyMatch(player -> player.isUsingAudioFile(audioFile) && player.isPlaying());
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
        return generalPlayer.isUsingAudioFile(audioFile) && isGeneralAudioPlaying();
    }

    /**
     * Returns whether {@link #isSystemAudioPlaying()} or {@link #isGeneralAudioPlaying()} return true.
     *
     * @return whether {@link #isSystemAudioPlaying()} or {@link #isGeneralAudioPlaying()} return true
     */
    public boolean isAudioPlaying() {
        return isSystemAudioPlaying() || isGeneralAudioPlaying();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = generalPlayer.hashCode();
        ret = 31 * ret + systemPlayers.hashCode();
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "AudioPlayerManager{"
                + "generalPlayer=" + generalPlayer + ", "
                + "systemPlayers=" + systemPlayers
                + "}";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof AudioPlayerManager)) {
            return false;
        }

        AudioPlayerManager other = (AudioPlayerManager) o;
        return generalPlayer.equals(other.generalPlayer)
                && systemPlayers.equals(other.systemPlayers);
    }
}