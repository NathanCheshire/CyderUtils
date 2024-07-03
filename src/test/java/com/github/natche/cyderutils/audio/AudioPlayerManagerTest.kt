package com.github.natche.cyderutils.audio

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/** Tests for instances of the AudioPlayerManager. */
class AudioPlayerManagerTest {
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
        val first = AudioPlayerManager("first")
        val equalToFirst = AudioPlayerManager("first")
        val notEqual = AudioPlayerManager("third")

        assertEquals(first, first)
        assertEquals(first, equalToFirst)
        assertNotEquals(first, notEqual)
        assertNotEquals(first, Object())

        // todo more stuff with players
    }

    /** Tests for the equals method. */
    @Test
    fun testEquals() {
    }

    /** Tests for the toString method. */
    @Test
    fun testToString() {
    }
}