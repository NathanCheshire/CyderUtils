package com.github.natche.cyderutils.font;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import java.awt.*;

/** A builder for a font. */
public class FontBuilder {
    /** The default font metric for built fonts. */
    public static final int DEFAULT_METRIC = Font.BOLD;

    /** The name of the font. */
    private final String name;

    /**
     * The metric of the font, that of {@link Font#PLAIN}, {@link Font#BOLD},
     * {@link Font#ITALIC}, or a combination of them.
     */
    private int metric = DEFAULT_METRIC;

    /** The size of the font. */
    private int size;

    /**
     * Constructs a new font builder.
     *
     * @param name the name of the font
     */
    public FontBuilder(String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());

        this.name = name;
    }

    /**
     * The valid font metric range.
     * This includes the following:
     * <ul>
     *     <li>{@link Font#PLAIN}</li>
     *     <li>{@link Font#BOLD}</li>
     *     <li>{@link Font#ITALIC}</li>
     *     <li>{@link Font#BOLD} + {@link Font#ITALIC}</li>
     * </ul>
     */
    private static final Range<Integer> FONT_METRIC_RANGE = Range.closed(0, 3);

    /**
     * The font metric, that of {@link Font#PLAIN}, {@link Font#BOLD},
     * {@link Font#ITALIC}, or a combination of them.
     *
     * @param metric font metric
     * @return this builder
     */
    public FontBuilder setMetric(int metric) {
        Preconditions.checkArgument(FONT_METRIC_RANGE.contains(metric));

        this.metric = metric;
        return this;
    }

    /**
     * Sets the size of this font.
     *
     * @param size the size of this font
     * @return this builder
     */
    public FontBuilder setSize(int size) {
        this.size = size;
        return this;
    }

    /**
     * Generates and returns a font based on the internally set
     * values of this builder.
     *
     * @return a new font
     */
    @SuppressWarnings("MagicConstant") /* font metric checked */
    public Font generate() {
        return new Font(name, metric, size);
    }
}
