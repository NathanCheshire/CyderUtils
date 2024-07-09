package com.github.natche.cyderutils.bounds;

import com.google.errorprone.annotations.Immutable;

/** A plain, non-html formatted string. */
@Immutable
public final class PlainString extends StringContainer {
    /**
     * Constructs a new plain string.
     *
     * @param containedString the contained string
     */
    public PlainString(String containedString) {
        super(containedString);
    }
}
