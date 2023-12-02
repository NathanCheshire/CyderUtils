package cyder.network.ipdataco;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.gson.annotations.SerializedName;

@Immutable
public class Threat {
    @SerializedName("is_tor")
    private final boolean isTor;
    @SerializedName("is_icloud_relay")
    private final boolean isIcloudRelay;
    @SerializedName("is_proxy")
    private final boolean isProxy;
    @SerializedName("is_datacenter")
    private final boolean isDatacenter;
    @SerializedName("is_anonymous")
    private final boolean isAnonymous;
    @SerializedName("is_known_attacker")
    private final boolean isKnownAttacker;
    @SerializedName("is_known_abuser")
    private final boolean isKnownAbuser;
    @SerializedName("is_threat")
    private final boolean isThreat;
    @SerializedName("is_bogon")
    private final boolean isBogon;
    private final ImmutableList<String> blocklists;

    // Constructor and Getters
}

