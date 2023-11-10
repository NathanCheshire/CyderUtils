package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.files.FileUtil;
import cyder.threads.CyderThreadRunner;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * An encapsulated JLayer{@link Player} for playing singular audio files and stopping.
 */
public final class CPlayer {
    /**
     * The audio file this player will stream/play.
     */
    private final File audioFile;

    /**
     * The file input stream for the audio file.
     */
    private FileInputStream fis;

    /**
     * The buffered input stream for the FileInputStream.
     */
    private BufferedInputStream bis;

    /**
     * The JLayer player object.
     */
    private Player player;

    /**
     * The runnables to invoke upon a completion event.
     */
    private final ArrayList<Runnable> onCompletionCallback;

    /**
     * Whether this player has been canceled.
     */
    private boolean canceled;

    /**
     * Whether this player is currently playing audio.
     */
    private boolean playing;

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
        this.onCompletionCallback = new ArrayList<>();
    }

    /**
     * Plays the encapsulated audio file.
     */
    public void play() {
        Preconditions.checkArgument(!playing);

        playing = true;
        canceled = false;

        CyderThreadRunner.submit(() -> {
            try {
                fis = new FileInputStream(audioFile);
                bis = new BufferedInputStream(fis);
                player = new Player(bis);
                player.play();
                if (!canceled) onCompletionCallback.forEach(Runnable::run);
            } catch (FileNotFoundException | JavaLayerException e) {
               throw new CPlayerException("Failed to play audio file, exception: " + e.getMessage());
            } finally {
                closeResources();
                playing = false;
            }
        }, audioFile.getAbsolutePath());
    }

    /**
     * Cancels this player, the on completion callbacks will not be invoked if present.
     */
    public void cancelPlaying() {
        canceled = true;
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
        try {
            if (player != null) player.close();
            player = null;
            if (bis != null) bis.close();
            bis = null;
            if (fis != null) fis.close();
            fis = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the callback to invoke upon a completion event.
     *
     * @param callback the callback to invoke upon a completion event
     * @return this player
     */
    @CanIgnoreReturnValue
    public CPlayer addOnCompletionCallback(Runnable callback) {
        Preconditions.checkNotNull(callback);
        onCompletionCallback.add(callback);
        return this;
    }

    /**
     * Returns whether this player is currently playing audio.
     *
     * @return whether this player is currently playing audio
     */
    public boolean isPlaying() {
        return playing;
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
     */
    public boolean isUsing(File audioFile) {
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
                && Objects.equals(other.onCompletionCallback, onCompletionCallback)
                && playing == other.playing
                && canceled == other.canceled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = audioFile.hashCode();
        ret = 31 * ret + Objects.hashCode(onCompletionCallback);
        ret = 31 * ret + Boolean.hashCode(playing);
        ret = 31 * ret + Boolean.hashCode(canceled);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "AudioPlayer{" + "audioFile=" + audioFile
                + ", fis=" + fis
                + ", bis=" + bis
                + ", player=" + player
                + ", onCompletionCallback=" + onCompletionCallback
                + ", canceled=" + canceled
                + ", playing=" + playing
                + "}";
    }
}
