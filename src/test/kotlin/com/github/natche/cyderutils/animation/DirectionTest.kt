package com.github.natche.cyderutils.animation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/** Tests for [Direction]s. */
class DirectionTest {
    /** Tests for the get name function. */
    @Test
    fun testGetName() {
        assertEquals("BOTTOM", Direction.BOTTOM.name)
        assertEquals("TOP", Direction.TOP.name)
        assertEquals("LEFT", Direction.LEFT.name)
        assertEquals("RIGHT", Direction.RIGHT.name)
    }

    /** Tests for the is vertical method. */
    @Test
    fun testIsVertical() {
        assertTrue(Direction.BOTTOM.isVertical)
        assertTrue(Direction.TOP.isVertical)
        assertFalse(Direction.LEFT.isVertical)
        assertFalse(Direction.RIGHT.isVertical)

        assertThrows(NullPointerException::class.java) { Direction.isVertical(null) }
    }

    /** Tests for the is horizontal method. */
    @Test
    fun testIsHorizontal() {
        assertFalse(Direction.BOTTOM.isHorizontal)
        assertFalse(Direction.TOP.isHorizontal)
        assertTrue(Direction.LEFT.isHorizontal)
        assertTrue(Direction.RIGHT.isHorizontal)

        assertThrows(NullPointerException::class.java) { Direction.isVertical(null) }
    }
}