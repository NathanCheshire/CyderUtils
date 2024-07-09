package com.github.natche.cyderutils.audio

import com.github.natche.cyderutils.audio.cplayer.AudioPlayer
import com.github.natche.cyderutils.threads.ThreadUtil
import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.BufferedInputStream

/** Tests for instances of the AudioPlayerManager. */
class AudioPlayerManagerTest {
    /**
     * The audio player factory used by tests within this file.
     */
    private val testAudioPlayerFactory: (BufferedInputStream) -> AudioPlayer = {
        object : AudioPlayer {
            override fun play() {
                Thread.sleep(5000)
            }

            override fun close() {
                Thread.sleep(500)
            }

            override fun toString(): String {
                return "AudioPlayerManagerTest.testAudioPlayerFactory"
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

    /** Tests for the play general audio, stop general audio, and is general audio playing methods. */
    @Test
    fun testPlayGeneralAudio() {
        val manager = AudioPlayerManager()
        manager.setAudioPlayerFactory(testAudioPlayerFactory)

        assertFalse(manager.isAudioPlaying)
        assertFalse(manager.isGeneralAudioPlaying)
        assertFalse(manager.isGeneralAudioPlaying(southWav))

        manager.playGeneralAudio(southWav)

        assertTrue(manager.isAudioPlaying)
        assertTrue(manager.isGeneralAudioPlaying)
        assertTrue(manager.isGeneralAudioPlaying(southWav))
        assertFalse(manager.isSystemAudioPlaying)

        manager.stopGeneralAudio()
        Thread.sleep(5000)

        assertFalse(manager.isAudioPlaying)
        assertFalse(manager.isGeneralAudioPlaying)
        assertFalse(manager.isGeneralAudioPlaying(southWav))
    }

    /** Tests for the play system audio, stop system audio, and is system audio playing methods. */
    @Test
    fun testPlaySystemAudio() {
        val manager = AudioPlayerManager()
        manager.setAudioPlayerFactory(testAudioPlayerFactory)

        assertFalse(manager.isAudioPlaying)
        assertFalse(manager.isSystemAudioPlaying)
        assertFalse(manager.isSystemAudioPlaying(southWav))

        manager.playSystemAudio(southWav)

        assertTrue(manager.isAudioPlaying)
        assertTrue(manager.isSystemAudioPlaying)
        assertTrue(manager.isSystemAudioPlaying(southWav))
        assertFalse(manager.isGeneralAudioPlaying)

        manager.stopSystemAudio()
        Thread.sleep(5000)

        assertFalse(manager.isAudioPlaying)
        assertFalse(manager.isSystemAudioPlaying)
        assertFalse(manager.isSystemAudioPlaying(southWav))
    }

    /** Tests for the hashcode method. */
    @Test
    fun testHashCode() {
        val first = AudioPlayerManager("first")
        val equalToFirst = AudioPlayerManager("first")
        val notEqual = AudioPlayerManager("third")

        assertEquals(97440463, first.hashCode())
        assertEquals(97440463, equalToFirst.hashCode())
        assertEquals(110331270, notEqual.hashCode())

        assertEquals(first.hashCode(), equalToFirst.hashCode())
        assertNotEquals(first.hashCode(), notEqual.hashCode())
        assertNotEquals(first.hashCode(), Object().hashCode())

        first.setAudioPlayerFactory(testAudioPlayerFactory)
        first.playSystemAudio(southWav)

        assertNotEquals(first.hashCode(), equalToFirst.hashCode())
        ThreadUtil.sleep(5500)
        assertEquals(first.hashCode(), equalToFirst.hashCode())
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

        first.setAudioPlayerFactory(testAudioPlayerFactory)
        first.playSystemAudio(southWav)

        assertNotEquals(first, equalToFirst)
        ThreadUtil.sleep(5500)
        assertEquals(first, equalToFirst)
    }

    /** Tests for the toString method. */
    @Test
    fun testToString() {
        val first = AudioPlayerManager("first")
        val second = AudioPlayerManager("second")
        second.setAudioPlayerFactory(testAudioPlayerFactory)
        second.playSystemAudio(carrotsMp3)

        assertEquals("AudioPlayerManager{generalPlayer=null, systemPlayers=[], id=first}", first.toString())
        assertEquals("AudioPlayerManager{generalPlayer=null, systemPlayers=[AudioPlayer{audioFile="
                + carrotsMp3.absolutePath + ", player=AudioPlayerManagerTest.testAudioPlayerFactory,"
                + " onCompletionCallbacks=[AudioPlayerManager{id=second}.systemPlayer{file="
                + carrotsMp3.absolutePath + "}.removeFromSystemPlayersCompletionCallback], canceled=false,"
                + " playing=true}], id=second}", second.toString())
    }

    companion object {
        private val carrotsMp3 = OsUtil.buildFile(
            "src",
            "test",
            "kotlin",
            "com",
            "github",
            "natche",
            "cyderutils",
            "audio",
            "resources",
            "TastyCarrots.mp3"
        )

        private val southWav = OsUtil.buildFile(
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
    }
}