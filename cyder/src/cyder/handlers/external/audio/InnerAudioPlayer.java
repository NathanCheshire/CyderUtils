package cyder.handlers.external.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.FileUtil;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * An inner class for easily playing a single audio file
 */
class InnerAudioPlayer {
    /**
     * The one and only file this audio player can play.
     */
    private final File audioFile;

    /**
     * Constructs a new InnerAudioPlay.
     *
     * @param audioFile the audio file for this object to handle
     */
    public InnerAudioPlayer(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        this.audioFile = audioFile;

        setup();
    }

    /**
     * Whether this object
     */
    private boolean killed;

    /**
     * The location the current audio file was paused/stopped at.
     */
    private long pauseLocation;

    /**
     * The total audio length of the current audio file.
     */
    private long totalAudioLength;

    /**
     * Performs necessary setup actions such as refreshing the title label.
     */
    private void setup() {
        AudioPlayer.refreshAudioTitleLabel();
    }

    /**
     * The audio player used to play audio.
     */
    private Player audioPlayer;

    /**
     * The file input stream used to calculate byte values outside of the play method.
     */
    private FileInputStream fis;

    /**
     * Starts playing the provided audio file at the optionally provided location.
     */
    public void play() {
        try {
            fis = new FileInputStream(audioFile);
            totalAudioLength = fis.available();
            long bytesSkipped = fis.skip(Math.max(0, pauseLocation));

            if (fis.available() != totalAudioLength - bytesSkipped) {
                throw new IllegalStateException("Fis failed to skip requested bytes");
            }

            BufferedInputStream bis = new BufferedInputStream(fis);

            ConsoleFrame.INSTANCE.revalidateAudioMenuVisibility();

            audioPlayer = new Player(bis);

            CyderThreadRunner.submit(() -> {
                try {
                    audioPlayer.play();

                    ConsoleFrame.INSTANCE.revalidateAudioMenuVisibility();

                    FileUtil.closeIfNotNull(fis);
                    FileUtil.closeIfNotNull(bis);
                    audioPlayer = null;

                    if (!killed) {
                        AudioPlayer.playAudioCallback();
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "AudioPlayer Play Audio Thread [" + FileUtil.getFilename(audioFile) + "]");

            AudioPlayer.refreshPlayPauseButtonIcon();
        } catch (Exception ignored) {}
    }

    /**
     * Returns whether this object is playing audio.
     *
     * @return whether this object is playing audio
     */
    public boolean isPlaying() {
        return audioPlayer != null;
    }

    /**
     * Pauses the audio player.
     */
    public void stop() {
        audioPlayer.close();
    }

    /**
     * Returns whether this inner audio player has been killed.
     *
     * @return whether this inner audio player has been killed
     */
    public boolean isKilled() {
        return killed;
    }

    /**
     * The amount to offset a pause request by so that a sequential play
     * request sounds like it was paused at that instant.
     */
    private static final int PAUSE_AUDIO_REACTION_OFFSET = 10000;

    /**
     * Kills the player if playing audio and returns the location to resume a new player object at.
     *
     * @return the location in bytes to resume a new player object at if desired
     */
    @CanIgnoreReturnValue
    public long kill() {
        long resumeLocation = 0L;

        try {
            resumeLocation = totalAudioLength - fis.available() - PAUSE_AUDIO_REACTION_OFFSET;
        } catch (Exception ignored) {}

        this.killed = true;

        if (audioPlayer != null) {
            audioPlayer.close();
        }

        fis = null;

        return resumeLocation;
    }

    /**
     * Returns the total computed audio length (bytes).
     *
     * @return the total computed audio length (bytes)
     */
    public long getTotalAudioLength() {
        return totalAudioLength;
    }

    /**
     * Sets the location this player should start playing at when {@link InnerAudioPlayer#play()} is invoked.
     *
     * @param pauseLocation the location this player should start from
     */
    public void setLocation(long pauseLocation) {
        this.pauseLocation = pauseLocation;
    }

    /**
     * Returns the raw pause location of the exact number of bytes played by fis.
     *
     * @return the raw pause location of the exact number of bytes played by fis
     * @throws IllegalStateException if the raw pause location could not be polled or fis is null
     */
    public long getRemainingBytes() {
        if (fis == null)
            throw new IllegalStateException("Fis is null");

        try {
            return totalAudioLength - fis.available();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new IllegalStateException("Could not poll remaining bytes");
    }

    /**
     * Returns the percent into the current audio this player object is.
     *
     * @return the percent into the current audio this player object is
     */
    public float getPercentIn() {
        float percentIn = 0f;

        try {
            percentIn = (totalAudioLength - fis.available()) / (float) totalAudioLength;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return percentIn;
    }

    /**
     * Returns the milliseconds into the current audio this player object is.
     *
     * @return the milliseconds into the current audio this player object is
     */
    public long getMillisecondsIn() {
        int totalMillis = AudioUtil.getMillisFast(audioFile);
        return (long) (totalMillis * getPercentIn());
    }
}
