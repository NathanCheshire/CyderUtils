package com.github.natche.cyderutils.audio.waveform

import com.github.natche.cyderutils.audio.CyderAudioFile
import com.github.natche.cyderutils.color.CyderColors
import com.github.natche.cyderutils.image.CyderImage
import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/** Tests for waveform image generation. */
class WaveformImageTest {
    /**
     * Saves the provided image to the build/test-results/images directory.
     */
    private fun saveGeneratedImage(image: CyderImage, filename: String) {
        val outputFile: File = OsUtil.buildFile("build", "test-results", "images", filename)
        image.saveTo(outputFile);
    }

    /** Tests the default waveform generation properties. */
    @Test
    fun testWaveformGenerationDefault() {
        val audioFile = CyderAudioFile.from(
            OsUtil.buildFile(
                "src",
                "test",
                "java",
                "com",
                "github",
                "natche",
                "cyderutils",
                "audio",
                "waveform",
                "resources",
                "RealThings.mp3"
            )
        )

        val builder = WaveformImage.WaveformImageBuilder(audioFile)

        val name = "RealThings"
        val descriptor = "Default"
        val filename =  "${name}${descriptor}.png"

        val truthFile = OsUtil.buildFile(
            "src",
            "test",
            "java",
            "com",
            "github",
            "natche",
            "cyderutils",
            "audio",
            "waveform",
            "resources",
            filename
        )

        val image = CyderImage.fromFile(truthFile)
        saveGeneratedImage(image, filename)
        assertTrue(image.equals(builder.generate()))
    }

    /** Test for generating an image using gray colors, white background and a resolution of 200x50 */
    @Test
    fun testWaveformGenerationGray() {
        val audioFile = CyderAudioFile.from(
            OsUtil.buildFile(
                "src",
                "test",
                "java",
                "com",
                "github",
                "natche",
                "cyderutils",
                "audio",
                "waveform",
                "resources",
                "RealThings.mp3"
            )
        )

        val builder = WaveformImage.WaveformImageBuilder(audioFile)
        builder.centerLineColor = CyderColors.gray
        builder.topWaveformColor = CyderColors.gray
        builder.bottomWaveformColor = CyderColors.gray
        builder.width = 200
        builder.height = 50

        val name = "RealThings"
        val descriptor = "GrayGray"
        val filename =  "${name}${descriptor}.png"

        val truthFile = OsUtil.buildFile(
            "src",
            "test",
            "java",
            "com",
            "github",
            "natche",
            "cyderutils",
            "audio",
            "waveform",
            "resources",
            filename
        )

        val image = CyderImage.fromFile(truthFile)
        saveGeneratedImage(image, filename)
        assertTrue(image.equals(builder.generate()))
    }

    /** Tests for generating an image with pink top and bottom, a navy center line, with a resolution of 6000x800. */
    @Test
    fun testWaveformGenerationPinkNavyLine() {
        val audioFile = CyderAudioFile.from(
            OsUtil.buildFile(
                "src",
                "test",
                "java",
                "com",
                "github",
                "natche",
                "cyderutils",
                "audio",
                "waveform",
                "resources",
                "RealThings.mp3"
            )
        )

        val builder = WaveformImage.WaveformImageBuilder(audioFile)
        builder.centerLineColor = CyderColors.navy
        builder.topWaveformColor = CyderColors.regularPink
        builder.bottomWaveformColor = CyderColors.regularPink
        builder.width = 6000
        builder.height = 800

        val name = "RealThings"
        val descriptor = "PinkNavyLine"
        val filename = "${name}${descriptor}.png"

        val truthFile = OsUtil.buildFile(
            "src",
            "test",
            "java",
            "com",
            "github",
            "natche",
            "cyderutils",
            "audio",
            "waveform",
            "resources",
            filename
        )
        val image = CyderImage.fromFile(truthFile)
        saveGeneratedImage(image, filename)
        assertTrue(image.equals(builder.generate()))
    }

    /** Tests for generating a small waveform image. */
    @Test
    fun testWaveformGenerationSmall() {
        val audioFile = CyderAudioFile.from(
            OsUtil.buildFile(
                "src",
                "test",
                "java",
                "com",
                "github",
                "natche",
                "cyderutils",
                "audio",
                "waveform",
                "resources",
                "RealThings.mp3"
            )
        )

        val builder = WaveformImage.WaveformImageBuilder(audioFile)
        builder.width = 100
        builder.height = 20

        val name = "RealThings"
        val descriptor = "Small"
        val filename = "${name}${descriptor}.png"

        val truthFile = OsUtil.buildFile(
            "src",
            "test",
            "java",
            "com",
            "github",
            "natche",
            "cyderutils",
            "audio",
            "waveform",
            "resources",
            filename
        )
        val image = CyderImage.fromFile(truthFile)
        saveGeneratedImage(image, filename)
        assertTrue(image.equals(builder.generate()))
    }
}