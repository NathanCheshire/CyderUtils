package com.github.natche.cyderutils.audio.cplayer;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;

/** The default factory for generating {@link AudioPlayer}s using {@link Player}s. */
public final class DefaultAudioPlayerFactory implements AudioPlayerFactory {
    @Override
    public AudioPlayer create(BufferedInputStream bis) {
        try {
            return new DefaultAudioPlayer(bis);
        } catch (JavaLayerException e) {
            throw new CPlayerException(e);
        }
    }
}
