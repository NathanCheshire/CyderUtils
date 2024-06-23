package com.github.natche.cyderutils.audio.cplayer

import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Test for the [CPlayer].
 */
class CPlayerTest
/**
 * Creates a new instance of this class for testing purposes.
 */
{
    private val validAudioFile = OsUtil.buildFile(
        "src",
        "test",
        "java",
        "com",
        "github",
        "natche",
        "cyderutils",
        "audio",
        "resources",
        "TastyCarrots.mp3"
    )

    private val anotherValidAudioFile = OsUtil.buildFile(
        "src",
        "test",
        "java",
        "com",
        "github",
        "natche",
        "cyderutils",
        "audio",
        "resources",
        "TastyCarrots.wav"
    )

    private val invalidAudioFile = OsUtil.buildFile(
        "src",
        "test",
        "java",
        "com",
        "github",
        "natche",
        "cyderutils",
        "audio",
        "resources",
        "Nightcore.aac"
    )

    /**
     * Tests for construction of CPlayers.
     */
    @Test
    fun testConstruction() {
        assertThrows(NullPointerException::class.java) { CPlayer(null) }
        assertThrows(IllegalArgumentException::class.java) { CPlayer(File("non_existent_file.txt")) }
        assertThrows(IllegalArgumentException::class.java) { CPlayer(File(".")) }
        assertThrows(IllegalArgumentException::class.java) { CPlayer(invalidAudioFile) }

        assertDoesNotThrow { CPlayer(validAudioFile) }
    }

    @Test
    fun testPlay() {
        val player = CPlayer(validAudioFile)
        assertDoesNotThrow { player.play() }
        assertTrue(player.isPlaying)
    }

    /**
     * Tests for the following methods:
     *  - [CPlayer.play]
     *  - [CPlayer.cancelPlaying]
     *  - [CPlayer.isPlaying]
     *  - [CPlayer.isCanceled]
     *  - [CPlayer.getAudioFile]
     *  - [CPlayer.isUsingAudioFile]
     */
    @Test
    fun testCancelPlaying() {
        val called = AtomicBoolean(false)
        val player = CPlayer(validAudioFile)
        player.addOnCompletionCallback { called.set(true) }
        assertDoesNotThrow { player.play() }
        Thread.sleep(500)
        assertEquals(validAudioFile, player.audioFile)
        assertTrue(player.isUsingAudioFile(validAudioFile))
        assertTrue(player.isPlaying)
        player.cancelPlaying()
        assertTrue(player.isCanceled)
        // Sleep to allow callbacks to execute if they would and state to reset
        Thread.sleep(500)
        assertFalse(called.get())
        assertFalse(player.isPlaying)
    }

    @Test
    fun testStopPlaying() {
        val called = AtomicBoolean(false)
        val player = CPlayer(validAudioFile)
        player.addOnCompletionCallback { called.set(true) }
        assertDoesNotThrow { player.play() }
        Thread.sleep(500)
        assertEquals(validAudioFile, player.audioFile)
        assertTrue(player.isUsingAudioFile(validAudioFile))
        assertTrue(player.isPlaying)
        player.stopPlaying()
        assertFalse(player.isCanceled)
        // Sleep to allow callbacks to execute if they would and state to reset
        Thread.sleep(500)
        assertTrue(called.get())
        assertFalse(player.isPlaying)
    }

    @Test
    fun testAddOnCompletionCallback() {
        val int = AtomicInteger(0)
        val player = CPlayer(validAudioFile)

        player.addOnCompletionCallback {int.incrementAndGet()}
        player.addOnCompletionCallback {int.incrementAndGet()}
        player.addOnCompletionCallback {int.incrementAndGet()}
        player.addOnCompletionCallback {int.incrementAndGet()}
        player.addOnCompletionCallback {int.incrementAndGet()}

        player.play()
        // todo sleeps shouldn't be needed here
        Thread.sleep(2000)
        player.stopPlaying()
        Thread.sleep(2000)
        assertEquals(5, int.get())
        assertFalse(player.isPlaying)
        assertFalse(player.isCanceled)
    }

    @Test
    fun testEquals() {}

    @Test
    fun testHashCode() {}

    @Test
    fun testToString() {}
}