package com.github.natche.cyderutils.audio.cplayer

import com.github.natche.cyderutils.utils.OsUtil
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File

/**
 * Test for the [CPlayer].
 */
class CPlayerTest
/**
 * Creates a new instance of this class for testing purposes.
 */
{
    private val validAudioFile = OsUtil.buildFile(
        "src", "test", "java", "com", "github", "natche", "cyderutils", "audio",
        "resources", "TastyCarrots.mp3"
    )

    private val invalidAudioFile = OsUtil.buildFile(
        "src", "test", "java", "com", "github", "natche", "cyderutils", "audio",
        "resources", "Nightcore.aac"
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
}