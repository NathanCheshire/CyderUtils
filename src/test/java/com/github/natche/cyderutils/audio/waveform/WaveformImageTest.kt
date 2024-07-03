package com.github.natche.cyderutils.audio.waveform

import com.github.natche.cyderutils.audio.CyderAudioFile
import com.github.natche.cyderutils.color.CyderColors
import org.junit.jupiter.api.Test

class WaveformImageTest {
    @Test
    fun testWaveformGenerationPinkNavyLine() {
        val file = CyderAudioFile.from("/users/nathancheshire/Downloads/RealThings.mp3")
        val builder = WaveformImage.WaveformImageBuilder(file)
        builder.centerLineColor = CyderColors.navy
        builder.topWaveformColor = CyderColors.regularPink
        builder.bottomWaveformColor = CyderColors.regularPink
        builder.width = 6000
        builder.height = 800

        val cyderAudioFile = builder.generate()
        val name = "RealThings"
        val descriptor = "PinkNavyLine"
        cyderAudioFile.saveTo("/users/nathancheshire/projects/CyderUtils/src"
                + "/test/java/com/github/natch/audio/waveform/${name}${descriptor}.png")
    }
}