package com.github.natche.cyderutils.audio;

import com.github.natche.cyderutils.utils.OsUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for {@link DetermineAudioLengthMethod}s. */
class DetermineAudioLengthMethodTest {
    /** Creates a new instance of this class for testing purposes. */
    DetermineAudioLengthMethodTest() {}

    /** Tests for the determineAudioLength method when provided a null file. */
    @Test
    void testDetermineAudioLengthThrowsForNullFile() {
        assertThrows(NullPointerException.class,
                () -> DetermineAudioLengthMethod.FFMPEG.determineAudioLength(null));
        assertThrows(NullPointerException.class,
                () -> DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(null));
    }

    /** Tests for the determineAudioLength method when provided a directory file. */
    @Test
    void testDetermineAudioLengthThrowsForDirectoryFile() {
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.FFMPEG.determineAudioLength(new File(".")));
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(new File(".")));
    }

    /** Tests for the determineAudioLength method when provided a non-existent file. */
    @Test
    void testDetermineAudioLengthThrowsForNonExistentFile() {
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.FFMPEG.determineAudioLength(new File("some_file.mp3")));
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(new File("some_file.mp3")));
    }

    /** Tests for the determineAudioLength method when provided an unsupported file. */
    @Test
    void testDetermineAudioLengthThrowsForUnsupportedFile() {
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.FFMPEG.determineAudioLength(new File("some_file.acc")));
        assertThrows(IllegalArgumentException.class,
                () -> DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(new File("some_file.acc")));
    }

    /** Tests for the determineAudioLength method works for a mp3 file using Python Mutagen. */
    @Test
    void testDetermineAudioLengthWorksForMp3UsingMutagen() {
        Future<Duration> futureDuration = DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(
                OsUtil.buildFile("src", "test", "kotlin", "com", "github", "natche", "cyderutils", "audio",
                        "resources", "TastyCarrots.mp3"));
        assertDoesNotThrow(() -> futureDuration.get());

        try {
            Duration duration = futureDuration.get();
            assertEquals(10.0, duration.getSeconds());
            assertEquals(3.1E7, duration.getNano());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Tests for the determineAudioLength method works for a mp3 file using FFmpeg. */
    @Test
    void testDetermineAudioLengthWorksForMp3UsingFfmpeg() {
        Future<Duration> futureDuration = DetermineAudioLengthMethod.FFMPEG.determineAudioLength(
                OsUtil.buildFile("src", "test", "kotlin", "com", "github", "natche", "cyderutils", "audio",
                        "resources", "TastyCarrots.mp3"));
        assertDoesNotThrow(() -> futureDuration.get());

        try {
            Duration duration = futureDuration.get();
            assertEquals(10.0, duration.getSeconds());
            assertEquals(3.1E7, duration.getNano());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Tests for the determineAudioLength method works for a wav file using Python Mutagen. */
    @Test
    void testDetermineAudioLengthWorksForWavUsingMutagen() {
        Future<Duration> futureDuration = DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(
                OsUtil.buildFile("src", "test", "kotlin", "com", "github", "natche", "cyderutils", "audio",
                        "resources", "TastyCarrots.wav"));
        assertDoesNotThrow(() -> futureDuration.get());

        try {
            Duration duration = futureDuration.get();
            assertEquals(10.0, duration.getSeconds());
            assertEquals(8000000.0, duration.getNano());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Tests for the determineAudioLength method works for a wav file using FFmpeg. */
    @Test
    void testDetermineAudioLengthWorksForWavUsingFfmpeg() {
        Future<Duration> futureDuration = DetermineAudioLengthMethod.FFMPEG.determineAudioLength(
                OsUtil.buildFile("src", "test", "kotlin", "com", "github", "natche", "cyderutils", "audio",
                        "resources", "TastyCarrots.wav"));
        assertDoesNotThrow(() -> futureDuration.get());

        try {
            Duration duration = futureDuration.get();
            assertEquals(10, duration.getSeconds());
            assertEquals(7000000.0, duration.getNano());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Tests for the determineAudioLength method works for an ogg file using Python Mutagen. */
    @Test
    void testDetermineAudioLengthWorksForOggUsingMutagen() {
        Future<Duration> futureDuration = DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(
                OsUtil.buildFile("src", "test", "kotlin", "com", "github", "natche", "cyderutils", "audio",
                        "resources", "TastyCarrots.ogg"));
        assertDoesNotThrow(() -> futureDuration.get());

        try {
            Duration duration = futureDuration.get();
            assertEquals(10.0, duration.getSeconds());
            assertEquals(1.8E7, duration.getNano());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Tests for the determineAudioLength method works for an ogg file using FFmpeg. */
    @Test
    void testDetermineAudioLengthWorksForOggUsingFfmpeg() {
        Future<Duration> futureDuration = DetermineAudioLengthMethod.FFMPEG.determineAudioLength(
                OsUtil.buildFile("src", "test", "kotlin", "com", "github", "natche", "cyderutils", "audio",
                        "resources", "TastyCarrots.ogg"));
        assertDoesNotThrow(() -> futureDuration.get());

        try {
            Duration duration = futureDuration.get();
            assertEquals(10.0, duration.getSeconds());
            assertEquals(1.7E7, duration.getNano());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Tests for the determineAudioLength method works for a m4a file using Python Mutagen. */
    @Test
    void testDetermineAudioLengthWorksForM4aUsingMutagen() {
        Future<Duration> futureDuration = DetermineAudioLengthMethod.PYTHON_MUTAGEN.determineAudioLength(
                OsUtil.buildFile("src", "test", "kotlin", "com", "github", "natche", "cyderutils", "audio",
                        "resources", "TastyCarrots.m4a"));
        assertDoesNotThrow(() -> futureDuration.get());

        try {
            Duration duration = futureDuration.get();
            assertEquals(10.0, duration.getSeconds());
            assertEquals(3.1E7, duration.getNano());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Tests for the determineAudioLength method works for a m4a file using FFmpeg. */
    @Test
    void testDetermineAudioLengthWorksForM4aUsingFfmpegUsingMutagen() {
        Future<Duration> futureDuration = DetermineAudioLengthMethod.FFMPEG.determineAudioLength(
                OsUtil.buildFile("src", "test", "kotlin", "com", "github", "natche", "cyderutils", "audio",
                        "resources", "TastyCarrots.m4a"));
        assertDoesNotThrow(() -> futureDuration.get());

        try {
            Duration duration = futureDuration.get();
            assertEquals(10.0, duration.getSeconds());
            assertEquals(8000000.0, duration.getNano());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
