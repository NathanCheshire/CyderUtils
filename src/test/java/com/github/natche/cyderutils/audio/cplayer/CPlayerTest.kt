package com.github.natche.cyderutils.audio.cplayer

import com.github.natche.cyderutils.exceptions.IllegalMethodException
import com.github.natche.cyderutils.structures.CyderRunnable
import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.lang.reflect.InvocationTargetException
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
    /**
     * A valid audio file.
     */
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

    /**
     * Another valid audio file of a different extension.
     */
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

    /**
     * An invalid audio file of an unsupported extension.
     */
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

    /**
     * Tests to ensure the suppressed default constructor is not invocable.
     */
    @Test
    fun testConstructionUsingSuppressedConstructor() {
        val constructor = CPlayer::class.java.getDeclaredConstructor()
        constructor.isAccessible = true

        val exception = assertThrows(InvocationTargetException::class.java) {
            constructor.newInstance()
        }

        val cause = exception.cause
        assertNotNull(cause)
        assertTrue(cause is IllegalMethodException)
        assertEquals("Invalid constructor; required audio file", cause?.message)
    }

    /**
     * Tests for the play method.
     */
    @Test
    fun testPlay() {
        val player = CPlayer(validAudioFile)
        assertDoesNotThrow { player.play() }
        assertTrue(player.isPlaying)
    }

    /**
     * Tests for the functionality of cancel playing; transitively tests the following methods:
     *
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
        assertThrows(IllegalStateException::class.java) { player.play() }
        assertEquals(validAudioFile, player.audioFile)
        assertTrue(player.isUsingAudioFile(validAudioFile))
        assertTrue(player.isPlaying)
        player.cancelPlaying()
        assertTrue(player.isCanceled)
        assertFalse(called.get())
        assertFalse(player.isPlaying)
    }

    /**
     * Tests the functionality of stop playing.
     */
    @Test
    fun testStopPlaying() {
        val called = AtomicBoolean(false)
        val player = CPlayer(validAudioFile)
        player.addOnCompletionCallback { called.set(true) }
        assertDoesNotThrow { player.play() }
        assertEquals(validAudioFile, player.audioFile)
        assertTrue(player.isUsingAudioFile(validAudioFile))
        assertTrue(player.isPlaying)
        player.stopPlaying()
        assertFalse(player.isCanceled)
        Thread.sleep(1000)
        assertTrue(called.get())
        assertFalse(player.isPlaying)
        // fuck
    }

    /**
     * Tests for adding on completion callbacks and that they are called.
     */
    @Test
    fun testAddOnCompletionCallback() {
        val int = AtomicInteger(0)
        val player = CPlayer(validAudioFile)

        player.addOnCompletionCallback { int.incrementAndGet() }
            .addOnCompletionCallback { int.incrementAndGet() }
            .addOnCompletionCallback { int.incrementAndGet() }
            .addOnCompletionCallback { int.incrementAndGet() }
            .addOnCompletionCallback { int.incrementAndGet() }

        player.play()
        player.stopPlaying()
        Thread.sleep(1000)
        assertEquals(5, int.get())
        assertFalse(player.isPlaying)
        assertFalse(player.isCanceled)
    }

    /**
     * Test for the equals method.
     */
    @Test
    fun testEquals() {
        val first = CPlayer(validAudioFile)
        val equal = CPlayer(validAudioFile)

        val notEqual = CPlayer(validAudioFile).addOnCompletionCallback { Thread.sleep(1) }

        assertEquals(first, first)
        assertEquals(first, equal)
        assertNotEquals(first, notEqual)
        assertNotEquals(first, Object())
    }

    /**
     * Tests for the hashCode method.
     */
    @Test
    fun testHashCode() {
        val first = CPlayer(validAudioFile)
        val equal = CPlayer(validAudioFile)

        val notEqual = CPlayer(validAudioFile).addOnCompletionCallback { Thread.sleep(0) }

        assertEquals(first.hashCode(), equal.hashCode())
        assertNotEquals(first.hashCode(), notEqual.hashCode())
        // no direct tests since not a stable method
    }

    /**
     * Test for the toString method.
     */
    @Test
    fun testToString() {
        val first = CPlayer(validAudioFile)
        val notEqual = CPlayer(validAudioFile).addOnCompletionCallback(object : CyderRunnable {
            override fun run() {
                Thread.sleep(0)
            }

            override fun toString(): String {
                return "CustomRunnable with sleep(0)"
            }
        })

        assertEquals(
            "AudioPlayer{audioFile=" + validAudioFile.absolutePath
                    + ", player=null, onCompletionCallbacks=[], canceled=false, playing=false}", first.toString()
        )
        assertEquals(
            "AudioPlayer{audioFile=" + validAudioFile.absolutePath
                    + ", player=null, onCompletionCallbacks=[CustomRunnable with sleep(0)],"
                    + " canceled=false, playing=false}", notEqual.toString()
        )
    }
}