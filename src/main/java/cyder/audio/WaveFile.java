package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import cyder.enumerations.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.strings.CyderStrings;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

/**
 * Wrapper class for a wave (.wav) file.
 * See <a href="http://soundfile.sapp.org/doc/WaveFormat">this link</a>
 * to reference the wav file data structure.
 * <p>
 * Instances of this class are immutable.
 */
@Immutable
public final class WaveFile {
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
     * Suppress default constructor.
     */
    private WaveFile() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Constructs a new WaveFile object.
     *
     * @param file the wave file
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist
     *                                  or is not of the {@link Extension#WAV} extension
     * @throws WaveFileException        if an exception occurs when attempting to set up
     *                                  the streams related to the wave file
     */
    public WaveFile(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.WAV.getExtension()));

        wavFile = file;

        try {
            setupStreams();
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new WaveFileException("Failed to setup wave file: " + e.getMessage());
        }
    }

    /**
     * Sets up the {@link AudioInputStream} and other fields related to this wav file.
     *
     * @throws IOException                   if the constructed {@link AudioInputStream} cannot be read from or closed properly
     * @throws UnsupportedAudioFileException if the File does not point to valid audio file data recognized by the system
     */
    private void setupStreams() throws IOException, UnsupportedAudioFileException {
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
            throw new FatalException("Failed to read bytes from FileInputStream constructed from: " + wavFile);
        }

        audioInputStream.close();

        try {
            clip = AudioSystem.getClip();
            clipStream = AudioSystem.getAudioInputStream(wavFile);
            clip.open(clipStream);
            clip.setFramePosition(0);
            isPlayable = true;
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            // non 16/8-bit audio file
            isPlayable = false;
            clip = null;
        }
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
        Preconditions.checkArgument(samplePoint <= data.length / sampleSize);

        byte[] sampleBytes = new byte[INT_SIZE];

        if (sampleSize >= 0) {
            System.arraycopy(data, samplePoint * sampleSize * numChannels,
                    sampleBytes, 0, sampleSize);
        }

        return ByteBuffer.wrap(sampleBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
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
        clip.start();
    }

    /**
     * Stops the clip of this WAV file if playing.
     *
     * @throws NullPointerException if either the encapsulated clip or clip stream are null
     * @throws IOException if an input or output error occurs when closing the clip stream
     */
    public void stop() throws IOException {
        Preconditions.checkNotNull(clip);
        Preconditions.checkNotNull(clipStream);

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
    public int getDurationTime() {
        return (int) (getNumFrames() / getAudioFormat().getFrameRate());
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
     * Returns the clip for this wav file.
     *
     * @return the clip for this wav file
     */
    public Clip getClip() {
        return clip;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(numChannels);
        ret = ret * 31 + Arrays.hashCode(data);
        ret = ret * 31 + Boolean.hashCode(isPlayable);
        ret = ret * 31 + audioFormat.hashCode();
        ret = ret * 31 + Objects.hashCode(clip);
        ret = ret * 31 + Integer.hashCode(sampleSize);
        ret = ret * 31 + Long.hashCode(numFrames);
        ret = ret * 31 + Integer.hashCode(sampleRate);
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
                + ", audioFormat=" + audioFormat
                + ", clip=" + clip
                + ", sampleSize=" + sampleSize
                + ", numFrames=" + numFrames
                + ", sampleRate=" + sampleRate
                + ", wavFile=" + wavFile
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
                && audioFormat.equals(other.audioFormat)
                && clip.equals(other.clip)
                && numFrames == other.numFrames
                && sampleRate == other.numFrames
                && wavFile.equals(other.wavFile);
    }
}
