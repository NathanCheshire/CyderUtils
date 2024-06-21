package cyder.audio;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DetermineAudioLengthMethod}s.
 */
class DetermineAudioLengthMethodTest {
    /**
     * Creates a new instance of this class for testing purposes.
     */
    DetermineAudioLengthMethodTest() {}

    /**
     * Tests for the determineAudioLength method when provided a null file.
     */
    @Test
    void testDetermineAudioLengthThrowsForNullFile() {
        assertThrows(NullPointerException.class,
                () -> DetermineAudioLengthMethod.FFMPEG.determineAudioLength(null));
        assertThrows(NullPointerException.class,
                () -> DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(null));
    }

    /**
     * Tests for the determineAudioLength method when provided a directory file.
     */
    @Test
    void testDetermineAudioLengthThrowsForDirectoryFile() {
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.FFMPEG.determineAudioLength(new File(".")));
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(new File(".")));
    }

    /**
     * Tests for the determineAudioLength method when provided a non-existent file.
     */
    @Test
    void testDetermineAudioLengthThrowsForNonExistentFile() {
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.FFMPEG.determineAudioLength(new File("some_file.mp3")));
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(new File("some_file.mp3")));
    }

    /**
     * Tests for the determineAudioLength method when provided an unsupported file.
     */
    @Test
    void testDetermineAudioLengthThrowsForUnsupportedFile() {
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.FFMPEG.determineAudioLength(new File("some_file.acc")));
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(new File("some_file.acc")));
    }

    /**
     * Tests for the determineAudioLength method works for a mp3 file.
     */
    @Test
    void testDetermineAudioLengthWorksForMp3() {
        Future<Duration> futureDuration = DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(
                new File("src/test/java/cyder/audio/TastyCarrots.mp3"));
        assertDoesNotThrow(() -> futureDuration.get());

        try {
            Duration duration = futureDuration.get();
            assertEquals(176.0, duration.getSeconds());
            assertEquals(4.57E8, duration.getNano());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests for the determineAudioLength method works for wav file.
     */
    @Test
    void testDetermineAudioLengthWorksForWav() {
        Future<Duration> futureDuration = DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(
                new File("src/test/java/cyder/audio/TastyCarrots.wav"));
        assertDoesNotThrow(() -> futureDuration.get());

        try {
            Duration duration = futureDuration.get();
            assertEquals(176.0, duration.getSeconds());
            assertEquals(4.25E8, duration.getNano());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // todo tests for other supported audio file formats
}
