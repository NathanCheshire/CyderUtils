package com.github.natche.cyderutils.audio.waveform;

import com.github.natche.cyderutils.annotations.Blocking;
import com.github.natche.cyderutils.audio.CyderAudioFile;
import com.github.natche.cyderutils.audio.validation.SupportedAudioFileType;
import com.github.natche.cyderutils.audio.wav.WaveFile;
import com.github.natche.cyderutils.audio.wav.WaveFileException;
import com.github.natche.cyderutils.color.CyderColors;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.image.CyderImage;
import com.google.common.base.Preconditions;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Future;

/** A class for generating audio waveforms PNGs from audio files. */
public final class WaveformImage {
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
    private WaveformImage() {
        throw new IllegalMethodException("Instances of WaveformImage are not allowed");
    }

    /**
     * Generates and returns a {@link BufferedImage} representing the audio file's waveform.
     * Note this method is blocking meaning the caller should invoke this method inside a separate thread.
     *
     * @param builder the builder for configuring the output image
     * @return a {@link BufferedImage} representing the audio file's waveform
     * @throws NullPointerException if the provided builder is null
     * @throws WaveFileException    if the builder's encapsulated {@link CyderAudioFile}
     *                              is not, or cannot be converted to, a wave file
     */
    @Blocking
    private static BufferedImage generate(WaveformImageBuilder builder) {
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

        for (int i = 0 ; i < normalizedSamples.length ; i++) {
            // Bottom wave portion
            g2d.setColor(builder.getBottomWaveformColor());
            g2d.drawLine(i, height / 2, i, height / 2 + normalizedSamples[i]);

            // Top wave portion
            g2d.setColor(builder.getTopWaveformColor());
            g2d.drawLine(i, height / 2 - normalizedSamples[i], i, height / 2);
        }

        // Draw center line
        g2d.setColor(builder.getCenterLineColor());
        for (int i = 0 ; i < width ; i++) {
            g2d.drawLine(i, height / 2, i, height / 2);
        }

        return waveformImage;
    }

    /** A builder for constructing the parameters of waveform png generation from a {@link CyderAudioFile}. */
    public static final class WaveformImageBuilder {
        private static final int DEFAULT_WIDTH = 800;
        private static final int DEFAULT_HEIGHT = 100;
        private static final Color DEFAULT_BACKGROUND_COLOR = CyderColors.vanilla;
        private static final Color DEFAULT_BOTTOM_WAVEFORM_COLOR = CyderColors.navy;
        private static final Color DEFAULT_TOP_WAVEFORM_COLOR = CyderColors.navy;
        private static final Color DEFAULT_CENTER_LINE_COLOR = CyderColors.navy;

        /** The audio file this builder is returning a representation of. */
        private final CyderAudioFile audioFile;

        /** the width of this waveform builder. */
        private int width = DEFAULT_WIDTH;

        /** the height of this waveform builder. */
        private int height = DEFAULT_HEIGHT;

        /** The background color of the waveform png. */
        private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;

        /** The color of the bottom of the waveform png. */
        private Color bottomWaveformColor = DEFAULT_BOTTOM_WAVEFORM_COLOR;

        /** The color of the top of the waveform png. */
        private Color topWaveformColor = DEFAULT_TOP_WAVEFORM_COLOR;

        /** The center line color for the waveform png. */
        private Color centerLineColor = DEFAULT_CENTER_LINE_COLOR;

        /**
         * Constructs a new WaveformGenerationBuilder object.
         *
         * @param audioFile the CyderAudioFile this builder will return a waveform representing
         * @throws NullPointerException if the provided audio file is null
         */
        public WaveformImageBuilder(CyderAudioFile audioFile) {
            Preconditions.checkNotNull(audioFile);

            this.audioFile = audioFile;
        }

        /**
         * Returns the audio file.
         *
         * @return the audio file
         */
        public CyderAudioFile getAudioFile() {
            return audioFile;
        }

        /**
         * Returns the width of this waveform builder.
         *
         * @return the width of this waveform builder
         */
        public int getWidth() {
            return width;
        }

        /**
         * Sets the width of this waveform builder.
         *
         * @param width the width of this waveform builder
         * @throws IllegalArgumentException if the provided width is less than one
         */
        public void setWidth(int width) {
            Preconditions.checkArgument(width > 0);
            this.width = width;
        }

        /**
         * Returns the height of this waveform builder.
         *
         * @return the height of this waveform builder
         */
        public int getHeight() {
            return height;
        }

        /**
         * Sets the height of this waveform builder.
         *
         * @param height the height of this waveform builder
         * @throws IllegalArgumentException if the provided height is less than one
         */
        public void setHeight(int height) {
            Preconditions.checkArgument(height > 0);
            this.height = height;
        }

        /**
         * Returns the background color of the waveform png.
         *
         * @return the background color of the waveform png
         */
        public Color getBackgroundColor() {
            return backgroundColor;
        }

        /**
         * Sets the background color of the waveform png.
         *
         * @param backgroundColor the background color of the waveform png
         * @throws NullPointerException if the provided color is null
         */
        public void setBackgroundColor(Color backgroundColor) {
            Preconditions.checkNotNull(backgroundColor);
            this.backgroundColor = backgroundColor;
        }

        /**
         * Returns the color of the bottom of the waveform png.
         *
         * @return the color of the bottom of the waveform png
         */
        public Color getBottomWaveformColor() {
            return bottomWaveformColor;
        }

        /**
         * Sets the color of the bottom of the waveform png.
         *
         * @param bottomWaveformColor the color of the bottom of the waveform png
         * @throws NullPointerException if the provided color is null
         */
        public void setBottomWaveformColor(Color bottomWaveformColor) {
            Preconditions.checkNotNull(bottomWaveformColor);
            this.bottomWaveformColor = bottomWaveformColor;
        }

        /**
         * Returns the color of the top of the waveform png.
         *
         * @return the color of the top of the waveform png
         */
        public Color getTopWaveformColor() {
            return topWaveformColor;
        }

        /**
         * Sets the color of the top of the waveform png.
         *
         * @param topWaveformColor the color of the top of the waveform png
         * @throws NullPointerException if the provided color is null
         */
        public void setTopWaveformColor(Color topWaveformColor) {
            Preconditions.checkNotNull(topWaveformColor);
            this.topWaveformColor = topWaveformColor;
        }

        /**
         * Sets the top and bottom waveform color.
         *
         * @param waveformColor the top and bottom waveform color
         * @throws NullPointerException if the provided color is null
         */
        public void setWaveformColor(Color waveformColor) {
            Preconditions.checkNotNull(waveformColor);
            this.bottomWaveformColor = waveformColor;
            this.topWaveformColor = waveformColor;
        }

        /**
         * Returns the center line color for the waveform png.
         *
         * @return the center line color for the waveform png
         */
        public Color getCenterLineColor() {
            return centerLineColor;
        }

        /**
         * Sets the center line color for the waveform png.
         *
         * @param centerLineColor the center line color for the waveform png
         * @throws NullPointerException if the provided color is null
         */
        public void setCenterLineColor(Color centerLineColor) {
            Preconditions.checkNotNull(centerLineColor);
            this.centerLineColor = centerLineColor;
        }

        /**
         * Generates and returns a {@link CyderImage} from this builder.
         *
         * @return a {@link CyderImage} representing the audio file's waveform
         */
        public CyderImage generate() {
            BufferedImage image = WaveformImage.generate(this);
            return CyderImage.fromBufferedImage(image);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof WaveformImageBuilder)) {
                return false;
            }

            WaveformImageBuilder other = (WaveformImageBuilder) o;
            return audioFile.equals(other.audioFile)
                    && width == other.width
                    && height == other.height
                    && backgroundColor.equals(other.backgroundColor)
                    && bottomWaveformColor.equals(other.bottomWaveformColor)
                    && topWaveformColor.equals(other.topWaveformColor)
                    && centerLineColor.equals(other.centerLineColor);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            int ret = audioFile.hashCode();
            ret = 31 * ret + Integer.hashCode(width);
            ret = 31 * ret + Integer.hashCode(height);
            ret = 31 * ret + backgroundColor.hashCode();
            ret = 31 * ret + bottomWaveformColor.hashCode();
            ret = 31 * ret + topWaveformColor.hashCode();
            ret = 31 * ret + centerLineColor.hashCode();
            return ret;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "WaveformGenerationBuilder{"
                    + "audioFile=" + audioFile
                    + ", width=" + width
                    + ", height=" + height
                    + ", backgroundColor=" + backgroundColor
                    + ", bottomWaveformColor=" + bottomWaveformColor
                    + ", topWaveformColor=" + topWaveformColor
                    + ", centerLineColor=" + centerLineColor
                    + "}";
        }
    }
}
