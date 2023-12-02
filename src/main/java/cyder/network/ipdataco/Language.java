package cyder.network.ipdataco;

import com.google.errorprone.annotations.Immutable;
import com.google.gson.annotations.SerializedName;

@Immutable
public final class Language {
    private final String name;
    @SerializedName("native")
    private final String nativeName;
    private final String code;

    // Constructor and Getters
}

