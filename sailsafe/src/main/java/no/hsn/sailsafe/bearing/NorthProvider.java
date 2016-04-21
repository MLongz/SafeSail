package no.hsn.sailsafe.bearing;

/**
 * Inspiration from https://github.com/meniku/bearing-example, Nils Kübler 2014
 * Created by Knut Johan Hesten on 2016-04-21.
 */
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import no.hsn.sailsafe.SailsafeApplication;

public class NorthProvider implements SensorEventListener, LocationListener {
    public static final String TAG = SailsafeApplication.TAG;

    /**
     * Interface implementation of the direction change
     */
    public interface ChangeEventListener {
        /**
         * Callback when the bearing changes.
         * @param bearing the new bearing value
         */
        void onAngleChanged(double bearing);

        void onNpLocationChanged(Location location);
    }

    private final SensorManager npSensorManager;
    private final LocationManager npLocationManager;
    private final Sensor sensorAccelerometer;
    private final Sensor sensorMagneticField;

    // some arrays holding intermediate values read from the sensors, used to calculate our smoothedAngleToMagneticNorth
    // value

    private float[] valuesAccelerometer;
    private float[] valuesMagneticField;
    private float[] matrixR;
    private float[] matrixI;
    private float[] matrixValues;

    private final double minDifferenceForEvent;

    private final double minDelayThrottleTime;

    private ChangeEventListener npChangeEventListener;

    private AverageQueue azimuthQueueToMagneticNorth;

    private double smoothedAngleToMagneticNorth = Double.NaN;

    private double angleToTrueNorth = Double.NaN;

    private double previousAngleToTrueNorth = Double.NaN;

    private Location currentLocation;

    private long lastAngleChangeSentAt = -1;
    private long lastLocationChangeSentAt = -1;

    /**
     * Default constructor.
     *
     * @param context Application Context
     */
    public NorthProvider(Context context) {
        this(context, 10, 0.5, 50);
    }

    /**
     * @param context Application Context
     * @param smoothing Amount of measurements used to calculate the average angle magnetic north.
     *                  A value of 1 equals the smallest delay.
     * @param minDiffForEvent Minimum change of angle in degrees required to run update event
     * @param minDelayThrottleTime Minimum delay in milliseconds between notifications
     */
    public NorthProvider(Context context, int smoothing, double minDiffForEvent, int minDelayThrottleTime) {
        this.npSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.sensorAccelerometer = npSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.npLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.sensorMagneticField = npSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        this.valuesAccelerometer = new float[3];
        this.valuesMagneticField = new float[3];

        this.matrixR = new float[9];
        this.matrixI = new float[9];
        this.matrixValues = new float[3];

        this.minDifferenceForEvent = minDiffForEvent;
        this.minDelayThrottleTime = minDelayThrottleTime;

        this.azimuthQueueToMagneticNorth = new AverageQueue(smoothing);
    }

    public void start() {
        this.npSensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        this.npSensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_UI);

        for (final String provider : npLocationManager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.PASSIVE_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                if (currentLocation == null) {
                    currentLocation = npLocationManager.getLastKnownLocation(provider);
                }
                npLocationManager.requestLocationUpdates(provider, 0, 100.0f, this);
            }
        }
    }

    public void stop() {
        npSensorManager.unregisterListener(this, sensorAccelerometer);
        npSensorManager.unregisterListener(this, sensorMagneticField);
        npLocationManager.removeUpdates(this);
    }

    /**
     * @return current angle
     */
    public double getAngle() {
        return angleToTrueNorth;
    }

    /**
     * Returns the event listener
     * @return the event listener
     */
    public ChangeEventListener getChangeEventListener()
    {
        return npChangeEventListener;
    }

    /**
     * Specifies the event listener to which bearing events must be sent.
     * @param changeEventListener the bearing event listener
     */
    public void setChangeEventListener(ChangeEventListener changeEventListener) {
        this.npChangeEventListener = changeEventListener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, valuesAccelerometer, 0, 3);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, valuesMagneticField, 0, 3);
                break;
        }

        boolean success = SensorManager.getRotationMatrix(matrixR, matrixI,
                valuesAccelerometer,
                valuesMagneticField);

        // calculate a new smoothed value and put in smoothedAngleToMagneticNorth
        if (success) {
            SensorManager.getOrientation(matrixR, matrixValues);
            azimuthQueueToMagneticNorth.putValue(matrixValues[0]);
            smoothedAngleToMagneticNorth = Math.toDegrees(azimuthQueueToMagneticNorth.getAverage());
        }

        updateAngle();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    @Override
    public void onLocationChanged(Location location) {
        this.currentLocation = location;
        //if(npChangeEventListener != null) {
        //    npChangeEventListener.onNpLocationChanged(currentLocation);
        //}
        updateLocation();

    }

    public Location getCurrentLocation() {
        return this.currentLocation;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {   }

    @Override
    public void onProviderEnabled(String s) {   }

    @Override
    public void onProviderDisabled(String s) {   }

    private void updateAngle() {
        if (!Double.isNaN(this.smoothedAngleToMagneticNorth)) {
            if(this.currentLocation == null) {
                Log.w(TAG, "Location is not valid, angle to north could not be set!");
                angleToTrueNorth = smoothedAngleToMagneticNorth;
            } else {
                angleToTrueNorth = getAngleForLocation(this.currentLocation);
            }

            // Throttle dispatching based on minDelayThrottleTime and minDiffForEvent
            if( System.currentTimeMillis() - lastAngleChangeSentAt > minDelayThrottleTime &&
                    (Double.isNaN(previousAngleToTrueNorth) || Math.abs(previousAngleToTrueNorth - angleToTrueNorth) >= minDifferenceForEvent)) {
                previousAngleToTrueNorth = angleToTrueNorth;
                if(npChangeEventListener != null) {
                    npChangeEventListener.onAngleChanged(angleToTrueNorth);
                }
                lastAngleChangeSentAt = System.currentTimeMillis();
            }
        }
    }

    private void updateLocation() {
        if (System.currentTimeMillis() - lastLocationChangeSentAt > minDelayThrottleTime) {
            if (npChangeEventListener != null) {
                npChangeEventListener.onNpLocationChanged(currentLocation);
            }
            lastLocationChangeSentAt = System.currentTimeMillis();
        }
    }

    private double getAngleForLocation(Location location) {
        return smoothedAngleToMagneticNorth + getGeomagneticField(location).getDeclination();
    }

    private GeomagneticField getGeomagneticField(Location location) {
        return new GeomagneticField(
                (float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getAltitude(),
                System.currentTimeMillis());
    }
}