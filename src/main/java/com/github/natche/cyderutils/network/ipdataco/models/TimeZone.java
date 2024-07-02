package com.github.natche.cyderutils.network.ipdataco.models;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import com.google.gson.annotations.SerializedName;

/** A TimeZone object containing timezone data adjusted for DST where applicable. */
@Immutable
public final class TimeZone {
    /** The name of the TimeZone. */
    private final String name;

    /** The abbreviation of the timezone. */
    @SerializedName("abbr")
    private final String abbreviation;

    /** The UTC offset of the timezone. */
    private final String offset;

    /** Whether DST has been accounted for. */
    @SerializedName("is_dst")
    private final boolean isDst;

    /** The current time in the timezone accounting for DST. */
    @SerializedName("current_time")
    private final String currentTime;

    /**
     * Constructs a new time zone object.
     *
     * @param name         the name of the TimeZone
     * @param abbreviation the abbreviation of the timezone
     * @param offset       the UTC offset of the timezone
     * @param isDst        whether DST has been accounted for
     * @param currentTime  the current time in the timezone accounting for DST
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if any string parameter is empty
     */
    public TimeZone(String name, String abbreviation, String offset, boolean isDst, String currentTime) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(abbreviation);
        Preconditions.checkNotNull(offset);
        Preconditions.checkNotNull(currentTime);
        Preconditions.checkArgument(!name.trim().isEmpty());
        Preconditions.checkArgument(!abbreviation.trim().isEmpty());
        Preconditions.checkArgument(!offset.trim().isEmpty());
        Preconditions.checkArgument(!currentTime.trim().isEmpty());

        this.name = name;
        this.abbreviation = abbreviation;
        this.offset = offset;
        this.isDst = isDst;
        this.currentTime = currentTime;
    }

    /**
     * Returns the name of the TimeZone.
     *
     * @return the name of the TimeZone
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the abbreviation of the timezone.
     *
     * @return the abbreviation of the timezone
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Returns the UTC offset of the timezone.
     *
     * @return the UTC offset of the timezone
     */
    public String getOffset() {
        return offset;
    }

    /**
     * Returns whether DST has been accounted for.
     *
     * @return whether DST has been accounted for
     */
    public boolean isDst() {
        return isDst;
    }

    /**
     * Returns the current time in the timezone accounting for DST.
     *
     * @return the current time in the timezone accounting for DST
     */
    public String getCurrentTime() {
        return currentTime;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "TimeZone{"
                + "name=\"" + name + "\""
                + ", abbreviation=\"" + abbreviation + "\""
                + ", offset=\"" + offset + "\""
                + ", isDst=" + isDst
                + ", currentTime=\"" + currentTime + "\""
                + "}";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = name.hashCode();
        ret = 31 * ret + abbreviation.hashCode();
        ret = 31 * ret + offset.hashCode();
        ret = 31 * ret + Boolean.hashCode(isDst);
        ret = 31 * ret + currentTime.hashCode();
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof TimeZone)) {
            return false;
        }

        TimeZone other = (TimeZone) o;
        return name.equals(other.name)
                && abbreviation.equals(other.abbreviation)
                && offset.equals(other.offset)
                && isDst == other.isDst
                && currentTime.equals(other.currentTime);
    }
}

