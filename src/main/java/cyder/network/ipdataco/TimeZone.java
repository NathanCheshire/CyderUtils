package cyder.network.ipdataco;

import com.google.errorprone.annotations.Immutable;
import com.google.gson.annotations.SerializedName;

@Immutable
public final class TimeZone {
    private final String name;
    private final String abbr;
    private final String offset;
    @SerializedName("is_dst")
    private final boolean isDst;
    @SerializedName("current_time")
    private final String currentTime;

    // Constructor and Getters
}

