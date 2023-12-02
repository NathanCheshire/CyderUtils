package cyder.network.ipdataco;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.gson.annotations.SerializedName;

@Immutable
public final class IpData {
    private final String ip;
    @SerializedName("is_eu")
    private final boolean isEu;
    private final String city;
    private final String region;
    @SerializedName("region_code")
    private final String regionCode;
    @SerializedName("region_type")
    private final String regionType;
    @SerializedName("country_name")
    private final String countryName;
    @SerializedName("country_code")
    private final String countryCode;
    @SerializedName("continent_name")
    private final String continentName;
    @SerializedName("continent_code")
    private final String continentCode;
    private final double latitude;
    private final double longitude;
    private final String postal;
    @SerializedName("calling_code")
    private final String callingCode;
    private final String flag;
    @SerializedName("emoji_flag")
    private final String emojiFlag;
    @SerializedName("emoji_unicode")
    private final String emojiUnicode;
    private final ASN asn;
    private final ImmutableList<Language> languages;
    private final Currency currency;
    @SerializedName("time_zone")
    private final TimeZone timeZone;
    private final Threat threat;
    private final int count;

    // Constructor and Getters
}

