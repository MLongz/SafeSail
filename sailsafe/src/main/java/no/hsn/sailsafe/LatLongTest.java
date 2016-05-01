package no.hsn.sailsafe;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.LatLongUtils;

/**
 * Created by M. Long on 01.05.2016.
 */
public class LatLongTest {
    /**
     * The equatorial radius as defined by the <a href="http://en.wikipedia.org/wiki/World_Geodetic_System">WGS84
     * ellipsoid</a>. WGS84 is the reference coordinate system used by the Global Positioning System.
     */
    public static final double EQUATORIAL_RADIUS = 6378137.0;

    /**
     * The flattening factor of the earth's ellipsoid is required for distance computation.
     */
    public static final double INVERSE_FLATTENING = 298.257223563;

    /**
     * Polar radius of earth is required for distance computation.
     */
    public static final double POLAR_RADIUS = 6356752.3142;

    /**
     * Maximum possible latitude coordinate.
     */
    public static final double LATITUDE_MAX = 90;

    /**
     * Minimum possible latitude coordinate.
     */
    public static final double LATITUDE_MIN = -LATITUDE_MAX;

    /**
     * Maximum possible longitude coordinate.
     */
    public static final double LONGITUDE_MAX = 180;

    /**
     * Minimum possible longitude coordinate.
     */
    public static final double LONGITUDE_MIN = -LONGITUDE_MAX;

    /**
     * Conversion factor from degrees to microdegrees.
     */
    private static final double CONVERSION_FACTOR = 1000000.0;

    private static final String DELIMITER = ",";

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

    /**
     * Calculates geodetic distance between two LatLongs using Vincenty inverse formula
     * for ellipsoids. This is very accurate but consumes more resources and time than the
     * sphericalDistance method.
     * <p/>
     * Adaptation of Chriss Veness' JavaScript Code on
     * http://www.movable-type.co.uk/scripts/latlong-vincenty.html
     * <p/>
     * Paper: Vincenty inverse formula - T Vincenty, "Direct and Inverse Solutions of Geodesics
     * on the Ellipsoid with application of nested equations", Survey Review, vol XXII no 176,
     * 1975 (http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf)
     *
     * @param latLong1 first LatLong
     * @param latLong2 second LatLong
     * @return distance in meters between points as a double
     */
    public static double vincentyDistance(LatLong latLong1, LatLong latLong2) {
        double f = 1 / LatLongTest.INVERSE_FLATTENING;
        double L = Math.toRadians(latLong2.latitude - latLong1.longitude);
        double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(latLong1.latitude)));
        double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(latLong2.latitude)));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double lambda = L, lambdaP, iterLimit = 100;

        double cosSqAlpha = 0, sinSigma = 0, cosSigma = 0, cos2SigmaM = 0, sigma = 0, sinLambda = 0, sinAlpha = 0, cosLambda = 0;
        do {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
                    * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0)
                return 0; // co-incident points
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            if (cosSqAlpha != 0) {
                cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            } else {
                cos2SigmaM = 0;
            }
            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L
                    + (1 - C)
                    * f
                    * sinAlpha
                    * (sigma + C * sinSigma
                    * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        if (iterLimit == 0)
            return 0; // formula failed to converge

        double uSq = cosSqAlpha
                * (Math.pow(LatLongTest.EQUATORIAL_RADIUS, 2) - Math.pow(LatLongTest.POLAR_RADIUS, 2))
                / Math.pow(LatLongTest.POLAR_RADIUS, 2);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B
                * sinSigma
                * (cos2SigmaM + B
                / 4
                * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
                * (-3 + 4 * sinSigma * sinSigma)
                * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
        double s = LatLongTest.POLAR_RADIUS * A * (sigma - deltaSigma);

        return s;
    }

}
