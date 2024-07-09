package com.github.natche.cyderutils.bounds;

import com.google.errorprone.annotations.Immutable;

/** An HTML formatted string. */
@Immutable
public final class HtmlString extends StringContainer {
    /**
     * Constructs a new html string.
     *
     * @param containedString the contained string
     */
    public HtmlString(String containedString) {
        super(containedString);
    }
}
