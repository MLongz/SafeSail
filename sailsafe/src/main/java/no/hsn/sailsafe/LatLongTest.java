package no.hsn.sailsafe;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.LatLongUtils;

public class LatLongTest {
    /**
     * Calculate the spherical distance between two LatLongs in meters using the Haversine
     * formula.
     * <p/>
     * This calculation is done using the assumption, that the earth is a sphere, it is not
     * though. If you need a higher precision and can afford a longer execution time you might
     * want to use vincentyDistance.
     *
     * @param latLong1 first LatLong
     * @param latLong2 second LatLong
     * @return distance in meters as a double
     */
    public static double sphericalDistance(LatLong latLong1, LatLong latLong2) {
        double dLat = Math.toRadians(latLong2.latitude - latLong1.latitude);
        double dLon = Math.toRadians(latLong2.longitude - latLong1.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(latLong1.latitude))
                * Math.cos(Math.toRadians(latLong2.latitude)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return c * LatLongUtils.EQUATORIAL_RADIUS;
    }
}
