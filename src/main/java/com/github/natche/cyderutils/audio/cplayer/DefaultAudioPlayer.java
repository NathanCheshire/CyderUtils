package com.github.natche.cyderutils.audio.cplayer;

import com.google.common.base.Preconditions;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;

/** The DefaultAudioPlayer used by classes throughout Cyder. */
public final class DefaultAudioPlayer implements AudioPlayer {
    /** The encapsulated player used for audio playback. */
    private final Player player;

    /**
     * Constructs a new DefaultAudioPlayer.
     *
     * @param bis the buffered input stream to read from
     * @throws JavaLayerException if an exception occurs.
     * @throws NullPointerException if the provided BufferedInputStream is null
     */
    public DefaultAudioPlayer(BufferedInputStream bis) throws JavaLayerException {
        Preconditions.checkNotNull(bis);

        this.player = new Player(bis);
    }

    /** {@inheritDoc} */
    @Override
    public void play() throws JavaLayerException {
        player.play();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        player.close();
    }
}
