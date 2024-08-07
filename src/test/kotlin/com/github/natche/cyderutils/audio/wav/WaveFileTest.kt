package com.github.natche.cyderutils.audio.wav

import com.github.natche.cyderutils.exceptions.IllegalMethodException
import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.reflect.InvocationTargetException

/** Tests for [WaveFile]s. */
class WaveFileTest {
    /** Tests construction of wave files. */
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

    /** Tests for the get num channels. method. */
    @Test
    fun testGetNumChannels() {
        assertEquals(2, carrots.numChannels)
        assertEquals(2, south.numChannels)
    }

    /** Tests for the get sample method and num samples. */
    @Test
    fun testGetSample() {
        assertEquals(441344, carrots.numSamples)
        assertEquals(480256, south.numSamples)

        // endpoints
        assertEquals(0, carrots.getSample(0))
        assertEquals(63756, carrots.getSample(441344 - 1))

        assertEquals(0, south.getSample(0))
        assertEquals(50534, south.getSample(480256 - 1))

        // middles
        assertEquals(4027, carrots.getSample(carrots.numSamples / 2))
        assertEquals(63029, south.getSample(south.numSamples / 2))
    }

    /** Tests for the get max sample method. */
    @Test
    fun testGetMaxSample() {
        assertEquals(65535, carrots.maxSample)
        assertEquals(65535, south.maxSample)
    }

    /** Tests for the get min sample method. */
    @Test
    fun testGetMinSample() {
        assertEquals(0, carrots.minSample)
        assertEquals(0, south.minSample)
    }

    /** Tests for the get average sample method. */
    @Test
    fun testGetAverageSample() {
        assertEquals(31889, carrots.averageSample)
        assertEquals(32736, south.averageSample)
    }

    /** Tests for the is playable method. */
    @Test
    fun testIsPlayable() {
        assertTrue(carrots.isPlayable)
        assertTrue(south.isPlayable)
    }

    /** Tests for the stop method. */
    @Test
    fun testStop() {
        val localSouth = WaveFile(southWav)
        assertThrows(IllegalStateException::class.java) { localSouth.stop() }

        localSouth.play()
        Thread.sleep(50)
        assertDoesNotThrow { localSouth.stop() }
    }

    /** Tests for the get audio format. */
    @Test
    fun testGetAudioFormat() {
        val carrotFormat = carrots.audioFormat
        assertEquals(2, carrotFormat.channels)
        assertEquals(44100.0f, carrotFormat.frameRate)
        assertEquals(4, carrotFormat.frameSize)
        assertFalse(carrotFormat.isBigEndian)
        assertEquals(44100.0f, carrotFormat.sampleRate)
        assertEquals(16, carrotFormat.sampleSizeInBits)

        val southFormat = south.audioFormat
        assertEquals(2, southFormat.channels)
        assertEquals(48000.0f, southFormat.frameRate)
        assertEquals(4, southFormat.frameSize)
        assertFalse(southFormat.isBigEndian)
        assertEquals(48000.0f, southFormat.sampleRate)
        assertEquals(16, southFormat.sampleSizeInBits)
    }

    /** Tests for the get sample size method. */
    @Test
    fun testGetSampleSize() {
        assertEquals(2, carrots.sampleSize)
        assertEquals(2, south.sampleSize)
    }

    /** Tests for the get duration time method. */
    @Test
    fun testGetDurationTime() {
        assertEquals(10.0078f, carrots.durationTime)
        assertEquals(10.005333f, south.durationTime)
    }

    /** Tests for the get num frames method. */
    @Test
    fun testGetNumFrames() {
        assertEquals(441344, carrots.numFrames)
        assertEquals(480256, south.numFrames)
    }

    /** Tests for the get sample rate method. */
    @Test
    fun testGetSampleRate() {
        assertEquals(44100, carrots.sampleRate)
        assertEquals(48000, south.sampleRate)
    }

    /** Tests for the equals method. */
    @Test
    fun testEquals() {
        assertEquals(carrots, carrots)
        assertEquals(carrots, equalCarrots)
        assertNotEquals(carrots, south)
        assertNotEquals(carrots, Object())
    }

    /** Tests for the hashcode method. */
    @Test
    fun testHashCode() {
        assertEquals(carrots.hashCode(), carrots.hashCode())
        assertEquals(carrots.hashCode(), WaveFile(carrotsWav).hashCode())
        assertNotEquals(carrots.hashCode(), south.hashCode())

        assertEquals(865705099, carrots.hashCode())
        assertEquals(754789234, south.hashCode())
    }

    /** Tests for the toString method. */
    @Test
    fun testToString() {
        assertEquals(
            "WaveFile{numChannels=2, dataLength=1765376, isPlayable=true, sampleSize=2,"
                    + " numFrames=441344, sampleRate=44100, wavFile=" + carrotsWav.absolutePath
                    + ", clipPlaying=false}", carrots.toString()
        )
        assertEquals(
            "WaveFile{numChannels=2, dataLength=1921024, isPlayable=true, sampleSize=2,"
                    + " numFrames=480256, sampleRate=48000, wavFile=" + southWav.absolutePath
                    + ", clipPlaying=false}", south.toString()
        )
    }

    companion object {
        private val carrotsWav = OsUtil.buildFile(
            "src",
            "test",
            "kotlin",
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
            "kotlin",
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
            "kotlin",
            "com",
            "github",
            "natche",
            "cyderutils",
            "audio",
            "resources",
            "TastyCarrots.mp3"
        )

        private lateinit var carrots: WaveFile
        private lateinit var equalCarrots: WaveFile
        private lateinit var south: WaveFile

        @JvmStatic
        @BeforeAll
        fun setUp(): Unit {
            carrots = WaveFile(carrotsWav)
            equalCarrots = WaveFile(carrotsWav)
            south = WaveFile(southWav)
        }
    }
}