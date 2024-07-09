package com.github.natche.cyderutils.audio

import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

/** Tests for the audio validation util. */
class AudioValidationUtilTest {
    /** Tests for the is valid m4a file method. */
    @Test
    fun testIsValidM4aFile() {
        assertThrows(NullPointerException::class.java) { AudioValidationUtil.isValidM4aFile(null) }
        assertThrows(IllegalArgumentException::class.java)
        { AudioValidationUtil.isValidM4aFile(File("non_existent.m4a")) }
        assertThrows(IllegalArgumentException::class.java) { AudioValidationUtil.isValidM4aFile(File(".")) }

        val southWav = OsUtil.buildFile(
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
        val carrotsM4a = OsUtil.buildFile(
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

        assertFalse(AudioValidationUtil.isValidM4aFile(southWav))
        assertTrue(AudioValidationUtil.isValidM4aFile(carrotsM4a))
    }
}