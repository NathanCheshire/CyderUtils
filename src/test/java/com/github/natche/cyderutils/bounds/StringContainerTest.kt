package com.github.natche.cyderutils.bounds

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/** Tests for [StringContainer]s. */
class StringContainerTest {
    /** Tests for construction of string containers. */
    @Test
    fun testConstruction() {
        assertThrows(NullPointerException::class.java) { PlainString(null) }
        assertThrows(NullPointerException::class.java) { HtmlString(null) }

        assertDoesNotThrow { PlainString("") }
        assertDoesNotThrow { HtmlString("") }

        assertDoesNotThrow { PlainString("Content") }
        assertDoesNotThrow { HtmlString("Content") }
    }

    /** Tests for the get contained string method. */
    @Test
    fun testGetter() {
        val plain = PlainString("Content")
        val html = HtmlString("Content")

        assertEquals("Content", plain.containedString)
        assertEquals("Content", html.containedString)
    }

    /** Tests for the hashcode method */
    @Test
    fun testHashcode() {
        val plain = PlainString("Content")
        val html = HtmlString("Content")
        val different = PlainString("Different")

        assertEquals(-1678783399, plain.hashCode())
        assertEquals(-1678783399, html.hashCode())
        assertEquals(plain.hashCode(), html.hashCode())
        assertEquals(-988319719, different.hashCode())
        assertNotEquals(plain.hashCode(), different.hashCode())
    }

    @Test
    fun testEquals() {
        val plain = PlainString("Content")
        val equal = PlainString("Content")
        val different = PlainString("Different")

        assertEquals(plain, plain)
        assertEquals(plain, equal)
        assertNotEquals(equal, different)
        assertNotEquals(equal, Object())
    }

    /** Test for the to string method. */
    @Test
    fun testToString() {
        assertEquals("PlainString{containedString=\"Plain\"}", PlainString("Plain").toString())
        assertEquals("HtmlString{containedString=\"HTML\"}", HtmlString("HTML").toString())
    }
}