package com.github.natche.cyderutils.audio.validation

import com.github.natche.cyderutils.utils.OsUtil
import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

/** Tests for supported audio file types */
class SupportedAudioFileTypeTest {
    private val aac = OsUtil.buildFile(
        "src",
        "test",
        "kotlin",
        "com",
        "github",
        "natche",
        "cyderutils",
        "audio",
        "resources",
        "Nightcore.aac"
    )

    private val m4a = OsUtil.buildFile(
        "src",
        "test",
        "kotlin",
        "com",
        "github",
        "natche",
        "cyderutils",
        "audio",
        "resources",
        "TastyCarrots.m4a"
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

    private val ogg = OsUtil.buildFile(
        "src",
        "test",
        "kotlin",
        "com",
        "github",
        "natche",
        "cyderutils",
        "audio",
        "resources",
        "TastyCarrots.ogg"
    )

    private val wav = OsUtil.buildFile(
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

    /** Tests for the get extension method. */
    @Test
    fun testGetExtension() {
        assertEquals(".mp3", SupportedAudioFileType.MP3.extension)
        assertEquals(".m4a", SupportedAudioFileType.M4A.extension)
        assertEquals(".wav", SupportedAudioFileType.WAVE.extension)
        assertEquals(".ogg", SupportedAudioFileType.OGG.extension)
    }

    /** Tests for the get signature method. */
    @Test
    fun testGetSignature() {
        assertEquals(ImmutableList.of(0x49, 0x44, 0x33), SupportedAudioFileType.MP3.signature)
        assertEquals(ImmutableList.of<Int>(), SupportedAudioFileType.M4A.signature)
        assertEquals(ImmutableList.of(0x52, 0x49, 0x46, 0x46), SupportedAudioFileType.WAVE.signature)
        assertEquals(ImmutableList.of(0x4F, 0x67, 0x67, 0x53), SupportedAudioFileType.OGG.signature)
    }

    /** Tests for the get conversion arguments method. */
    @Test
    fun testGetConversionArguments() {
        assertEquals(ImmutableList.of("-q:a", "0", "-map", "a"), SupportedAudioFileType.MP3.conversionArguments)
        assertEquals(
            ImmutableList.of("-c:a", "acc", "-q:a", "100", "-map", "a"),
            SupportedAudioFileType.M4A.conversionArguments
        )
        assertEquals(ImmutableList.of("-map", "a"), SupportedAudioFileType.WAVE.conversionArguments)
        assertEquals(
            ImmutableList.of("-c:a", "libvorbis", "-q:a", "4", "-map", "a"),
            SupportedAudioFileType.OGG.conversionArguments
        )
    }

    /** Tests for the is supported method. */
    @Test
    fun testIsSupported() {
        assertThrows(NullPointerException::class.java) {
            SupportedAudioFileType.isSupported(null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SupportedAudioFileType.isSupported(File("non_existent_file.mp3"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            SupportedAudioFileType.isSupported(File("."))
        }

        assertFalse(SupportedAudioFileType.isSupported(aac))
        assertTrue(SupportedAudioFileType.isSupported(ogg))
        assertTrue(SupportedAudioFileType.isSupported(mp3))
        assertTrue(SupportedAudioFileType.isSupported(m4a))
        assertTrue(SupportedAudioFileType.isSupported(wav))
    }

    /** Tests for the is of type method. */
    @Test
    fun testIsOfType() {
        assertThrows(NullPointerException::class.java) {
            SupportedAudioFileType.OGG.isOfType(null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SupportedAudioFileType.OGG.isOfType(File("non_existent_file.mp3"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            SupportedAudioFileType.OGG.isOfType(File("."))
        }

        assertThrows(IllegalArgumentException::class.java) { SupportedAudioFileType.M4A.isOfType(m4a) }
        assertThrows(IllegalArgumentException::class.java) { SupportedAudioFileType.M4A.isOfType(mp3) }
        assertThrows(IllegalArgumentException::class.java) { SupportedAudioFileType.M4A.isOfType(ogg) }
        assertThrows(IllegalArgumentException::class.java) { SupportedAudioFileType.M4A.isOfType(wav) }

        assertTrue(SupportedAudioFileType.OGG.isOfType(ogg))
        assertFalse(SupportedAudioFileType.MP3.isOfType(ogg))
        assertFalse(SupportedAudioFileType.WAVE.isOfType(ogg))

        assertFalse(SupportedAudioFileType.OGG.isOfType(mp3))
        assertTrue(SupportedAudioFileType.MP3.isOfType(mp3))
        assertFalse(SupportedAudioFileType.WAVE.isOfType(mp3))

        assertFalse(SupportedAudioFileType.OGG.isOfType(wav))
        assertFalse(SupportedAudioFileType.MP3.isOfType(wav))
        assertTrue(SupportedAudioFileType.WAVE.isOfType(wav))
    }
}