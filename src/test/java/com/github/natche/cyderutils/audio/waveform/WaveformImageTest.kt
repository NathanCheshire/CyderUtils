package com.github.natche.cyderutils.audio.waveform

import com.github.natche.cyderutils.audio.CyderAudioFile
import com.github.natche.cyderutils.color.CyderColors
import com.github.natche.cyderutils.image.CyderImage
import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.platform.commons.logging.LoggerFactory
import java.awt.image.BufferedImage
import java.io.File

/** Tests for waveform image generation. */
class WaveformImageTest {
    private val logger = LoggerFactory.getLogger(WaveformImageTest::class.java)

    /**
     * Saves the provided image to the build/test-results/images directory.
     */
    private fun saveGeneratedImage(image: CyderImage, filename: String) {
        val outputFile: File = OsUtil.buildFile("build", "test-results", "images", filename)
        image.saveTo(outputFile)
    }

    private fun imagesAreSimilar(img1: BufferedImage, img2: BufferedImage, tolerance: Double = 0.01): Boolean {
        if (img1.width != img2.width || img1.height != img2.height) {
            return false
        }
        var diffCount = 0
        for (y in 0 until img1.height) {
            for (x in 0 until img1.width) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    diffCount++
                }
            }
        }
        val totalPixels = img1.width * img1.height
        val diffPercentage = diffCount.toDouble() / totalPixels
        logger.info { "Difference percentage: ${"%.2f".format(diffPercentage * 100)}%" }
        return diffPercentage <= tolerance
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
        assertTrue(imagesAreSimilar(image.bufferedImage, builder.generate().bufferedImage))
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
        assertTrue(imagesAreSimilar(image.bufferedImage, builder.generate().bufferedImage))
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
        assertTrue(imagesAreSimilar(image.bufferedImage, builder.generate().bufferedImage))
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
        assertTrue(imagesAreSimilar(image.bufferedImage, builder.generate().bufferedImage))
    }
}