package com.github.natche.cyderutils.utils;

import com.google.common.collect.ImmutableList;

/** The primary operating systems utilized in our modern-day world. */
public enum OperatingSystem {
    /** Macintosh OS. */
    MAC("Mac", ImmutableList.of("mac")),

    /** The Windows operating system. */
    WINDOWS("Windows", ImmutableList.of("win")),

    /** Any GNU/Linux based operating system. */
    GNU_LINUX("GNU/Linux", ImmutableList.of("nix", "nux", "aix", "linux")),

    /** An indeterminable operating system. */
    UNKNOWN("Unknown", ImmutableList.of());

    private final String osName;
    private final ImmutableList<String> osPrefixes;

    OperatingSystem(String osName, ImmutableList<String> osPrefixes) {
        this.osName = osName;
        this.osPrefixes = osPrefixes;
    }

    /**
     * Returns the name of this operating system.
     *
     * @return the name of this operating system
     */
    public String getOsName() {
        return osName;
    }

    /**
     * Returns whether this is the host operating system.
     *
     * @return whether this is the host operating system
     */
    public boolean isCurrentOperatingSystem() {
        String currentOS = OsUtil.OPERATING_SYSTEM_NAME.toLowerCase();
        return osPrefixes.stream().anyMatch(currentOS::contains);
    }
}
