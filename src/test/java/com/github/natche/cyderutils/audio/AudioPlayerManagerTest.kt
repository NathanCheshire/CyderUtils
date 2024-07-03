package com.github.natche.cyderutils.audio

import com.github.natche.cyderutils.audio.cplayer.AudioPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.BufferedInputStream

/** Tests for instances of the AudioPlayerManager. */
class AudioPlayerManagerTest {
    /**
     * The audio player factory used by tests within this file.
     * // todo use me
     */
    private val testAudioPlayerFactory: (BufferedInputStream) -> AudioPlayer = {
        object : AudioPlayer {
            override fun play() {
                Thread.sleep(5000)
            }

            override fun close() {
                Thread.sleep(500)
            }
        }
    }

    /** Tests for construction. */
    @Test
    fun testConstruction() {
        assertThrows(NullPointerException::class.java) { AudioPlayerManager(null) }
        assertThrows(IllegalArgumentException::class.java) { AudioPlayerManager("") }
        assertThrows(IllegalArgumentException::class.java) { AudioPlayerManager("     ") }

        assertDoesNotThrow { AudioPlayerManager("natche") }
        assertDoesNotThrow { AudioPlayerManager() }
    }

    /** Tests for the get id method. */
    @Test
    fun testGetId() {
        val manager = AudioPlayerManager("sad-alex")
        assertEquals("sad-alex", manager.id)
    }

    /** Tests for the play general audio method. */
    @Test
    fun testPlayGeneralAudio() {
    }

    /** Tests for the play system audio method. */
    @Test
    fun testPlaySystemAudio() {
    }

    /** Tests for the stop general audio method. */
    @Test
    fun testStopGeneralAudio() {
    }

    /** Tests for the stop system audio method. */
    @Test
    fun testStopSystemAudio() {
    }

    /** Tests for the is system audio playing and specific file methods. */
    @Test
    fun testIsSystemAudioPlaying() {
    }

    /** Tests for the is general audio playing and specific file methods. */
    @Test
    fun testIsGeneralAudioPlaying() {
    }

    /** Tests for the hashcode method. */
    @Test
    fun testHashCode() {
    }

    /** Tests for the equals method. */
    @Test
    fun testEquals() {
        val first = AudioPlayerManager("first")
        val equalToFirst = AudioPlayerManager("first")
        val notEqual = AudioPlayerManager("third")

        assertEquals(first, first)
        assertEquals(first, equalToFirst)
        assertNotEquals(first, notEqual)
        assertNotEquals(first, Object())

        // todo more stuff with players
    }

    /** Tests for the toString method. */
    @Test
    fun testToString() {
    }
}