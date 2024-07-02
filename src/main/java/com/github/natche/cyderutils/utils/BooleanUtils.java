package com.github.natche.cyderutils.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.strings.StringUtil;

/** Utilities related to booleans. */
public final class BooleanUtils {
    /** The list of strings indicating a negative intent. */
    private static final ImmutableList<String> falseStrings = ImmutableList.of(
            "false",
            "no",
            "not",
            "nein",
            "kein",
            "keine",
            "keinen",
            "0",
            "nil",
            "nill"
    );

    /** The list of strings indicating a positive intent. */
    private static final ImmutableList<String> trueStrings = ImmutableList.of(
            "true",
            "yes",
            "sure",
            "ya",
            "ja",
            "sicher",
            "gewiss",
            "klar",
            "1"
    );

    /** Suppress default constructor. */
    private BooleanUtils() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns whether the provided string is indicative of a positive intent.
     *
     * @param input the input string
     * @return whether the provided string is indicative of a positive intent
     */
    public static boolean isTrue(String input) {
        Preconditions.checkNotNull(input);

        return StringUtil.in(input, true, trueStrings);
    }

    /**
     * Returns whether the provided string is indicative of a negative intent.
     *
     * @param input the input string
     * @return whether the provided string is indicative of a negative intent
     */
    public static boolean isFalse(String input) {
        Preconditions.checkNotNull(input);

        return StringUtil.in(input, true, falseStrings);
    }
}
