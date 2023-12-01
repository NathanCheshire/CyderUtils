package cyder.network;

import cyder.network.ipdataco.IpDataManager;

/**
 * The supported base URls for querying IpData objects via a {@link IpDataManager}.
 */
public enum IpDataBaseUrl {
    /**
     * The standard ipdata base url.
     */
    STANDARD("https://api.ipdata.co/?api-key="),

    /**
     * The base url for EU users.
     * Requests are specifically routed through the EU servers, that of Paris, Ireland, and Frankfurt.
     */
    EU("https://eu-api.ipdata.co?api-key=");

    private final String baseUrl;

    IpDataBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the base URL for this ipdata header.
     *
     * @return the base URL for this ipdata header
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
