package com.github.natche.cyderutils.audio.cplayer;

import javazoom.jl.decoder.JavaLayerException;

/** An interface used by {@link CPlayer} to accept classes which have some stop/play audio functionality. */
public interface AudioPlayer {
    /**
     * Starts this player.
     *
     * @throws JavaLayerException if an exception occurs
     */
    void play() throws JavaLayerException;

    /** Closes the resources this player is using, stopping the playback. */
    void close();
}
