package com.github.natche.cyderutils.audio.cplayer;

import com.github.natche.cyderutils.annotations.ForReadability;
import com.github.natche.cyderutils.audio.validation.SupportedAudioFileType;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.files.FileUtil;
import com.github.natche.cyderutils.structures.CyderRunnable;
import com.github.natche.cyderutils.threads.CyderThreadRunner;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An encapsulated JLayer {@link Player} for playing singular audio files.
 */
public final class CPlayer {
    /**
     * The default audio player object used internally by this class.
     * This is required since {@link Player} does not directly implement {@link AudioPlayer}
     * due to the way Java interfaces resolve.
     */
    private static final class DefaultAudioPlayer implements AudioPlayer {
        /**
         * The encapsulated player used for audio playback.
         */
        private final Player player;

        /**
         * Constructs a new DefaultAudioPlayer.
         *
         * @param bis the buffered input stream to read from
         * @throws JavaLayerException if an exception occurs.
         */
        public DefaultAudioPlayer(BufferedInputStream bis) throws JavaLayerException {
            this.player = new Player(bis);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void play() throws JavaLayerException {
            player.play();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() {
            player.close();
        }
    }

    /**
     * The default factory for generating {@link AudioPlayer}s using {@link Player}s.
     */
    private static final class DefaultAudioPlayerFactory implements AudioPlayerFactory {
        @Override
        public AudioPlayer create(BufferedInputStream bis) {
            try {
                return new DefaultAudioPlayer(bis);
            } catch (JavaLayerException e) {
                throw new CPlayerException(e);
            }
        }
    }

    /**
     * The audio file this player will stream/play.
     */
    private final File audioFile;

    /**
     * The factory which generates a new {@link AudioPlayer} when required.
     */
    private AudioPlayerFactory playerFactory = new DefaultAudioPlayerFactory();

    /**
     * The AudioPlayer object usually a {@link Player}.
     */
    private AudioPlayer player;

    /**
     * The runnables to invoke upon a completion event.
     */
    private final ArrayList<CyderRunnable> onCompletionCallbacks;

    /**
     * Whether this player has been canceled.
     */
    private final AtomicBoolean canceled = new AtomicBoolean();

    /**
     * Whether this player is currently playing audio.
     */
    private final AtomicBoolean playing = new AtomicBoolean();

    /**
     * Whether this player was requested to stop playing.
     * A request to stop playing is immediately set, whereas {@link #playing} might remain true
     * for a period following the invocation of {@link #stopPlaying()} or {@link #cancelPlaying()}.
     * Thus, this reflects the internal intent of the future proceedings of the audio.
     */
    private final AtomicBoolean requestedToStopPlaying = new AtomicBoolean();

    /**
     * Suppress default constructor.
     *
     * @throws IllegalArgumentException if invoked
     */
    private CPlayer() {
        throw new IllegalMethodException("Invalid constructor; required audio file");
    }

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
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

        this.audioFile = audioFile;
        this.onCompletionCallbacks = new ArrayList<>();
    }

    /**
     * Sets the factory this instance will use to generate a new {@link AudioPlayer}.
     *
     * @param factory the factory
     * @throws NullPointerException if the provided factory is null
     */
    public void setAudioPlayerFactory(AudioPlayerFactory factory) {
        Preconditions.checkNotNull(factory);
        this.playerFactory = factory;
    }

    /**
     * Plays the encapsulated audio file.
     * The audio is played in a new thread.
     */
    public void play() {
        Preconditions.checkState(!playing.get());

        playing.set(true);
        canceled.set(false);

        CyderThreadRunner.submit(() -> {
            try (BufferedInputStream bis = FileUtil.bisForFile(audioFile)) {
                player = playerFactory.create(bis);

                // Edge case of cancel or stop called before this thread started playing
                if (!canceled.get() && !requestedToStopPlaying.get()) {
                    player.play();
                }
                playing.set(false);
                requestedToStopPlaying.set(false);

                if (!canceled.get()) {
                    onCompletionCallbacks.forEach(CyderRunnable::run);
                }
            } catch (JavaLayerException | IOException e) {
                throw new CPlayerException("Failed to play audio file, exception: " + e.getMessage());
            } finally {
                closeResources();
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
        return "CPlayer.play{"
                + "audioFile=\"" + audioFile.getAbsolutePath() + "\""
                + "}";
    }

    /**
     * Cancels this player, the on completion callbacks will not be invoked if present.
     */
    public void cancelPlaying() {
        canceled.set(true);
        requestedToStopPlaying.set(true);
        closeResources();
    }

    /**
     * Returns whether this CPlayer has been canceled.
     *
     * @return whether this CPlayer has been canceled
     */
    public boolean isCanceled() {
        return canceled.get();
    }

    /**
     * Stops the player, the completion callbacks will be invoked if present.
     */
    public void stopPlaying() {
        requestedToStopPlaying.set(true);
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
     * Note, a completion is a play invocation finishing or a call to {@link #stopPlaying()}.
     * Invoking {@link #cancelPlaying()} will not trigger completion callbacks
     *
     * @param callback the callback to invoke upon a completion event
     * @return this player
     * @throws NullPointerException if the provide callback is null
     */
    @CanIgnoreReturnValue
    public CPlayer addOnCompletionCallback(CyderRunnable callback) {
        Preconditions.checkNotNull(callback);
        onCompletionCallbacks.add(callback);
        return this;
    }

    /**
     * Returns the number of on-completion callbacks.
     *
     * @return the number of on-completion callbacks
     */
    public int getOnCompletionCallbackLength() {
        return onCompletionCallbacks.size();
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
        return audioFile.equals(other.audioFile)
                && Objects.equals(player, other.player)
                && other.onCompletionCallbacks.size() == onCompletionCallbacks.size()
                && playing.get() == other.playing.get()
                && canceled.get() == other.canceled.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = audioFile.hashCode();
        ret = 31 * ret + Objects.hashCode(player);
        ret = 31 * ret + Integer.hashCode(onCompletionCallbacks.size());
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
                + ", onCompletionCallbacks=" + onCompletionCallbacks
                + ", canceled=" + canceled
                + ", playing=" + playing
                + "}";
    }
}
