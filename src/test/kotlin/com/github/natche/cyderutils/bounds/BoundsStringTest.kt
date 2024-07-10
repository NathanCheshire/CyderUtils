package com.github.natche.cyderutils.bounds

import com.github.natche.cyderutils.files.FileUtil
import com.github.natche.cyderutils.font.CyderFonts
import com.github.natche.cyderutils.subroutines.NecessarySubroutines
import com.github.natche.cyderutils.utils.ArrayUtil
import com.github.natche.cyderutils.utils.OsUtil
import com.github.natche.cyderutils.utils.StaticUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.awt.Font
import java.awt.GraphicsEnvironment

/** Tests for [BoundsString]s. */
class BoundsStringTest {
    /**
     * Tests for the builder mutators.
     */
    @Test
    fun testBuilder() {
        assertThrows(NullPointerException::class.java) { BoundsString.Builder(null) }

        val bs = BoundsString.Builder("")

        assertThrows(NullPointerException::class.java) { bs.setFont(null) }
        assertDoesNotThrow { bs.setFont(CyderFonts.DEFAULT_FONT) }

        assertThrows(IllegalArgumentException::class.java) { bs.setLinePadding(-10) }
        assertThrows(IllegalArgumentException::class.java) { bs.setLinePadding(-1) }
        assertDoesNotThrow { bs.setLinePadding(0) }
        assertDoesNotThrow { bs.setLinePadding(1) }
        assertDoesNotThrow { bs.setLinePadding(10) }

        assertThrows(IllegalArgumentException::class.java) { bs.setMaxWidth(-1) }
        assertThrows(IllegalArgumentException::class.java) { bs.setMaxWidth(0) }
        assertDoesNotThrow { bs.setMaxWidth(1) }

        assertThrows(IllegalArgumentException::class.java) { bs.setMaxHeight(-1) }
        assertThrows(IllegalArgumentException::class.java) { bs.setMaxHeight(0) }
        assertDoesNotThrow { bs.setMaxHeight(1) }
    }

    /** Tests for construction of [BoundsString]s without HTML styling. */
    @Test
    fun testWithoutHtmlStyling() {
        var bs = BoundsString.Builder("Some text without HTML styling and even more text after that")
            .setFont(CyderFonts.DEFAULT_FONT_SMALL)
            .setMaxWidth(400)
            .build()

        assertEquals(363.04296875, bs.width)
        assertEquals(56.8203125, bs.height)
        assertEquals("<html>Some text without HTML styling<br/>and even more text after that</html>", bs.text)

        bs = BoundsString.Builder("The quick brown fox jumps over the lazy dogs"
                + " The quick brown fox jumps over the lazy dogs"
                + " The quick brown fox jumps over the lazy dogs"
                + " The quick brown fox jumps over the lazy dogs")
            .setFont(CyderFonts.DEFAULT_FONT_SMALL)
            .setMaxWidth(400)
            .build()

        assertEquals(380.05859375, bs.width)
        assertEquals(180.4609375, bs.height)
        assertEquals("<html>The quick brown fox jumps over<br/>"
                + "the lazy dogs The quick brown<br/>"
                + "fox jumps over the lazy dogs The<br/>"
                + "quick brown fox jumps over the<br/>"
                + "lazy dogs The quick brown fox<br/>"
                + "jumps over the lazy dogs</html>", bs.text)

        bs = BoundsString.Builder("The quick brown fox jumps over the lazy dogs"
                + " The quick brown fox jumps over the lazy dogs"
                + " The quick brown fox jumps over the lazy dogs"
                + " The quick brown fox jumps over the lazy dogs")
            .setFont(CyderFonts.DEFAULT_FONT_LARGE)
            .setMaxWidth(800)
            .build()

        assertEquals(771.4697265625, bs.width)
        assertEquals(226.103515625, bs.height)
        assertEquals("<html>The quick brown fox jumps over the lazy<br/>"
                + "dogs The quick brown fox jumps over the<br/>"
                + "lazy dogs The quick brown fox jumps over<br/>"
                + "the lazy dogs The quick brown fox jumps<br/>"
                + "over the lazy dogs</html>", bs.text)
    }

    /**
     * Tests for construction of [BoundsString]s with HTML styling.
     */
    @Test
    fun testWithHtmlStyling() {
        val bs = BoundsString.Builder("<html>Some text with HTML<br/>"
                + " styling and even <div>more</div> text after that</html>")
            .setFont(CyderFonts.DEFAULT_FONT_SMALL)
            .setMaxWidth(400)
            .build()

        assertEquals(240.92578125, bs.width)
        assertEquals(87.73046875, bs.height)
        assertEquals("<html>Some text with HTML<br/> styling"
                + " and even<br/><div>more</div> text after that</html>", bs.text)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll(): Unit {
            val fontFiles = OsUtil.buildFile("static", "fonts").listFiles()
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()

            for (fontFile in fontFiles!!) {
                try {
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}