package com.github.natche.cyderutils.audio.waveform;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.annotations.Blocking;
import com.github.natche.cyderutils.audio.CyderAudioFile;
import com.github.natche.cyderutils.audio.validation.SupportedAudioFileType;
import com.github.natche.cyderutils.audio.wav.WaveFile;
import com.github.natche.cyderutils.audio.wav.WaveFileException;
import com.github.natche.cyderutils.enumerations.Extension;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

/** A utility class for generating audio waveforms PNGs using {@link WaveformGenerationBuilder}s. */
public final class WaveformGenerator {
    /**
     * The number denoting a value should be interpolated.
     * This could be any negative value, but I have elected to choose -69 for the memes.
     */
    private static final int INTERPOLATION_REQUIRED = -69;

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private WaveformGenerator() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Generates and returns a {@link BufferedImage} representing the audio file's waveform.
     * Note this method is blocking meaning the caller should invoke this method inside a separate thread.
     *
     * @param builder the builder for configuring the output image
     * @return a {@link BufferedImage} representing the audio file's waveform
     * @throws NullPointerException if the provided builder is null
     * @throws WaveFileException if the builder's encapsulated {@link CyderAudioFile} is not or cannot be converted to a wave file
     */
    @Blocking
    public static BufferedImage generate(WaveformGenerationBuilder builder) {
        Preconditions.checkNotNull(builder);

        int width = builder.getWidth();
        int height = builder.getHeight();

        BufferedImage waveformImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = waveformImage.createGraphics();

        CyderAudioFile audioFile = builder.getAudioFile();
        Future<CyderAudioFile> futureConvertedToWav = audioFile.convertTo(SupportedAudioFileType.WAVE);

        while (!futureConvertedToWav.isDone()) Thread.onSpinWait();
        CyderAudioFile convertedToWav;
        try {
            convertedToWav = futureConvertedToWav.get();
        } catch (Exception e) {
            throw new WaveFileException("Failed to convert audio file to wav");
        }

        WaveFile wav = convertedToWav.toWaveFile();

        int numFrames = (int) wav.getNumFrames();
        if (numFrames < width) width = numFrames;
        int[] nonNormalizedSamples = new int[width];

        int sampleLocationIncrement = (int) Math.ceil(numFrames / (double) width);

        int maxAmplitude = 0;
        int nonNormalizedSamplesIndex = 0;
        for (int i = 0 ; i < numFrames ; i += sampleLocationIncrement) {
            maxAmplitude = Math.max(maxAmplitude, wav.getSample(i));

            nonNormalizedSamples[nonNormalizedSamplesIndex] = wav.getSample(i);
            nonNormalizedSamplesIndex++;
        }

        g2d.setPaint(builder.getBackgroundColor());
        g2d.fillRect(0, 0, width, height);

        int[] normalizedSamples = new int[width];
        for (int i = 0 ; i < nonNormalizedSamples.length ; i++) {
            int normalizedValue = (int) ((nonNormalizedSamples[i] / (double) maxAmplitude) * height);
            if (normalizedValue > height / 2) normalizedValue = INTERPOLATION_REQUIRED;
            normalizedSamples[i] = normalizedValue;
        }

        // Loop through samples and interpolate where needed
        for (int i = 0 ; i < normalizedSamples.length ; i++) {
            int currentSample = normalizedSamples[i];
            if (currentSample != INTERPOLATION_REQUIRED) continue;

            int nextValue = 0;
            for (int j = i ; j < normalizedSamples.length ; j++) {
                int currentNextValue = normalizedSamples[j];
                if (currentNextValue != INTERPOLATION_REQUIRED) {
                    nextValue = currentNextValue;
                    break;
                }
            }

            // Last value that isn't an interpolation value
            int lastValue = 0;
            for (int j = i ; j >= 0 ; j--) {
                int currentLastValue = normalizedSamples[j];
                if (currentLastValue != INTERPOLATION_REQUIRED) {
                    lastValue = currentLastValue;
                    break;
                }
            }

            normalizedSamples[i] = (nextValue + lastValue) / 2;
        }

        // Draw center line
        g2d.setColor(builder.getCenterLineColor());
        for (int i = 0 ; i < width ; i++) {
            g2d.drawLine(i, height / 2, i, height / 2);
        }

        for (int i = 0 ; i < normalizedSamples.length ; i++) {
            // Bottom wave portion
            g2d.setColor(builder.getBottomWaveformColor());
            g2d.drawLine(i, height / 2, i, height / 2 + normalizedSamples[i]);

            // Top wave portion
            g2d.setColor(builder.getTopWaveformColor());
            g2d.drawLine(i, height / 2 - normalizedSamples[i], i, height / 2);
        }

        try {
            wav.stop();
        } catch(IOException ignored) {}

        return waveformImage;
    }
}
