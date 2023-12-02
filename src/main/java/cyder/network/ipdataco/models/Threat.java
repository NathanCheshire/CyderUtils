package cyder.network.ipdataco.models;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.gson.annotations.SerializedName;

/**
 * An IP address ipdataco considers a threat.
 */
@Immutable
public final class Threat {
    /**
     * Whether the ip address is associated with a Tor network node.
     */
    @SerializedName("is_tor")
    private final boolean isTor;

    /**
     * Whether the ip address is associated with Apple's iCloud relay service.
     */
    @SerializedName("is_icloud_relay")
    private final boolean isIcloudRelay;

    /**
     * Whether the ip address is a known proxy.
     */
    @SerializedName("is_proxy")
    private final boolean isProxy;

    /**
     * Whether the ip address belongs to a datacenter such as a cloud provider.
     */
    @SerializedName("is_datacenter")
    private final boolean isDatacenter;

    /**
     * Whether {@link #isTor} or {@link #isProxy} is true.
     */
    @SerializedName("is_anonymous")
    private final boolean isAnonymous;

    /**
     * Whether the IP address is a known source of malicious activity such as attacks, malware, bot nets, etc.
     */
    @SerializedName("is_known_attacker")
    private final boolean isKnownAttacker;

    /**
     * Whether the IP address is a known source of spam, harvesting, etc.
     */
    @SerializedName("is_known_abuser")
    private final boolean isKnownAbuser;

    /**
     * Whether {@link #isKnownAbuser} or {@link #isKnownAttacker} is true.
     */
    @SerializedName("is_threat")
    private final boolean isThreat;

    /**
     * Whether an IP address is a bogon.
     */
    @SerializedName("is_bogon")
    private final boolean isBogon;

    /**
     * The list of blocked IP addresses.
     */
    @SerializedName("blocklists")
    private final ImmutableList<BlockListEntry> blocklist;

    /**
     * Constructs a new Threat instance.
     *
     * @param isTor           whether the ip address is associated with a Tor network node
     * @param isIcloudRelay   whether the ip address is associated with Apple's iCloud relay service
     * @param isProxy         whether the ip address is a known proxy
     * @param isDatacenter    whether the ip address belongs to a datacenter such as a cloud provider
     * @param isAnonymous     whether {@link #isTor} or {@link #isProxy} is true
     * @param isKnownAttacker whether the IP address is a known source of malicious
     *                        activity such as attacks, malware, bot nets, etc
     * @param isKnownAbuser   the IP address is a known source of spam, harvesting, etc
     * @param isThreat        whether {@link #isKnownAbuser} or {@link #isKnownAttacker} is true
     * @param isBogon         whether an IP address is a bogon
     * @param blocklist       the list of blocked IP addresses
     */
    public Threat(boolean isTor,
                  boolean isIcloudRelay,
                  boolean isProxy,
                  boolean isDatacenter,
                  boolean isAnonymous,
                  boolean isKnownAttacker,
                  boolean isKnownAbuser,
                  boolean isThreat,
                  boolean isBogon,
                  ImmutableList<BlockListEntry> blocklist) {
        this.isTor = isTor;
        this.isIcloudRelay = isIcloudRelay;
        this.isProxy = isProxy;
        this.isDatacenter = isDatacenter;
        this.isAnonymous = isAnonymous;
        this.isKnownAttacker = isKnownAttacker;
        this.isKnownAbuser = isKnownAbuser;
        this.isThreat = isThreat;
        this.isBogon = isBogon;
        this.blocklist = blocklist;
    }

    /**
     * Returns whether the ip address is associated with a Tor network node.
     *
     * @return whether the ip address is associated with a Tor network node
     */
    public boolean isTor() {
        return isTor;
    }

    /**
     * Returns the ip address is associated with Apple's iCloud relay service.
     *
     * @return the ip address is associated with Apple's iCloud relay service
     */
    public boolean isIcloudRelay() {
        return isIcloudRelay;
    }

    /**
     * Returns whether the ip address is a known proxy.
     *
     * @return whether the ip address is a known proxy
     */
    public boolean isProxy() {
        return isProxy;
    }

    /**
     * Returns whether the ip address belongs to a datacenter such as a cloud provider.
     *
     * @return whether the ip address belongs to a datacenter such as a cloud provider
     */
    public boolean isDatacenter() {
        return isDatacenter;
    }

    /**
     * Returns whether {@link #isTor} or {@link #isProxy} is true.
     *
     * @return whether {@link #isTor} or {@link #isProxy} is true
     */
    public boolean isAnonymous() {
        return isAnonymous;
    }

    /**
     * Returns whether the IP address is a known source of malicious activity such as attacks, malware, bot nets, etc.
     *
     * @return whether the IP address is a known source of malicious activity such as attacks, malware, bot nets, etc
     */
    public boolean isKnownAttacker() {
        return isKnownAttacker;
    }

    /**
     * Returns whether the IP address is a known source of spam, harvesting, etc.
     *
     * @return whether the IP address is a known source of spam, harvesting, etc
     */
    public boolean isKnownAbuser() {
        return isKnownAbuser;
    }

    /**
     * Returns whether {@link #isKnownAbuser} or {@link #isKnownAttacker} is true.
     *
     * @return whether {@link #isKnownAbuser} or {@link #isKnownAttacker} is true
     */
    public boolean isThreat() {
        return isThreat;
    }

    /**
     * Returns whether an IP address is a bogon.
     *
     * @return whether an IP address is a bogon
     */
    public boolean isBogon() {
        return isBogon;
    }

    /**
     * Returns the list of blocked IP addresses.
     *
     * @return the list of blocked IP addresses
     */
    public ImmutableList<BlockListEntry> getBlocklist() {
        return blocklist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Threat{"
                + "isTor=" + isTor
                + ", isIcloudRelay=" + isIcloudRelay
                + ", isProxy=" + isProxy
                + ", isDatacenter=" + isDatacenter
                + ", isAnonymous=" + isAnonymous
                + ", isKnownAttacker=" + isKnownAttacker
                + ", isKnownAbuser=" + isKnownAbuser
                + ", isThreat=" + isThreat
                + ", isBogon=" + isBogon
                + ", blocklist=" + blocklist
                + '}';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Boolean.hashCode(isTor);
        ret = 31 * ret + Boolean.hashCode(isIcloudRelay);
        ret = 31 * ret + Boolean.hashCode(isProxy);
        ret = 31 * ret + Boolean.hashCode(isDatacenter);
        ret = 31 * ret + Boolean.hashCode(isAnonymous);
        ret = 31 * ret + Boolean.hashCode(isKnownAttacker);
        ret = 31 * ret + Boolean.hashCode(isKnownAbuser);
        ret = 31 * ret + Boolean.hashCode(isThreat);
        ret = 31 * ret + Boolean.hashCode(isBogon);
        ret = 31 * ret + blocklist.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Threat)) {
            return false;
        }

        Threat other = (Threat) o;
        return isTor == other.isTor
                && isIcloudRelay == other.isIcloudRelay
                && isProxy == other.isProxy
                && isDatacenter == other.isDatacenter
                && isAnonymous == other.isAnonymous
                && isKnownAttacker == other.isKnownAttacker
                && isKnownAbuser == other.isKnownAbuser
                && isThreat == other.isThreat
                && isBogon == other.isBogon
                && blocklist.equals(other.blocklist);
    }
}

