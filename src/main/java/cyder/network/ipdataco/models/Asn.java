package cyder.network.ipdataco.models;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

/**
 * An object representing an autonomous system number and related data from ipdata.co.
 */
@Immutable
public final class Asn {
    /**
     * The autonomous system number.
     */
    private final String asn;

    /**
     * The organization name.
     */
    private final String name;

    /**
     * The website of the organization.
     */
    private final String domain;

    /**
     * The network prefix the IP address belongs to.
     */
    private final String route;

    /**
     * The usage type associated with teh IP address and ASN.
     */
    private final String type;

    /**
     * Constructs a new instance of an Asn object.
     *
     * @param asn the autonomous system number
     * @param name the organization name
     * @param domain the website of the organization
     * @param route the network prefix the IP address belongs to
     * @param type the usage type associated with teh IP address and ASN
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if any parameter is empty
     */
    public Asn(String asn, String name, String domain, String route, String type) {
        Preconditions.checkNotNull(asn);
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(route);
        Preconditions.checkNotNull(type);
        Preconditions.checkArgument(!asn.trim().isEmpty());
        Preconditions.checkArgument(!name.trim().isEmpty());
        Preconditions.checkArgument(!domain.trim().isEmpty());
        Preconditions.checkArgument(!route.trim().isEmpty());
        Preconditions.checkArgument(!type.trim().isEmpty());

        this.asn = asn;
        this.name = name;
        this.domain = domain;
        this.route = route;
        this.type = type;
    }

    /**
     * Returns the autonomous system number.
     *
     * @return the autonomous system number
     */
    public String getAsn() {
        return asn;
    }

    /**
     * Returns the organization name.
     *
     * @return the organization name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the website of the organization.
     *
     * @return the website of the organization
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Returns the network prefix the IP address belongs to.
     *
     * @return the network prefix the IP address belongs to
     */
    public String getRoute() {
        return route;
    }

    /**
     * Returns the usage type associated with teh IP address and ASN.
     *
     * @return the usage type associated with teh IP address and ASN
     */
    public String getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Asn{"
                + "asn=" + asn + "\""
                + ", name=\"" + name + "\""
                + ", domain=\"" + domain + "\""
                + ", route=\"" + route + "\""
                + ", type=\"" + type +"\""
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = asn.hashCode();
        ret = 31 * ret + name.hashCode();
        ret = 31 * ret + domain.hashCode();
        ret = 31 * ret + route.hashCode();
        ret = 31 * ret + type.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Asn)) {
            return false;
        }

        Asn other = (Asn) o;
        return asn.equals(other.asn)
                && name.equals(other.name)
                && domain.equals(other.domain)
                && route.equals(other.route)
                && type.equals(other.type);
    }
}
