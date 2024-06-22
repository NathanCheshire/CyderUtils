package com.github.natche.cyderutils.elevation.models;

import com.google.gson.annotations.SerializedName;

/**
 * A json serialization class for an elevation query.
 */
public class ElevationQuery {
    /**
     * The x point of this elevation query.
     */
    private double x;

    /**
     * The y point of this elevation query.
     */
    private double y;

    /**
     * The data source this elevation was obtained from.
     */
    @SuppressWarnings("unused")
    @SerializedName("Data_source")
    private String dataSource;

    /**
     * The elevation at this x,y location.
     */
    @SerializedName("Elevation")
    private String elevation;

    /**
     * The units of this elevation measurement.
     */
    @SerializedName("Units")
    private String units;

    /**
     * Constructs a new elevation query object.
     */
    public ElevationQuery() {}

    /**
     * Returns the x point of this elevation query.
     *
     * @return the x point of this elevation query
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y point of this elevation query.
     *
     * @return the y point of this elevation query
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the data source this elevation was obtained from.
     *
     * @return the data source this elevation was obtained from
     */
    public String getDataSource() {
        return dataSource;
    }

    /**
     * Returns the elevation at this x,y location.
     *
     * @return the elevation at this x,y location
     */
    public String getElevation() {
        return elevation;
    }

    /**
     * Returns the units of this elevation measurement.
     *
     * @return the units of this elevation measurement
     */
    public String getUnits() {
        return units;
    }
}
