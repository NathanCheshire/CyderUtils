package com.github.natche.cyderutils.constants;

import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;

/** Urls used throughout Cyder. */
public final class CyderUrls {
    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private CyderUrls() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /** The wikipedia query base. */
    public static final String WIKIPEDIA_SUMMARY_BASE = "https://en.wikipedia.org/w/api.php?format=json&action=query";

    /** The header for individual YouTube videos without their uuid. */
    public static final String YOUTUBE_VIDEO_HEADER = "https://www.youtube.com/watch?v=";
}
