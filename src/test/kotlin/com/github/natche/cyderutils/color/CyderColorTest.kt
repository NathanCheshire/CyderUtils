package com.github.natche.cyderutils.color

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/** Tests for [CyderColor]s. */
class CyderColorTest {
    /** Tests for construction of CyderColors. */
    @Test
    fun testConstruction() {

    }

    /**
     * Tests for the set opacity method.
     */
    @Test
    fun testSetOpacity() {
    }

    /** Tests for the get inverse method. */
    @Test
    fun testGetInverse() {
    }

    /** Tests for the get grayscale method. */
    @Test
    fun testGetGrayscale() {
    }

    /** Tests for the merge colors method. */
    @Test
    fun testMerge() {
    }

    /** Tests for the get transition colors method. */
    @Test
    fun testGetTransitionColors() {
    }

    /** Tests for the parse color from hex method. */
    @Test
    fun testParseColorFromHex() {
        assertThrows(NullPointerException::class.java) { CyderColor.parseColorFromHex(null) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor.parseColorFromHex("") }
        assertThrows(IllegalArgumentException::class.java) { CyderColor.parseColorFromHex("   ") }
        assertThrows(IllegalArgumentException::class.java) { CyderColor.parseColorFromHex("D") }
        assertThrows(IllegalArgumentException::class.java) { CyderColor.parseColorFromHex("#D") }
        assertThrows(IllegalArgumentException::class.java) { CyderColor.parseColorFromHex("DD") }
        assertThrows(IllegalArgumentException::class.java) { CyderColor.parseColorFromHex("#DD") }
        assertThrows(IllegalArgumentException::class.java) { CyderColor.parseColorFromHex("DDDD") }
        assertThrows(IllegalArgumentException::class.java) { CyderColor.parseColorFromHex("#DDDD") }
        assertThrows(IllegalArgumentException::class.java) { CyderColor.parseColorFromHex("DDDDD") }
        assertThrows(IllegalArgumentException::class.java) { CyderColor.parseColorFromHex("#DDDDD") }

        assertDoesNotThrow { CyderColor.parseColorFromHex("DDD") }
        assertDoesNotThrow { CyderColor.parseColorFromHex("#DDD") }
        assertDoesNotThrow { CyderColor.parseColorFromHex("DDDDDD") }
        assertDoesNotThrow { CyderColor.parseColorFromHex("#DDDDDD") }

        val from = CyderColor.parseColorFromHex("123")
        assertEquals(0x11, from.red)
        assertEquals(0x22, from.green)
        assertEquals(0x33, from.blue)
    }
}