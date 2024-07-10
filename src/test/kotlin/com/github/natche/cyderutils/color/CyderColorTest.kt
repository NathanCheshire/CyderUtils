package com.github.natche.cyderutils.color

import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/** Tests for [CyderColor]s. */
class CyderColorTest {
    /** Tests for construction of CyderColors. */
    @Test
    fun testConstruction() {
        assertThrows(IllegalArgumentException::class.java) { CyderColor(-10) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor(-1) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor(256) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor(300) }
        assertDoesNotThrow { CyderColor(0) }
        assertDoesNotThrow { CyderColor(128) }
        assertDoesNotThrow { CyderColor(255) }

        assertThrows(IllegalArgumentException::class.java) { CyderColor(-10, -10, -10) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor(-1, -1, -1) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor(0, -1, -1) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor(0, 0, -1) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor(255, 255, 256) }
        assertDoesNotThrow { CyderColor(0, 0, 0) }
        assertDoesNotThrow { CyderColor(255, 255, 255) }

        assertThrows(IllegalArgumentException::class.java) { CyderColor(-10, -10, -10, -10) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor(-1, -1, -1, -1) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor(0, -1, -1, -1) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor(0, 0, 0, -1) }
        assertThrows(IllegalArgumentException::class.java) { CyderColor(255, 255, 255, 256) }
        assertDoesNotThrow { CyderColor(0, 0, 0, 0) }
        assertDoesNotThrow { CyderColor(255, 255, 255, 255) }

        val string: String? = null
        assertThrows(NullPointerException::class.java) { CyderColor(string) }

        val color: CyderColor? = null
        assertThrows(java.lang.NullPointerException::class.java) { CyderColor(color) }
    }

    /**
     * Tests for the set opacity method.
     */
    @Test
    fun testSetOpacity() {
        val oneTwoEight = CyderColor(CyderColors.navy)
        assertEquals(CyderColor(26, 32, 51, 128), oneTwoEight.withOpacity(128))

        val zero = CyderColor(CyderColors.navy)
        assertEquals(CyderColor(26, 32, 51, 0), zero.withOpacity(0))
    }

    /** Tests for the get inverse method. */
    @Test
    fun testGetInverse() {
        assertEquals(CyderColor(255), CyderColor(0).inverse)
        assertEquals(CyderColor(0), CyderColor(255).inverse)
        assertEquals(CyderColor(127), CyderColor(128).inverse)
        assertEquals(CyderColor(128), CyderColor(127).inverse)

        assertEquals(CyderColor(231, 199, 157), CyderColor(24, 56, 98).inverse)
        assertEquals(CyderColor(229, 223, 204), CyderColor(CyderColors.navy).inverse)
        assertEquals(CyderColor(19, 191, 133), CyderColor(CyderColors.regularPink).inverse)
    }

    /** Tests for the get grayscale method. */
    @Test
    fun testGetGrayscale() {
        assertEquals(CyderColor(32), CyderColor(CyderColors.navy).grayscale)
        assertEquals(CyderColor(105), CyderColor(CyderColors.regularPink).grayscale)
        assertEquals(CyderColor(0), CyderColor(CyderColor(0)).grayscale)
        assertEquals(CyderColor(128), CyderColor(CyderColor(128)).grayscale)
        assertEquals(CyderColor(255), CyderColor(CyderColor(255)).grayscale)
    }

    /** Tests for the merge colors method. */
    @Test
    fun testMerge() {
        val white = CyderColor(0)
        val black = CyderColor(0xFF)

        assertThrows(NullPointerException::class.java) { white.merge(null) }
        // Should be communicative
        assertEquals(CyderColor(0x7F), white.merge(black))
        assertEquals(CyderColor(0x7F), black.merge(white))

        assertEquals(
            CyderColor(131, 48, 86),
            CyderColor(CyderColors.navy).merge(CyderColors.regularPink)
        )
        assertEquals(
            CyderColor(131, 48, 86),
            CyderColor(CyderColors.regularPink).merge(CyderColors.navy)
        )
    }

    /** Tests for the get transition colors method. */
    @Test
    fun testGetTransitionColors() {
        val cc = CyderColor(0)
        assertThrows(NullPointerException::class.java)
        { cc.getTransitionColors(null, 10) }
        assertThrows(IllegalArgumentException::class.java)
        { cc.getTransitionColors(cc, 10) }
        assertThrows(IllegalArgumentException::class.java)
        { cc.getTransitionColors(CyderColor(0xFF), -1) }
        assertThrows(IllegalArgumentException::class.java)
        { cc.getTransitionColors(CyderColor(0xFF), 0) }
        assertThrows(IllegalArgumentException::class.java)
        { cc.getTransitionColors(CyderColor(0xFF), 256) }

        assertDoesNotThrow { cc.getTransitionColors(CyderColor(0xFF), 5) }
        assertDoesNotThrow { cc.getTransitionColors(CyderColor(0xFF), 255) }

        assertEquals(
            ImmutableList.of(
                CyderColor(0), CyderColor(25),
                CyderColor(51), CyderColor(76),
                CyderColor(102), CyderColor(127),
                CyderColor(153), CyderColor(178),
                CyderColor(204), CyderColor(229)
            ),
            cc.getTransitionColors(CyderColor(0xFF), 10)
        )

        assertEquals(
            ImmutableList.of(
                CyderColor(26, 32, 51),
                CyderColor(47, 35, 58),
                CyderColor(68, 38, 65),
                CyderColor(89, 41, 72),
                CyderColor(110, 44, 79),
                CyderColor(131, 48, 86),
                CyderColor(152, 51, 93),
                CyderColor(173, 54, 100),
                CyderColor(194, 57, 107),
                CyderColor(215, 60, 114)
            ),
            CyderColor(CyderColors.navy).getTransitionColors(CyderColors.regularPink, 10)
        )
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