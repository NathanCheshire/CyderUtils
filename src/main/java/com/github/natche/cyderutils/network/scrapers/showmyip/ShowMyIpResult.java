package com.github.natche.cyderutils.network.scrapers.showmyip;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import com.github.natche.cyderutils.network.scrapers.ScraperResult;

/** Represents the result of an IP information lookup from showmyip.com. */
@Immutable
public final class ShowMyIpResult implements ScraperResult {
    private final String ipv4;
    private final String ipv6;
    private final String country;
    private final String region;
    private final String city;
    private final String zip;
    private final String timezone;
    private final String isp;
    private final String organization;
    private final String asNumberAndName;
    private final String userAgent;

    /**
     * Constructs a new {@code ShowMyIpResult} with the specified IP information.
     *
     * @param ipv4 the IPv4 address
     * @param ipv6 the IPv6 address
     * @param country the country
     * @param region the region
     * @param city the city
     * @param zip the ZIP code
     * @param timezone the timezone
     * @param isp the Internet Service Provider (ISP)
     * @param organization the organization
     * @param asNumberAndName the AS number and name
     * @param userAgent the user agent
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any argument is empty
     */
    public ShowMyIpResult(String ipv4,
                          String ipv6,
                          String country,
                          String region,
                          String city,
                          String zip,
                          String timezone,
                          String isp,
                          String organization,
                          String asNumberAndName,
                          String userAgent) {
        Preconditions.checkNotNull(ipv4, "IPv4 address cannot be null");
        Preconditions.checkArgument(!ipv4.isEmpty(), "IPv4 address cannot be empty");
        Preconditions.checkNotNull(ipv6, "IPv6 address cannot be null");
        Preconditions.checkArgument(!ipv6.isEmpty(), "IPv6 address cannot be empty");
        Preconditions.checkNotNull(country, "Country cannot be null");
        Preconditions.checkArgument(!country.isEmpty(), "Country cannot be empty");
        Preconditions.checkNotNull(region, "Region cannot be null");
        Preconditions.checkArgument(!region.isEmpty(), "Region cannot be empty");
        Preconditions.checkNotNull(city, "City cannot be null");
        Preconditions.checkArgument(!city.isEmpty(), "City cannot be empty");
        Preconditions.checkNotNull(zip, "ZIP code cannot be null");
        Preconditions.checkArgument(!zip.isEmpty(), "ZIP code cannot be empty");
        Preconditions.checkNotNull(timezone, "Timezone cannot be null");
        Preconditions.checkArgument(!timezone.isEmpty(), "Timezone cannot be empty");
        Preconditions.checkNotNull(isp, "ISP cannot be null");
        Preconditions.checkArgument(!isp.isEmpty(), "ISP cannot be empty");
        Preconditions.checkNotNull(organization, "Organization cannot be null");
        Preconditions.checkArgument(!organization.isEmpty(), "Organization cannot be empty");
        Preconditions.checkNotNull(asNumberAndName, "AS number and name cannot be null");
        Preconditions.checkArgument(!asNumberAndName.isEmpty(), "AS number and name cannot be empty");
        Preconditions.checkNotNull(userAgent, "User agent cannot be null");
        Preconditions.checkArgument(!userAgent.isEmpty(), "User agent cannot be empty");

        this.ipv4 = ipv4;
        this.ipv6 = ipv6;
        this.country = country;
        this.region = region;
        this.city = city;
        this.zip = zip;
        this.timezone = timezone;
        this.isp = isp;
        this.organization = organization;
        this.asNumberAndName = asNumberAndName;
        this.userAgent = userAgent;
    }

    /**
     * Returns the IPv4 address.
     *
     * @return the IPv4 address
     */
    public String getIpv4() {
        return ipv4;
    }

    /**
     * Returns the IPv6 address.
     *
     * @return the IPv6 address
     */
    public String getIpv6() {
        return ipv6;
    }

    /**
     * Returns the country.
     *
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Returns the region.
     *
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Returns the city.
     *
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * Returns the ZIP code.
     *
     * @return the ZIP code
     */
    public String getZip() {
        return zip;
    }

    /**
     * Returns the timezone.
     *
     * @return the timezone
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Returns the Internet Service Provider (ISP).
     *
     * @return the ISP
     */
    public String getIsp() {
        return isp;
    }

    /**
     * Returns the organization.
     *
     * @return the organization
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * Returns the AS number and name.
     *
     * @return the AS number and name
     */
    public String getAsNumberAndName() {
        return asNumberAndName;
    }

    /**
     * Returns the user agent.
     *
     * @return the user agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ShowMyIpResult{"
                + "ipv4=\"" + ipv4 + "\""
                + ", ipv6=\"" + ipv6 + "\""
                + ", country=\"" + country + "\""
                + ", region=\"" + region + "\""
                + ", city=\"" + city + "\""
                + ", zip=\"" + zip + "\""
                + ", timezone=\"" + timezone + "\""
                + ", isp=\"" + isp + "\""
                + ", organization=\"" + organization + "\""
                + ", asNumberAndName=\"" + asNumberAndName + "\""
                + ", userAgent=\"" + userAgent + "\""
                + "}";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = ipv4.hashCode();
        ret = 31 * ret + ipv6.hashCode();
        ret = 31 * ret + country.hashCode();
        ret = 31 * ret + region.hashCode();
        ret = 31 * ret + city.hashCode();
        ret = 31 * ret + zip.hashCode();
        ret = 31 * ret + timezone.hashCode();
        ret = 31 * ret + isp.hashCode();
        ret = 31 * ret + organization.hashCode();
        ret = 31 * ret + asNumberAndName.hashCode();
        ret = 31 * ret + userAgent.hashCode();
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ShowMyIpResult)) {
            return false;
        }

        ShowMyIpResult other = (ShowMyIpResult) o;
        return ipv4.equals(other.ipv4)
                && ipv6.equals(other.ipv6)
                && country.equals(other.country)
                && region.equals(other.region)
                && city.equals(other.city)
                && zip.equals(other.zip)
                && timezone.equals(other.timezone)
                && isp.equals(other.isp)
                && organization.equals(other.organization)
                && asNumberAndName.equals(other.asNumberAndName)
                && userAgent.equals(other.userAgent);
    }

    /** {@inheritDoc} */
    @Override
    public String getFromUrl() {
        return ShowMyIpScraper.url;
    }
}
