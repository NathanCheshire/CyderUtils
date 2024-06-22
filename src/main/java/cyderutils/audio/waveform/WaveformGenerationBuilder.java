package cyderutils.audio.waveform;

import com.google.common.base.Preconditions;
import cyderutils.audio.CyderAudioFile;
import cyderutils.audio.wav.WaveFileException;
import cyderutils.color.CyderColors;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A builder for constructing the parameters of waveform png generation from a {@link cyderutils.audio.CyderAudioFile}.
 */
public final class WaveformGenerationBuilder {
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 80;
    private static final Color DEFAULT_BACKGROUND_COLOR = CyderColors.vanilla;
    private static final Color DEFAULT_BOTTOM_WAVEFORM_COLOR = CyderColors.navy;
    private static final Color DEFAULT_TOP_WAVEFORM_COLOR = CyderColors.navy;
    private static final Color DEFAULT_CENTER_LINE_COLOR = CyderColors.navy;

    /**
     * The audio file this builder is returning a representation of.
     */
    private final CyderAudioFile audioFile;

    /**
     * the width of this waveform builder.
     */
    private int width = DEFAULT_WIDTH;

    /**
     * the height of this waveform builder.
     */
    private int height = DEFAULT_HEIGHT;

    /**
     * The background color of the waveform png.
     */
    private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;

    /**
     * The color of the bottom of the waveform png.
     */
    private Color bottomWaveformColor = DEFAULT_BOTTOM_WAVEFORM_COLOR;

    /**
     * The color of the top of the waveform png.
     */
    private Color topWaveformColor = DEFAULT_TOP_WAVEFORM_COLOR;

    /**
     * The center line color for the waveform png.
     */
    private Color centerLineColor = DEFAULT_CENTER_LINE_COLOR;

    /**
     * Constructs a new WaveformGenerationBuilder object.
     *
     * @param audioFile the CyderAudioFile this builder will return a waveform representing
     * @throws NullPointerException if the provided audio file is null
     */
    public WaveformGenerationBuilder(CyderAudioFile audioFile) {
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
     * Generates and returns a {@link BufferedImage} from this builder.
     *
     * @return a {@link BufferedImage} representing the audio file's waveform
     * @throws WaveFileException if this builder's encapsulated {@link CyderAudioFile} is not or cannot be converted to a wave file
     */
    public BufferedImage generate() {
        return WaveformGenerator.generate(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof WaveformGenerationBuilder)) {
            return false;
        }

        WaveformGenerationBuilder other = (WaveformGenerationBuilder) o;
        return audioFile.equals(other.audioFile)
                && width == other.width
                && height == other.height
                && backgroundColor.equals(other.backgroundColor)
                && bottomWaveformColor.equals(other.bottomWaveformColor)
                && topWaveformColor.equals(other.topWaveformColor)
                && centerLineColor.equals(other.centerLineColor);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
