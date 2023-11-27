package cyder.network.scrapers;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import cyder.constants.CyderRegexPatterns;

/**
 * A record used to store the data scraped by {@link WhatsMyIpScraper#getIspAndNetworkDetails()}.
 */
@Immutable
public final class WhatsMyIpScraperResult {
    /**
     * The sharable report header.
     */
    private static final String SHARABLE_REPORT_HEADER = "https://www.whatismyisp.com/ip/";

    /**
     * The internet service provider.
     */
    private final String isp;

    /**
     * The hostname.
     */
    private final String hostname;

    /**
     * The ip.
     */
    private final String ip;

    /**
     * The city.
     */
    private final String city;

    /**
     * The state.
     */
    private final String state;

    /**
     * The country.
     */
    private final String country;

    /**
     * Constructs a new WhatsMyIpScraperResult instance.
     *
     * @param isp the internet service provider
     * @param hostname the hostname
     * @param ip the ip
     * @param city the city
     * @param state the state
     * @param country the country
     * @throws NullPointerException if null is provided for any parameter
     * @throws IllegalArgumentException if an empty string is provided for any parameter.
     */
    public WhatsMyIpScraperResult(String isp, String hostname, String ip,
                                  String city, String state, String country) {
        Preconditions.checkNotNull(isp);
        Preconditions.checkNotNull(hostname);
        Preconditions.checkNotNull(ip);
        Preconditions.checkNotNull(city);
        Preconditions.checkNotNull(state);
        Preconditions.checkNotNull(country);
        Preconditions.checkArgument(!isp.trim().isEmpty());
        Preconditions.checkArgument(!hostname.trim().isEmpty());
        Preconditions.checkArgument(!ip.trim().isEmpty());
        Preconditions.checkArgument(!city.trim().isEmpty());
        Preconditions.checkArgument(!state.trim().isEmpty());
        Preconditions.checkArgument(!country.trim().isEmpty());

        this.isp = isp;
        this.hostname = hostname;
        this.ip = ip;
        this.city = city;
        this.state = state;
        this.country = country;
    }

    /**
     * Returns the isp.
     *
     * @return the isp
     */
    public String getIsp() {
        return isp;
    }

    /**
     * Returns the hostname.
     *
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Returns the ip.
     *
     * @return the ip
     */
    public String getIp() {
        return ip;
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
     * Returns the state.
     *
     * @return the state
     */
    public String getState() {
        return state;
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
     * Returns a sharable report of this scrape result.
     *
     * @return a sharable report of this scrape result
     */
    public String getSharableReport() {
        return SHARABLE_REPORT_HEADER + ip;
    }

    /**
     * Returns whether the ip of this result is a valid IPV4 pattern.
     *
     * @return whether the ip of this result is a valid IPV4 pattern
     */
    public boolean isIpValidIpv4() {
        return CyderRegexPatterns.ipv4Pattern.matcher(ip).matches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "WhatsMyIpScraperResult{"
                + "isp=\"" + isp + "\""
                + ", hostname=\"" + hostname + "\""
                + ", ip=\"" + ip + "\""
                + ", city=\"" + city + "\""
                + ", state=\"" + state + "\""
                + ", country=\"" + country + "\""
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = isp.hashCode();
        ret = 31 * ret + hostname.hashCode();
        ret = 31 * ret + ip.hashCode();
        ret = 31 * ret + city.hashCode();
        ret = 31 * ret + state.hashCode();
        ret = 31 * ret + country.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof WhatsMyIpScraperResult)) {
            return false;
        }

        WhatsMyIpScraperResult other = (WhatsMyIpScraperResult) o;
        return isp.equals(other.isp)
                && hostname.equals(other.hostname)
                && ip.equals(other.ip)
                && city.equals(other.city)
                && state.equals(other.state)
                && country.equals(other.country);
    }
}
