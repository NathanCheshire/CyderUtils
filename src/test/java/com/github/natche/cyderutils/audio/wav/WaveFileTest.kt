package com.github.natche.cyderutils.audio.wav

import com.github.natche.cyderutils.exceptions.IllegalMethodException
import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.reflect.InvocationTargetException

/**
 * Tests for [WaveFile]s.
 */
class WaveFileTest {
    /**
     * Tests construction of wave files.
     */
    @Test
    fun testConstruction() {
        val constructor = WaveFile::class.java.getDeclaredConstructor()
        constructor.isAccessible = true

        val exception = assertThrows(InvocationTargetException::class.java) {
            constructor.newInstance()
        }

        val cause = exception.cause
        assertNotNull(cause)
        assertTrue(cause is IllegalMethodException)
        assertEquals("Invalid constructor, a valid wave file is required", cause?.message)

        assertThrows(NullPointerException::class.java) { WaveFile(null) }
        assertThrows(IllegalArgumentException::class.java) { WaveFile(File("non_existent_file.mp3")) }
        assertThrows(IllegalArgumentException::class.java) { WaveFile(File(".")) }
        assertThrows(IllegalArgumentException::class.java) { WaveFile(mp3) }

        assertDoesNotThrow { WaveFile(carrotsWav) }
        assertDoesNotThrow { WaveFile(southWav) }
    }

    /**
     * Tests for the get num channels. method.
     */
    @Test
    fun testGetNumChannels() {
        assertEquals(2, carrots.numChannels)
        assertEquals(2, south.numChannels)
    }

    /**
     * Tests for the get sample method and num samples.
     */
    @Test
    fun testGetSample() {
        assertEquals(7780352, carrots.numSamples)
        assertEquals(10237771, south.numSamples)

        // endpoints
        assertEquals(0, carrots.getSample(0))
        assertEquals(0, carrots.getSample(7780352 - 1))

        assertEquals(0, south.getSample(0))
        assertEquals(0, south.getSample(10237771 - 1))

        // middles
        assertEquals(57681, carrots.getSample( carrots.numSamples / 2))
        assertEquals(64247, south.getSample( south.numSamples / 2))
    }

    /**
     * Tests for the get max sample method.
     */
    @Test
    fun testGetMaxSample() {
        assertEquals(65535, carrots.maxSample)
        assertEquals(65535, south.maxSample)
    }

    /**
     * Tests for the get min sample method.
     */
    @Test
    fun testGetMinSample() {
        assertEquals(0, carrots.minSample)
        assertEquals(0, south.minSample)
    }

    /**
     * Tests for the get average sample method.
     */
    @Test
    fun testGetAverageSample() {
        assertEquals(32131, carrots.averageSample)
        assertEquals(33049, south.averageSample)
    }

    /**
     * Tests for the is playable method.
     */
    @Test
    fun testIsPlayable() {
        assertTrue(carrots.isPlayable)
        assertTrue(south.isPlayable)
    }

    /**
     * Tests for the stop method.
     */
    @Test
    fun testStop() {}

    /**
     * Tests for the get audio format.
     */
    @Test
    fun testGetAudioFormat() {}

    /**
     * Tests for the get sample size method.
     */
    @Test
    fun testGetSampleSize() {}

    /**
     * Tests for the get duration time method.
     */
    @Test
    fun testGetDurationTime() {}

    /**
     * Tests for the get num frames method.
     */
    @Test
    fun testGetNumFrames() {}

    /**
     * Tests for the get sample rate method.
     */
    @Test
    fun testGetSampleRate() {}

    /**
     * Tests for the get clip method.
     */
    @Test
    fun testGetClip() {}

    /**
     * Tests for the equals method.
     */
    @Test
    fun testEquals() {}

    /**
     * Tests for the hashcode method.
     */
    @Test
    fun testHashCode() {}

    /**
     * Tests for the toString method.
     */
    @Test
    fun testToString() {}

    companion object {
        private val carrotsWav = OsUtil.buildFile(
            "src",
            "test",
            "java",
            "com",
            "github",
            "natche",
            "cyderutils",
            "audio",
            "resources",
            "TastyCarrots.wav"
        )

        private val southWav = OsUtil.buildFile(
            "src",
            "test",
            "java",
            "com",
            "github",
            "natche",
            "cyderutils",
            "audio",
            "resources",
            "ManOfTheSouth.wav"
        )

        private val mp3 = OsUtil.buildFile(
            "src",
            "test",
            "java",
            "com",
            "github",
            "natche",
            "cyderutils",
            "audio",
            "resources",
            "TastyCarrots.mp3"
        )

        private lateinit var carrots: WaveFile
        private lateinit var south : WaveFile

        @JvmStatic
        @BeforeAll
        fun setUp(): Unit {
            carrots = WaveFile(carrotsWav)
            south = WaveFile(southWav)
        }
    }
}