package com.github.natche.cyderutils.audio

import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File

/** Tests for [CyderAudioFile]s. */
class CyderAudioFileTest {
    /** Tests for creation from factories. */
    @Test
    fun testCreationFactories() {
        val file: File? = null
        assertThrows(NullPointerException::class.java) { CyderAudioFile.from(file) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile.from("") }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile.from("    ") }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile.from(File(".")) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile.from("something.mp3") }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile.from(File("something.mp3")) }

        assertDoesNotThrow { CyderAudioFile.from(southWav) }
        assertDoesNotThrow { CyderAudioFile.from(carrotsM4a) }
    }

    /** Tests for creation via the constructors. */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { CyderAudioFile(null) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile(File("something.mp3")) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile(File(".")) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile(nightcoreAac) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile(nightcoreAac, 0, 0) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile(nightcoreAac, 20, 20) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile(nightcoreAac, 19, 20) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile(nightcoreAac, 20, 21) }

        assertDoesNotThrow { CyderAudioFile(southWav) }
        assertDoesNotThrow { CyderAudioFile(southWav, 20, 0) }
    }

    @Test
    fun testConstructionViaBuilder() {
        assertThrows(NullPointerException::class.java) { CyderAudioFile.Builder(null) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile.Builder(File("file.mp3")) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile.Builder(File(".")) }
        assertThrows(IllegalArgumentException::class.java) { CyderAudioFile.Builder(nightcoreAac) }

        assertDoesNotThrow { CyderAudioFile.Builder(southWav) }

        val builder = CyderAudioFile.Builder(southWav)
        assertThrows(IllegalArgumentException::class.java) { builder.setHighpass(-20) }
        assertThrows(IllegalArgumentException::class.java) { builder.setLowpass(40000) }

        assertDoesNotThrow { builder.setLowpass(0) }
        assertDoesNotThrow { builder.setHighpass(100) }
        assertThrows(IllegalArgumentException::class.java) { builder.setLowpass(101) }
        assertThrows(IllegalArgumentException::class.java) { builder.setLowpass(100) }
        assertDoesNotThrow { builder.setLowpass(99) }

        assertThrows(NullPointerException::class.java) { builder.setOutputDirectory(null) }
        assertThrows(IllegalArgumentException::class.java) { builder.setOutputDirectory(File("file.mp3")) }
        assertThrows(IllegalArgumentException::class.java) { builder.setOutputDirectory(File("../directoryThing")) }

        assertDoesNotThrow { builder.setOutputDirectory(File(".")) }
    }

    @Test
    fun testSetDreamifyHighPass() {
        val file = CyderAudioFile(southWav)
        file.setDreamifyLowPass(100)
        assertThrows(IllegalArgumentException::class.java) { file.setDreamifyHighPass(99) }
        assertThrows(IllegalArgumentException::class.java) { file.setDreamifyHighPass(100) }
        assertDoesNotThrow { file.setDreamifyHighPass(101) }
    }

    @Test
    fun testSetDreamifyLowPass() {
        val file = CyderAudioFile(southWav)
        file.setDreamifyLowPass(0)
        file.setDreamifyHighPass(100)
        assertThrows(IllegalArgumentException::class.java) { file.setDreamifyLowPass(101) }
        assertThrows(IllegalArgumentException::class.java) { file.setDreamifyLowPass(100) }
        assertDoesNotThrow { file.setDreamifyLowPass(99) }
    }

    @Test
    fun testSetOutputDirectory() {
        val file = CyderAudioFile(southWav)
        assertThrows(NullPointerException::class.java) { file.setOutputDirectory(null) }
        assertThrows(IllegalArgumentException::class.java) { file.setOutputDirectory(File("file.txt")) }
        assertThrows(IllegalArgumentException::class.java) { file.setOutputDirectory(File("directory")) }
        assertDoesNotThrow { file.setOutputDirectory(File(".")) }
    }

    @Test
    fun testConvertTo() {

    }

    @Test
    fun testGetAudioLength() {

    }

    @Test
    fun testDreamify() {

    }

    @Test
    fun testToWaveFile() {

    }

    @Test
    fun testEquals() {

    }

    @Test
    fun testToString() {

    }

    @Test
    fun testHashCode() {

    }

    companion object {
        val southWav: File = OsUtil.buildFile(
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
        val carrotsM4a: File = OsUtil.buildFile(
            "src",
            "test",
            "java",
            "com",
            "github",
            "natche",
            "cyderutils",
            "audio",
            "resources",
            "TastyCarrots.m4a"
        )
        val nightcoreAac: File = OsUtil.buildFile(
            "src",
            "test",
            "java",
            "com",
            "github",
            "natche",
            "cyderutils",
            "audio",
            "resources",
            "Nightcore.aac"
        )
    }
}