package com.github.natche.cyderutils.audio.waveform

import com.github.natche.cyderutils.audio.CyderAudioFile
import com.github.natche.cyderutils.color.CyderColors
import com.github.natche.cyderutils.image.CyderImage
import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for waveform image generation.
 */
class WaveformImageTest {
    /**
     * Tests for generating an image with pink top and bottom, a navy center line, with a resolution of 6000x800.
     */
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
            "${name}${descriptor}.png"
        )
        val image = CyderImage.fromFile(truthFile)
        assertTrue(image.equals(builder.generate()))
    }
}