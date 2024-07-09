package com.github.natche.cyderutils.audio.ffmpeg

import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/** Tests for the [FfmpegCommandBuilder]. */
class FfmpegCommandBuilderTest {
    /** Tests the default constructor. */
    @Test
    fun testConstruction() {
        assertDoesNotThrow { FfmpegCommandBuilder() }
        val builder = FfmpegCommandBuilder()
        assertEquals("ffmpeg", builder.build())
    }

    /** Tests the construction from an initial part. */
    @Test
    fun testConstructionFromInitialPart() {
        assertEquals("ffmpeg", FfmpegCommandBuilder().build())
        assertEquals("ffprobe", FfmpegCommandBuilder(FfmpegArgument.FFPROBE).build())
        assertEquals("map", FfmpegCommandBuilder(FfmpegArgument.MAP).build())
    }

    /** Tests the construction from a list. */
    @Test
    fun testConstructionFromList() {
        val list = ImmutableList.of("one", "two", "three")
        assertEquals("ffmpeg one two three", FfmpegCommandBuilder(list).build())
    }

    /** Tests adding an argument. */
    @Test
    fun testAddArgument() {
        val builder = FfmpegCommandBuilder()
        assertEquals("ffmpeg", builder.build())
        builder.addArgument("arg")
        assertEquals("ffmpeg arg", builder.build())
        builder.addArgument("another")
        assertEquals("ffmpeg arg another", builder.build())
    }

    /** Tests adding arguments. */
    @Test
    fun testAddArguments() {
        val builder = FfmpegCommandBuilder()
        assertEquals("ffmpeg", builder.build())
        builder.addArgument("map", "value")
        assertEquals("ffmpeg map value", builder.build())
    }

    /** Tests for the addAllArguments method. */
    @Test
    fun testAddAllArguments() {
        val builder = FfmpegCommandBuilder()
        assertEquals("ffmpeg", builder.build())
        val list = ImmutableList.of("one", "two", "three")
        builder.addAllArguments(list)
        assertEquals("ffmpeg one two three", builder.build())
    }

    /** Tests for the list method. */
    @Test
    fun testList() {
        val builder = FfmpegCommandBuilder()
        assertEquals("ffmpeg", builder.build())
        val list = ImmutableList.of("one", "two", "three")
        builder.addAllArguments(list)
        assertEquals(ImmutableList.of("ffmpeg", "one", "two", "three"), builder.list())
    }

    /** Test for the toString method. */
    @Test
    fun testToString() {
        val firstBuilder = FfmpegCommandBuilder(FfmpegArgument.FFMPEG)
            .addArgument("argument")

        val secondBuilder = FfmpegCommandBuilder(FfmpegArgument.FFPROBE)
            .addArgument(FfmpegArgument.MAP.argument)
            .addArgument(FfmpegArgument.AUDIO_CODE_C.argument)
            .addArgument(FfmpegArgument.INPUT.argument)

        assertEquals("FfmpegCommandBuilder{commandParts=[ffmpeg, argument]}", firstBuilder.toString())
        assertEquals("FfmpegCommandBuilder{commandParts=[ffprobe, -map, -c:a, -i]}", secondBuilder.toString())
    }

    /** Tests for the hashcode method. */
    @Test
    fun testHashCode() {
        val first = FfmpegCommandBuilder(FfmpegArgument.FFPROBE)
            .addArgument(FfmpegArgument.MAP.argument)
            .addArgument(FfmpegArgument.AUDIO_CODE_C.argument)
            .addArgument(FfmpegArgument.INPUT.argument)
        val equalFirst = FfmpegCommandBuilder(FfmpegArgument.FFPROBE)
            .addArgument(FfmpegArgument.MAP.argument)
            .addArgument(FfmpegArgument.AUDIO_CODE_C.argument)
            .addArgument(FfmpegArgument.INPUT.argument)

        val second = FfmpegCommandBuilder(FfmpegArgument.FFMPEG)
            .addArgument(FfmpegArgument.MAP.argument)
            .addArgument(FfmpegArgument.LOG_LEVEL.argument)
            .addArgument(FfmpegArgument.INPUT.argument)

        assertEquals(-2109989217, first.hashCode())
        assertEquals(equalFirst.hashCode(), first.hashCode())
        assertEquals(350673534, second.hashCode())
    }

    /** Tests for the equals method. */
    @Test
    fun testEquals() {
        val first = FfmpegCommandBuilder(FfmpegArgument.FFPROBE)
            .addArgument(FfmpegArgument.MAP.argument)
            .addArgument(FfmpegArgument.AUDIO_CODE_C.argument)
            .addArgument(FfmpegArgument.INPUT.argument)
        val equalFirst = FfmpegCommandBuilder(FfmpegArgument.FFPROBE)
            .addArgument(FfmpegArgument.MAP.argument)
            .addArgument(FfmpegArgument.AUDIO_CODE_C.argument)
            .addArgument(FfmpegArgument.INPUT.argument)

        val second = FfmpegCommandBuilder(FfmpegArgument.FFMPEG)
            .addArgument(FfmpegArgument.MAP.argument)
            .addArgument(FfmpegArgument.LOG_LEVEL.argument)
            .addArgument(FfmpegArgument.INPUT.argument)

        assertEquals(first, first)
        assertEquals(first, equalFirst)
        assertNotEquals(first, second)
        assertNotEquals(first, Object())
    }
}