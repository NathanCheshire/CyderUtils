package com.github.natche.cyderutils.elevation.models;

import com.google.gson.annotations.SerializedName;

/** A JSON serialization class for an elevation data query. */
public class ElevationData {
    /** The USGS elevation point query service object. */
    @SerializedName("USGS_Elevation_Point_Query_Service")
    private Uepqs uepqs;

    /** Constructs a new instance of an ElevationData object. */
    public ElevationData() {}

    /**
     * Returns the USGS elevation point query service object.
     *
     * @return the USGS elevation point query service object
     */
    public Uepqs getUepqs() {
        return uepqs;
    }
}
