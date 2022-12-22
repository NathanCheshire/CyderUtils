package cyder.files

import com.google.common.collect.ImmutableList
import main.java.cyder.files.FileUtil
import main.java.cyder.utils.StaticUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Tests for [FileUtil]s.
 */
class FileUtilTest {
    /**
     * Tests for the is supported image extension method.
     */
    @Test
    fun testIsSupportedImageExtension() {
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.isSupportedImageExtension(null) }
        Assertions.assertFalse(FileUtil.isSupportedImageExtension(File("file")))
        Assertions.assertFalse(FileUtil.isSupportedImageExtension(File("c:\\users")))
        Assertions.assertFalse(FileUtil.isSupportedImageExtension(File("File.mp3")))

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.isSupportedImageExtension(File("File.png"))
        }

        Assertions.assertTrue(FileUtil.isSupportedImageExtension(StaticUtil.getStaticResource("Default.png")))
        Assertions.assertTrue(FileUtil.isSupportedImageExtension(StaticUtil.getStaticResource("x.png")))
    }

    /**
     * Tests for the is supported audio extension method.
     */
    @Test
    fun testIsSupportedAudioExtension() {
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.isSupportedAudioExtension(null) }
        Assertions.assertFalse(FileUtil.isSupportedAudioExtension(File("file")))
        Assertions.assertFalse(FileUtil.isSupportedAudioExtension(File("c:\\users")))
        Assertions.assertFalse(FileUtil.isSupportedAudioExtension(File("File.png")))

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.isSupportedAudioExtension(File("File.mp3"))
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.isSupportedAudioExtension(File("File.wav"))
        }

        Assertions.assertTrue(FileUtil.isSupportedAudioExtension(
                StaticUtil.getStaticResource("223.mp3")))
        Assertions.assertTrue(FileUtil.isSupportedAudioExtension(
                StaticUtil.getStaticResource("allthestars.mp3")))
        Assertions.assertTrue(FileUtil.isSupportedAudioExtension(
                StaticUtil.getStaticResource("allthestars.wav")))
        Assertions.assertTrue(FileUtil.isSupportedAudioExtension(
                StaticUtil.getStaticResource("commando.wav")))
    }

    /**
     * Tests for the is supported font extension method.
     */
    @Test
    fun testIsSupportedFontExtension() {
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.isSupportedFontExtension(null) }
        Assertions.assertFalse(FileUtil.isSupportedFontExtension(File("file")))
        Assertions.assertFalse(FileUtil.isSupportedFontExtension(File("c:\\users")))
        Assertions.assertFalse(FileUtil.isSupportedFontExtension(File("File.png")))

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.isSupportedFontExtension(File("File.ttf"))
        }

        Assertions.assertFalse(FileUtil.isSupportedFontExtension(StaticUtil.getStaticResource("x.png")))
        Assertions.assertTrue(FileUtil.isSupportedFontExtension(StaticUtil.getStaticResource("tahoma.ttf")))
    }

    /**
     * Tests for the file matches signature method.
     */
    @Test
    fun testFileMatchesSignature() {
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.fileMatchesSignature(null, null)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.fileMatchesSignature(File(""), null)
        }
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.fileMatchesSignature(StaticUtil.getStaticResource("x.png"), null)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.fileMatchesSignature(StaticUtil.getStaticResource("x.png"), ImmutableList.of())
        }

        Assertions.assertFalse(FileUtil.fileMatchesSignature(StaticUtil.getStaticResource("x.png"),
                ImmutableList.of(0xFA)))
        Assertions.assertFalse(FileUtil.fileMatchesSignature(StaticUtil.getStaticResource("x.png"),
                FileUtil.PNG_SIGNATURE))
    }

    /**
     * Tests for the get filename method.
     */
    @Test
    fun testGetFilename() {
        Assertions.assertThrows(IllegalArgumentException::class.java) { FileUtil.getFilename("") }
        Assertions.assertEquals("", FileUtil.getFilename(File("")))
        Assertions.assertEquals("MyFile", FileUtil.getFilename(File("MyFile.png")))
        Assertions.assertEquals("My File", FileUtil.getFilename(File("My File.png")))
        Assertions.assertEquals("My_File", FileUtil.getFilename(File("My_File.png")))
        Assertions.assertEquals("My_File_second", FileUtil.getFilename(File("My_File_second.png")))
        Assertions.assertEquals("yyyy.mm.dd", FileUtil.getFilename(File("yyyy.mm.dd.png")))

        Assertions.assertThrows(IllegalArgumentException::class.java) { FileUtil.getFilename("") }
        Assertions.assertEquals("MyFile", FileUtil.getFilename("MyFile.png"))
        Assertions.assertEquals("My File", FileUtil.getFilename("My File.png"))
        Assertions.assertEquals("My_File", FileUtil.getFilename("My_File.png"))
        Assertions.assertEquals("My_File_second", FileUtil.getFilename("My_File_second.png"))
        Assertions.assertEquals("yyyy.mm.dd", FileUtil.getFilename("yyyy.mm.dd.png"))
    }
}