package cyder.audio.cplayer;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.ForReadability;
import cyder.files.FileUtil;
import cyder.threads.CyderThreadRunner;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An encapsulated JLayer {@link Player} for playing singular audio files.
 */
public final class CPlayer {
    /**
     * The audio file this player will stream/play.
     */
    private final File audioFile;

    /**
     * The JLayer player object.
     */
    private Player player;

    /**
     * The runnables to invoke upon a completion event.
     */
    private final ArrayList<Runnable> onCompletionCallbacks;

    /**
     * Whether this player has been canceled.
     */
    private final AtomicBoolean canceled = new AtomicBoolean();

    /**
     * Whether this player is currently playing audio.
     */
    private final AtomicBoolean playing = new AtomicBoolean();

    /**
     * Constructs a new CPlayer object.
     *
     * @param audioFile the audio file this player will play
     * @throws NullPointerException     if the provided audio file is null
     * @throws IllegalArgumentException if the provided audio file does not exist
     *                                  or is not a file or is not a supported audio file
     */
    public CPlayer(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));

        this.audioFile = audioFile;
        this.onCompletionCallbacks = new ArrayList<>();
    }

    /**
     * Plays the encapsulated audio file.
     * The audio is played in a new thread.
     */
    public void play() {
        Preconditions.checkArgument(!playing.get());

        playing.set(true);
        canceled.set(false);

        CyderThreadRunner.submit(() -> {
            try (BufferedInputStream bis = FileUtil.bisForFile(audioFile)) {
                player = new Player(bis);
                player.play();
                if (!canceled.get()) onCompletionCallbacks.forEach(Runnable::run);
            } catch (JavaLayerException | IOException e) {
               throw new CPlayerException("Failed to play audio file, exception: " + e.getMessage());
            } finally {
                closeResources();
                playing.set(false);
            }
        }, getPlayThreadName());
    }

    /**
     * Returns the thread name for the audio playing thread spawned by {@link #play()}.
     *
     * @return the thread name for the audio playing thread spawned by {@link #play()}
     */
    @ForReadability
    private String getPlayThreadName() {
        return "CPlayer{"
                + "audioFile=\"" + audioFile.getAbsolutePath() + "\""
                + "}";
    }

    /**
     * Cancels this player, the on completion callbacks will not be invoked if present.
     */
    public void cancelPlaying() {
        canceled.set(true);
        closeResources();
    }

    /**
     * Stops the player, the completion callbacks will be invoked if present.
     */
    public void stopPlaying() {
        closeResources();
    }

    /**
     * Closes all resources open by this player.
     */
    private void closeResources() {
        if (player != null) player.close();
        player = null;
    }

    /**
     * Adds the callback to invoke upon a completion event.
     *
     * @param callback the callback to invoke upon a completion event
     * @return this player
     * @throws NullPointerException if the provide callback is null
     */
    @CanIgnoreReturnValue
    public CPlayer addOnCompletionCallback(Runnable callback) {
        Preconditions.checkNotNull(callback);
        onCompletionCallbacks.add(callback);
        return this;
    }

    /**
     * Returns whether this player is currently playing audio.
     *
     * @return whether this player is currently playing audio
     */
    public boolean isPlaying() {
        return playing.get();
    }

    /**
     * Returns the audio file of this player.
     *
     * @return the audio file of this player
     */
    public File getAudioFile() {
        return audioFile;
    }

    /**
     * Returns whether the provided audio file is equal to the encapsulated audio file.
     *
     * @param audioFile the audio file
     * @return whether the provided audio file is equal to the encapsulated audio file
     * @throws NullPointerException if the provided file is null
     */
    public boolean isUsingAudioFile(File audioFile) {
        Preconditions.checkNotNull(audioFile);

        return this.audioFile.equals(audioFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof CPlayer)) {
            return false;
        }

        CPlayer other = (CPlayer) o;
        return audioFile == other.audioFile
                && Objects.equals(other.onCompletionCallbacks, onCompletionCallbacks)
                && playing == other.playing
                && canceled == other.canceled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = audioFile.hashCode();
        ret = 31 * ret + Objects.hashCode(onCompletionCallbacks);
        ret = 31 * ret + Boolean.hashCode(playing.get());
        ret = 31 * ret + Boolean.hashCode(canceled.get());
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "AudioPlayer{"
                + "audioFile=" + audioFile
                + ", player=" + player
                + ", onCompletionCallback=" + onCompletionCallbacks
                + ", canceled=" + canceled
                + ", playing=" + playing
                + "}";
    }
}
