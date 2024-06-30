package com.github.natche.cyderutils.audio;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class AudioSetupTest {

    @Test
    public void testAudioSystemInitialization() {
        try {
            File audioFile = new File("src/test/resources/test.wav");
            if (!audioFile.exists()) {
                fail("Audio file not found.");
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            assertNotNull(clip, "Audio system initialized and clip loaded successfully.");
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
            fail("Exception details: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception details: " + e.getMessage());
        }
    }
}
