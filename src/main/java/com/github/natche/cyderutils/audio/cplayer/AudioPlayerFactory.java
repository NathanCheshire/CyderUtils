package com.github.natche.cyderutils.audio.cplayer;

import java.io.BufferedInputStream;

/**
 * A factory for generating {@link AudioPlayer}s from a provided {@link BufferedInputStream}.
 */
public interface AudioPlayerFactory {
    /**
     * Creates a new {@link AudioPlayer} using the provided {@link BufferedInputStream}.
     *
     * @param bis the {@link BufferedInputStream} to use
     * @return a new {@link AudioPlayer} ready to have {@link AudioPlayer#play()} invoked.
     */
    AudioPlayer create(BufferedInputStream bis);
}
