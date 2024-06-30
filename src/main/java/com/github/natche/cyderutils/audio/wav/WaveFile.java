package com.github.natche.cyderutils.audio.wav;

import com.github.natche.cyderutils.audio.validation.SupportedAudioFileType;
import com.github.natche.cyderutils.enumerations.Extension;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wrapper class for a wave (.wav) file.
 * See <a href="http://soundfile.sapp.org/doc/WaveFormat">here</a>
 * to reference the wav file data structure.
 * <p>
 * Instances of this class are immutable.
 */
@Immutable
public final class WaveFile {
    private static final Logger logger = LoggerFactory.getLogger(WaveFile.class);

    /**
     * The number of bits per sample of a wav file.
     */
    public static final int BITS_PER_SAMPLE = 8;

    /**
     * The number of bytes for an integer primitive.
     */
    public static final int INT_SIZE = 4;

    /**
     * The number of channels of the wav.
     */
    private int numChannels;

    /**
     * The wav byte data.
     */
    private byte[] data;

    /**
     * Whether the file could be decoded and is playable.
     */
    private boolean isPlayable;

    /**
     * The audio format of the wav.
     */
    private AudioFormat audioFormat;

    /**
     * The clip object for the wav.
     */
    private Clip clip;

    /**
     * The stream for the clip.
     */
    private AudioInputStream clipStream;

    /**
     * The sample size of the wav.
     */
    private int sampleSize = AudioSystem.NOT_SPECIFIED;

    /**
     * The number of frames of the wav.
     */
    private long numFrames = AudioSystem.NOT_SPECIFIED;

    /**
     * The sample rate of the wav.
     */
    private int sampleRate = AudioSystem.NOT_SPECIFIED;

    /**
     * The wrapped wav file.
     */
    private final File wavFile;

    /**
     * Whether the clip is playing.
     */
    private final AtomicBoolean clipPlaying = new AtomicBoolean(false);

    /**
     * Suppress default constructor.
     */
    private WaveFile() {
        throw new IllegalMethodException("Invalid constructor, a valid wave file is required");
    }

    /**
     * Constructs a new WaveFile object.
     *
     * @param file the wave file
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist, is not a file,
     *                                  or is not of the {@link Extension#WAV} extension
     * @throws WaveFileException        if an exception occurs when attempting to set up
     *                                  the streams related to the wave file
     */
    public WaveFile(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkArgument(SupportedAudioFileType.WAVE.isOfType(file));

        wavFile = file;

        setupStreams();
    }

    /**
     * Sets up the {@link AudioInputStream} and other fields related to this wav file.
     *
     * @throws WaveFileException if an exception occurs during setup such as failing to
     *                           create an audio input stream or to read from the stream
     */
    private void setupStreams() throws WaveFileException {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);
            audioFormat = audioInputStream.getFormat();
            numFrames = audioInputStream.getFrameLength();

            sampleRate = (int) audioFormat.getSampleRate();
            sampleSize = audioFormat.getSampleSizeInBits() / BITS_PER_SAMPLE;
            numChannels = audioFormat.getChannels();

            long dataLength = numFrames * audioFormat.getSampleSizeInBits() * audioFormat.getChannels() / 8;

            data = new byte[(int) dataLength];
            int bytesRead = audioInputStream.read(data);
            if (bytesRead == -1) {
                throw new IOException("Failed to read bytes from FileInputStream constructed from: " + wavFile);
            }

            audioInputStream.close();
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new WaveFileException(e);
        }

        try {
            clip = AudioSystem.getClip();
            clipStream = AudioSystem.getAudioInputStream(wavFile);
            clip.open(clipStream);
            clip.setFramePosition(0);
            clip.addLineListener(event -> {
                LineEvent.Type type = event.getType();
                if (LineEvent.Type.START.equals(type)) {
                    clipPlaying.set(true);
                } else if (LineEvent.Type.STOP.equals(type) || LineEvent.Type.CLOSE.equals(type)) {
                    clipPlaying.set(false);
                }
            });

            isPlayable = true;
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            logger.info(e, () -> "Error setting up streams");
            // non 16/8-bit audio file
            isPlayable = false;
            clip = null;
        }
    }

    /**
     * Returns the number of channels this wav file contains.
     * If there are two channels, the first sample is the first data point for the first channel,
     * the second sample is the first data point for the second channel, the third sample is the second
     * data point for the first channel, and so on.
     *
     * @return the number of channels this wav file contains
     */
    public int getNumChannels() {
        return numChannels;
    }

    /**
     * Returns the number of samples in this wav file.
     *
     * @return the number of samples in this wav file
     */
    public int getNumSamples() {
        // todo could just save this off
        return data.length / (sampleSize * numChannels);
    }

    /**
     * Returns the amplitude of the wave at the provided sample point.
     * <p>
     * Note that in case of stereos, samples go one after another meaning
     * 0 is the first of the left channel, 1 the first of the right, 2
     * the second of the left, 3 the second of the right, etc.
     *
     * @param samplePoint the point to sample the wave file at
     * @return the amplitude at the sample point
     * @throws IllegalArgumentException if the provided sample point is less than 0 or greater
     *                                  than the data's length divided by the sample size
     */
    public int getSample(int samplePoint) {
        Preconditions.checkArgument(samplePoint >= 0);
        Preconditions.checkArgument(samplePoint < getNumSamples());

        byte[] sampleBytes = new byte[INT_SIZE];

        if (sampleSize >= 0) {
            System.arraycopy(data, samplePoint * sampleSize * numChannels,
                    sampleBytes, 0, sampleSize);
        }

        return ByteBuffer.wrap(sampleBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * Returns the maximum sample this wave file contains.
     *
     * @return the maximum sample this wave file contains
     */
    public int getMaxSample() {
        int ret = Integer.MIN_VALUE;

        for (int i = 0 ; i < getNumSamples() ; i++) {
            ret = Math.max(ret, getSample(i));
        }

        return ret;
    }

    /**
     * Returns the minimum sample this wave file contains.
     *
     * @return the minimum sample this wave file contains
     */
    public int getMinSample() {
        int ret = Integer.MAX_VALUE;

        for (int i = 0 ; i < getNumSamples() ; i++) {
            ret = Math.min(ret, getSample(i));
        }

        return ret;
    }

    /**
     * Returns the average sample this wave file contains.
     *
     * @return the average sample this wave file contains
     */
    public int getAverageSample() {
        long sum = 0;
        int numSamples = getNumSamples();

        for (int i = 0 ; i < numSamples ; i++) {
            sum += getSample(i);
        }

        return (int) (sum / numSamples);
    }

    /**
     * Returns whether this wav file is playable.
     *
     * @return whether this wav file is playable
     */
    public boolean isPlayable() {
        return isPlayable;
    }

    /**
     * Play the clip of this wav file.
     *
     * @throws NullPointerException if the encapsulated clip is null
     */
    public void play() {
        Preconditions.checkNotNull(clip);
        Preconditions.checkState(!clipPlaying.get());
        clip.start();
    }

    /**
     * Stops the clip of this WAV file if playing.
     *
     * @throws NullPointerException if either the encapsulated clip or clip stream are null
     * @throws IOException          if an input or output error occurs when closing the clip stream
     */
    public void stop() throws IOException {
        Preconditions.checkNotNull(clip);
        Preconditions.checkNotNull(clipStream);
        Preconditions.checkState(clipPlaying.get());

        clip.stop();
        clipStream.close();
    }

    /**
     * Returns the format of this wav file.
     *
     * @return the format of this wav file
     */
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    /**
     * Returns the sample rate of this wav file.
     *
     * @return the sample rate of this wav file
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * Returns the duration of this wave file in seconds.
     *
     * @return the duration of this wave file in seconds
     */
    public float getDurationTime() {
        return (getNumFrames() / getAudioFormat().getFrameRate());
    }

    /**
     * Returns the number of frames of the wav file.
     *
     * @return the number of frames of the wav file
     */
    public long getNumFrames() {
        return numFrames;
    }

    /**
     * Returns the sample rate of this wav file.
     *
     * @return the sample rate of this wav file
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(numChannels);
        ret = ret * 31 + Arrays.hashCode(data);
        ret = ret * 31 + Boolean.hashCode(isPlayable);
        ret = ret * 31 + Integer.hashCode(sampleSize);
        ret = ret * 31 + Long.hashCode(numFrames);
        ret = ret * 31 + Integer.hashCode(sampleRate);
        ret = ret * 31 + Boolean.hashCode(clipPlaying.get());
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "WaveFile{"
                + "numChannels=" + numChannels
                + ", dataLength=" + data.length
                + ", isPlayable=" + isPlayable
                + ", sampleSize=" + sampleSize
                + ", numFrames=" + numFrames
                + ", sampleRate=" + sampleRate
                + ", wavFile=" + wavFile
                + ", clipPlaying=" + clipPlaying.get()
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof WaveFile)) {
            return false;
        }

        WaveFile other = (WaveFile) o;

        return numChannels == other.numChannels
                && Arrays.equals(data, other.data)
                && isPlayable == other.isPlayable
                && numFrames == other.numFrames
                && sampleRate == other.sampleRate
                && clipPlaying.get() == other.clipPlaying.get()
                && wavFile.equals(other.wavFile);
    }
}
