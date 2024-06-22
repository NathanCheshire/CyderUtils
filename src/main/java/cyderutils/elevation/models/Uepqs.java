package cyderutils.elevation.models;

import com.google.gson.annotations.SerializedName;

/**
 * A JSON serialization class for a Universal elevation point query service.
 */
public final class Uepqs {
    /**
     * The elevation query object of this universal elevation point query service.
     */
    @SuppressWarnings("unused")
    @SerializedName("Elevation_Query")
    private ElevationQuery elevationQuery;

    /**
     * Constructs a new universal elevation point query service object.
     */
    public Uepqs() {}

    /**
     * Returns the elevation query object.
     *
     * @return the elevation query object
     */
    public ElevationQuery getElevationData() {
        return elevationQuery;
    }
}
