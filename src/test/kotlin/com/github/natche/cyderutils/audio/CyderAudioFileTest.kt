package com.github.natche.cyderutils.audio

import com.github.natche.cyderutils.audio.validation.SupportedAudioFileType
import com.github.natche.cyderutils.enumerations.SystemPropertyKey
import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.IllegalStateException
import java.time.Duration

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

    /**
     * Tests for construction a CyderAudioFile via a builder.
     */
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

    /** Tests for the set dreamify high pass method. */
    @Test
    fun testSetDreamifyHighPass() {
        val file = CyderAudioFile(southWav)
        file.setDreamifyLowPass(100)
        assertThrows(IllegalArgumentException::class.java) { file.setDreamifyHighPass(99) }
        assertThrows(IllegalArgumentException::class.java) { file.setDreamifyHighPass(100) }
        assertDoesNotThrow { file.setDreamifyHighPass(101) }
    }

    /** Tests for the set dreamify low pass method. */
    @Test
    fun testSetDreamifyLowPass() {
        val file = CyderAudioFile(southWav)
        file.setDreamifyLowPass(0)
        file.setDreamifyHighPass(100)
        assertThrows(IllegalArgumentException::class.java) { file.setDreamifyLowPass(101) }
        assertThrows(IllegalArgumentException::class.java) { file.setDreamifyLowPass(100) }
        assertDoesNotThrow { file.setDreamifyLowPass(99) }
    }

    /** Tests for the set output directory method. */
    @Test
    fun testSetOutputDirectory() {
        val file = CyderAudioFile(southWav)
        assertThrows(NullPointerException::class.java) { file.setOutputDirectory(null) }
        assertThrows(IllegalArgumentException::class.java) { file.setOutputDirectory(File("file.txt")) }
        assertThrows(IllegalArgumentException::class.java) { file.setOutputDirectory(File("directory")) }
        assertDoesNotThrow { file.setOutputDirectory(File(".")) }
    }

    /** Tests for the convert to method. */
    @Test
    fun testConvertTo() {
        val file = CyderAudioFile(southWav)
        file.setOutputDirectory(File(SystemPropertyKey.JAVA_IO_TMPDIR.property))
        assertThrows(NullPointerException::class.java) { file.convertTo(null) }

        for (audioFormat in SupportedAudioFileType.values()) {
            val futureResult = file.convertTo(audioFormat)
            while (!futureResult.isDone) Thread.onSpinWait()
            val result = futureResult.get()
            assertNotNull(result)
        }
    }

    /** Tests for the get audio length method. */
    @Test
    fun testGetAudioLength() {
        val file = CyderAudioFile(southWav)
        assertThrows(NullPointerException::class.java) { file.getAudioLength(null) }

        var futureResult = file.getAudioLength(DetermineAudioLengthMethod.FFMPEG)
        while (!futureResult.isDone) Thread.onSpinWait()
        var result = futureResult.get()
        assertEquals(Duration.ofSeconds(10, 5000000), result)

        futureResult = file.getAudioLength(DetermineAudioLengthMethod.PYTHON_MUTAGEN)
        while (!futureResult.isDone) Thread.onSpinWait()
        // todo when PythonManager is done fix these
//        result = futureResult.get()
//        assertEquals(Duration.ofSeconds(10, 5000000), result)
    }

    /** Tests for the dreamify method. */
    @Test
    fun testDreamify() {
        val file = CyderAudioFile(southWav)
        file.setOutputDirectory(File(SystemPropertyKey.JAVA_IO_TMPDIR.property))
        val futureOptionalResult = file.dreamify()
        while (!futureOptionalResult.isDone) Thread.onSpinWait()
        Thread.sleep(5000)
        val optionalResult = futureOptionalResult.get()
        assertTrue(optionalResult.isPresent)
        assertNotNull(optionalResult.get())
    }

    /** Tests for the to wave file method. */
    @Test
    fun testToWaveFile() {
        assertDoesNotThrow { CyderAudioFile(southWav).toWaveFile() }
        assertThrows(IllegalStateException::class.java) { CyderAudioFile(carrotsM4a).toWaveFile() }
    }

    /** Tests for the equals method. */
    @Test
    fun testEquals() {
        val first = CyderAudioFile(southWav)
        val equal = CyderAudioFile(southWav)
        val notEqual = CyderAudioFile(carrotsM4a)

        assertEquals(first, first)
        assertEquals(first, equal)
        assertNotEquals(first, notEqual)
        assertNotEquals(first, Object())

        first.setDreamifyHighPass(7000)
        assertNotEquals(first, equal)
        equal.setDreamifyHighPass(7000)
        assertEquals(first, equal)
    }

    /** Tests for the to string method. */
    @Test
    fun testToString() {
        val first = CyderAudioFile(southWav)
        first.setDreamifyHighPass(5000)
        first.setDreamifyLowPass(1000)
        first.setOutputDirectory(File("."))

        val second = CyderAudioFile(carrotsM4a)

        assertEquals(
            "CyderAudioFile{audioFile=" + southWav.absolutePath
                    + ", dreamifyLowPass=1000, dreamifyHighPass=5000}", first.toString()
        )
        assertEquals(
            "CyderAudioFile{audioFile=" + carrotsM4a.absolutePath
                    + ", dreamifyLowPass=200, dreamifyHighPass=1500}", second.toString()
        )
    }

    /** Tests for the hashcode method. */
    @Test
    fun testHashCode() {
        val first = CyderAudioFile(southWav)
        val equalFirst = CyderAudioFile(southWav)
        val second = CyderAudioFile(carrotsM4a)

        assertEquals(first.hashCode(), equalFirst.hashCode())
        assertNotEquals(first.hashCode(), second.hashCode())
    }

    companion object {
        val southWav: File = OsUtil.buildFile(
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
        val carrotsM4a: File = OsUtil.buildFile(
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
        val nightcoreAac: File = OsUtil.buildFile(
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
    }
}