package com.github.natche.cyderutils.network.ipdataco.models;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.gson.annotations.SerializedName;
import com.github.natche.cyderutils.network.ipdataco.IpDataManager;

/** A model class for the data returned from querying ipdataco using a {@link IpDataManager}. */
@Immutable
public final class IpData {
    /** The IP address that was looked up. */
    private final String ip;

    /** Whether the country is a recognized member of the European Union. */
    @SerializedName("is_eu")
    private final boolean isEu;

    /** The name of the city from where the IP Address is located. */
    private final String city;

    /** The name of the region where the IP Address is located. */
    private final String region;

    /** ISO 3166-2 code for the region. */
    @SerializedName("region_code")
    private final String regionCode;

    /** The region type, such as state for the USA or province for Canada. */
    @SerializedName("region_type")
    private final String regionType;

    /** The name of the country where the IP Address is located. */
    @SerializedName("country_name")
    private final String countryName;

    /** The two-letter ISO 3166-1 alpha-2 code for the country. */
    @SerializedName("country_code")
    private final String countryCode;

    /** The name of the continent where the IP Address is located. */
    @SerializedName("continent_name")
    private final String continentName;

    /** The two-letter ISO 3166-1 alpha-2 code for the continent. */
    @SerializedName("continent_code")
    private final String continentCode;

    /** An approximate latitudinal location for the IP Address. */
    private final double latitude;

    /** An approximate longitudinal location for the IP Address. */
    private final double longitude;

    /** The Postal code for where the IP Address is located. */
    private final String postal;

    /** The International Calling Code for the country where the IP Address is located. */
    @SerializedName("calling_code")
    private final String callingCode;

    /** A link to a PNG/SVG file with the flag of the country where the IP Address is located. */
    private final String flag;

    /** An emoji version of the flag of the country where the IP Address is located. */
    @SerializedName("emoji_flag")
    private final String emojiFlag;

    /** The Unicode for the emoji flag. */
    @SerializedName("emoji_unicode")
    private final String emojiUnicode;

    /** The ASN object for this request. */
    private final Asn asn;

    /** The languages for this IP. */
    private final ImmutableList<Language> languages;

    /** The currency for this IP. */
    private final Currency currency;

    /** The timezone for this IP. */
    @SerializedName("time_zone")
    private final TimeZone timeZone;

    /** The threat data for this IP. */
    private final Threat threat;

    /** The total number of requests made by your API key in the last 24 hrs, updated once a minute. */
    private final int count;

    /**
     * Constructs a new IP data instance.
     *
     * @param ip            the ip address that was looked up
     * @param isEu          whether the country is a recognized member of the European Union
     * @param city          the name of the city from where the IP Address is located
     * @param region        the name of the region where the IP Address is located.
     * @param regionCode    ISO 3166-2 code for the region
     * @param regionType    the region type, such as state for the USA or province for Canada
     * @param countryName   the name of the country where the IP Address is located
     * @param countryCode   the two-letter ISO 3166-1 alpha-2 code for the country
     * @param continentName the name of the continent where the IP Address is located
     * @param continentCode the two-letter ISO 3166-1 alpha-2 code for the continent
     * @param latitude      an approximate latitudinal location for the IP Address
     * @param longitude     an approximate longitudinal location for the IP Address
     * @param postal        the Postal code for where the IP Address is located
     * @param callingCode   the International Calling Code for the country where the IP Address is located
     * @param flag          a link to a PNG/SVG file with the flag of the country where the IP Address is located
     * @param emojiFlag     an emoji version of the flag of the country where the IP Address is located
     * @param emojiUnicode  the Unicode for the emoji flag
     * @param asn           the asn object for this request
     * @param languages     the languages for this IP
     * @param currency      the currency for this IP
     * @param timeZone      the timezone for this IP
     * @param threat        the threat data for this IP
     * @param count         the total number of requests made by your API key in the last 24 hrs, updated once a minute
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if any string parameter is empty
     */
    public IpData(String ip,
                  boolean isEu,
                  String city,
                  String region,
                  String regionCode,
                  String regionType,
                  String countryName,
                  String countryCode,
                  String continentName,
                  String continentCode,
                  double latitude,
                  double longitude,
                  String postal,
                  String callingCode,
                  String flag,
                  String emojiFlag,
                  String emojiUnicode,
                  Asn asn,
                  ImmutableList<Language> languages,
                  Currency currency,
                  TimeZone timeZone,
                  Threat threat,
                  int count) {
        Preconditions.checkNotNull(ip);
        Preconditions.checkNotNull(city);
        Preconditions.checkNotNull(region);
        Preconditions.checkNotNull(regionCode);
        Preconditions.checkNotNull(regionType);
        Preconditions.checkNotNull(countryName);
        Preconditions.checkNotNull(countryCode);
        Preconditions.checkNotNull(continentName);
        Preconditions.checkNotNull(continentCode);
        Preconditions.checkNotNull(postal);
        Preconditions.checkNotNull(callingCode);
        Preconditions.checkNotNull(flag);
        Preconditions.checkNotNull(emojiFlag);
        Preconditions.checkNotNull(emojiUnicode);
        Preconditions.checkNotNull(asn);
        Preconditions.checkNotNull(languages);
        Preconditions.checkNotNull(currency);
        Preconditions.checkNotNull(timeZone);
        Preconditions.checkNotNull(threat);
        Preconditions.checkArgument(!ip.trim().isEmpty());
        Preconditions.checkArgument(!city.trim().isEmpty());
        Preconditions.checkArgument(!region.trim().isEmpty());
        Preconditions.checkArgument(!regionCode.trim().isEmpty());
        Preconditions.checkArgument(!regionType.trim().isEmpty());
        Preconditions.checkArgument(!countryName.trim().isEmpty());
        Preconditions.checkArgument(!countryCode.trim().isEmpty());
        Preconditions.checkArgument(!continentName.trim().isEmpty());
        Preconditions.checkArgument(!continentCode.trim().isEmpty());
        Preconditions.checkArgument(!postal.trim().isEmpty());
        Preconditions.checkArgument(!callingCode.trim().isEmpty());
        Preconditions.checkArgument(!flag.trim().isEmpty());
        Preconditions.checkArgument(!emojiFlag.trim().isEmpty());
        Preconditions.checkArgument(!emojiUnicode.trim().isEmpty());

        this.ip = ip;
        this.isEu = isEu;
        this.city = city;
        this.region = region;
        this.regionCode = regionCode;
        this.regionType = regionType;
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.continentName = continentName;
        this.continentCode = continentCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.postal = postal;
        this.callingCode = callingCode;
        this.flag = flag;
        this.emojiFlag = emojiFlag;
        this.emojiUnicode = emojiUnicode;
        this.asn = asn;
        this.languages = languages;
        this.currency = currency;
        this.timeZone = timeZone;
        this.threat = threat;
        this.count = count;
    }

    /**
     * Returns the IP address that was looked up.
     *
     * @return the IP address that was looked up
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns whether the country is a recognized member of the European Union.
     *
     * @return whether the country is a recognized member of the European Union
     */
    public boolean isEu() {
        return isEu;
    }

    /**
     * Returns the name of the city from where the IP Address is located.
     *
     * @return the name of the city from where the IP Address is located
     */
    public String getCity() {
        return city;
    }

    /**
     * Returns the name of the region where the IP Address is located.
     *
     * @return the name of the region where the IP Address is located
     */
    public String getRegion() {
        return region;
    }

    /**
     * Returns the ISO 3166-2 code for the region.
     *
     * @return the ISO 3166-2 code for the region
     */
    public String getRegionCode() {
        return regionCode;
    }

    /**
     * Returns the region type, such as state for the USA or province for Canada.
     *
     * @return the region type, such as state for the USA or province for Canada
     */
    public String getRegionType() {
        return regionType;
    }

    /**
     * Returns the name of the country where the IP Address is located.
     *
     * @return the name of the country where the IP Address is located
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * Returns the two-letter ISO 3166-1 alpha-2 code for the country.
     *
     * @return the two-letter ISO 3166-1 alpha-2 code for the country
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Returns the name of the continent where the IP Address is located.
     *
     * @return the name of the continent where the IP Address is located
     */
    public String getContinentName() {
        return continentName;
    }

    /**
     * Returns the two-letter ISO 3166-1 alpha-2 code for the continent.
     *
     * @return the two-letter ISO 3166-1 alpha-2 code for the continent
     */
    public String getContinentCode() {
        return continentCode;
    }

    /**
     * Returns an approximate latitudinal location for the IP Address.
     *
     * @return an approximate latitudinal location for the IP Address
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns an approximate longitudinal location for the IP Address.
     *
     * @return an approximate longitudinal location for the IP Address
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Returns the Postal code for where the IP Address is located.
     *
     * @return the Postal code for where the IP Address is located
     */
    public String getPostal() {
        return postal;
    }

    /**
     * Returns the International Calling Code for the country where the IP Address is located.
     *
     * @return the International Calling Code for the country where the IP Address is located
     */
    public String getCallingCode() {
        return callingCode;
    }

    /**
     * Returns a link to a PNG/SVG file with the flag of the country where the IP Address is located.
     *
     * @return a link to a PNG/SVG file with the flag of the country where the IP Address is located
     */
    public String getFlag() {
        return flag;
    }

    /**
     * Returns an emoji version of the flag of the country where the IP Address is located.
     *
     * @return an emoji version of the flag of the country where the IP Address is located
     */
    public String getEmojiFlag() {
        return emojiFlag;
    }

    /**
     * Returns the Unicode for the emoji flag.
     *
     * @return the Unicode for the emoji flag
     */
    public String getEmojiUnicode() {
        return emojiUnicode;
    }

    /**
     * Returns the ASN object for this request.
     *
     * @return the ASN object for this request
     */
    public Asn getAsn() {
        return asn;
    }

    /**
     * Returns the languages for this IP.
     *
     * @return the languages for this IP
     */
    public ImmutableList<Language> getLanguages() {
        return languages;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "IpData{"
                + "ip=\"" + ip + "\""
                + ", isEu=" + isEu
                + ", city=\"" + city + "\""
                + ", region=\"" + region + "\""
                + ", regionCode=\"" + regionCode + "\""
                + ", regionType=\"" + regionType + "\""
                + ", countryName=\"" + countryName + "\""
                + ", countryCode=\"" + countryCode + "\""
                + ", continentName=\"" + continentName + "\""
                + ", continentCode=\"" + continentCode + "\""
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", postal=\"" + postal + "\""
                + ", callingCode=\"" + callingCode + "\""
                + ", flag=\"" + flag + "\""
                + ", emojiFlag=\"" + emojiFlag + "\""
                + ", emojiUnicode=\"" + emojiUnicode + "\""
                + ", asn=" + asn
                + ", languages=" + languages
                + ", currency=" + currency
                + ", timeZone=" + timeZone
                + ", threat=" + threat
                + ", count=" + count
                + "}";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + Boolean.hashCode(isEu);
        result = 31 * result + city.hashCode();
        result = 31 * result + region.hashCode();
        result = 31 * result + regionCode.hashCode();
        result = 31 * result + regionType.hashCode();
        result = 31 * result + countryName.hashCode();
        result = 31 * result + countryCode.hashCode();
        result = 31 * result + continentName.hashCode();
        result = 31 * result + continentCode.hashCode();
        result = 31 * result + Double.hashCode(latitude);
        result = 31 * result + Double.hashCode(longitude);
        result = 31 * result + postal.hashCode();
        result = 31 * result + callingCode.hashCode();
        result = 31 * result + flag.hashCode();
        result = 31 * result + emojiFlag.hashCode();
        result = 31 * result + emojiUnicode.hashCode();
        result = 31 * result + asn.hashCode();
        result = 31 * result + languages.hashCode();
        result = 31 * result + currency.hashCode();
        result = 31 * result + timeZone.hashCode();
        result = 31 * result + threat.hashCode();
        result = 31 * result + Integer.hashCode(count);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof IpData)) {
            return false;
        }

        IpData other = (IpData) o;
        return ip.equals(other.ip)
                && isEu == other.isEu
                && city.equals(other.city)
                && region.equals(other.region)
                && regionCode.equals(other.regionCode)
                && regionType.equals(other.regionType)
                && countryName.equals(other.countryName)
                && countryCode.equals(other.countryCode)
                && continentName.equals(other.continentName)
                && continentCode.equals(other.continentCode)
                && Double.compare(latitude, other.latitude) == 0
                && Double.compare(longitude, other.longitude) == 0
                && postal.equals(other.postal)
                && callingCode.equals(other.callingCode)
                && flag.equals(other.flag)
                && emojiFlag.equals(other.emojiFlag)
                && emojiUnicode.equals(other.emojiUnicode)
                && asn.equals(other.asn)
                && languages.equals(other.languages)
                && currency.equals(other.currency)
                && timeZone.equals(other.timeZone)
                && threat.equals(other.threat)
                && count == other.count;
    }
}

