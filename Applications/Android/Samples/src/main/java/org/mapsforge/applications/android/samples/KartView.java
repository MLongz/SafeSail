package org.mapsforge.applications.android.samples;

import android.app.Fragment;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.rendertheme.AssetsRenderTheme;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.layer.Layer;import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Long Huynh on 10.02.2016.
 */
public class KartView extends Fragment implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, XmlRenderThemeMenuCallback {
    private MapView mapView;
    private TileCache tileCache;
    private TileRendererLayer tileRendererLayer;
    private LatLong myLocationNow;


    private ImageView imageCompass;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    TextView tvHeading;

    private MapView mMap = mapView; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = KartView.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    Bitmap bitmapRed;
    private HashMap<String, Layer> mLayers = new HashMap<String, Layer>();

    public KartView() {
        super();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.mapviewer, container, false);
        getMyLocation();

        this.imageCompass = (ImageView) rootView.findViewById(R.id.imageViewCompass);
        imageCompass.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                setCenter(myLocationNow);
            }
        });
        this.tvHeading = (TextView) rootView.findViewById(R.id.tvHeading);

        this.mapView = (MapView) rootView.findViewById(R.id.mapView);
        this.mapView.getMapScaleBar().setVisible(true);

        this.mapView.setClickable(true);
        this.mapView.setBuiltInZoomControls(false);
        this.mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
        this.mapView.getMapZoomControls().setZoomLevelMax((byte) 20);

        creatTileCache();
        createLayer();

        return rootView;
    }

/** Bruker AssesRendertheme klassen til å hente en rendertheme, returnerer null dersom det er feil */
    public XmlRenderTheme getRenderTheme() {
        try {
            return new AssetsRenderTheme(this.getActivity(), getRenderThemePrefix(), getRenderThemeFile(), this);
        } catch (IOException e) {
            Log.e(SamplesApplication.TAG, "Render theme failure " + e.toString());
        }
        return null;
    }

    public String getRenderThemePrefix() {
        return "";
    }

/** Henter rendertheme fila */
    public String getRenderThemeFile() {
        return "renderthemes/rendertheme-v4.xml";
    }

    /** Laqger layer ved bruk av en render theme, tegner opp kartet*/
    private void createLayer() {
        MapDataStore mapDataStore = new MapFile(getMapFile());
        this.tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                this.mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(getRenderTheme());
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);

    }

    /** Lager en tilecatch som lagrer layers så vi slipper å tegne den på nytt*/
    private void creatTileCache() {

        this.tileCache = AndroidUtil.createTileCache(this.getActivity(), "mapcache",
                mapView.getModel().displayModel.getTileSize(), 1f,
                this.mapView.getModel().frameBufferModel.getOverdrawFactor());
    }


    private void getMyLocation() {
        // getlocation
        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }

    /** Henter mapfila i SD kortet*/
    public File getMapFile() {
        File file = new File(Environment.getExternalStorageDirectory(), getMapFileName());
        return file;
    }

    protected String getMapFileName() {
        return "testing.map";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mapView.destroyAll();
    }
/** ---------------------COMPASS---------------------------------------- */
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);
        /**tvHeading.setText(Float.toString(degree) + "°");*/

        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                - degree,
                Animation.RELATIVE_TO_SELF, 0.5F,
                Animation.RELATIVE_TO_SELF, 0.5F);

        ra.setDuration(210);
        ra.setFillAfter(true);

        imageCompass.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /** --------------------------------------------------------------- */


    private void setUpMapIfNeeded() {

        /**if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }*/
    }


    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
    }


    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLong latLng = new LatLong(currentLatitude, currentLongitude);

        tvHeading.setText(latLng.toString());
        drawMarker(latLng);
        setCenter(latLng);
        myLocationNow = latLng;

    }

    private void setCenter(LatLong latLng){
        this.mapView.getModel().mapViewPosition.setCenter(latLng);
        this.mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);
    }

    private void drawMarker(LatLong latLng){
        Drawable drawableRed = getResources()
                .getDrawable(R.drawable.marker_red);
        bitmapRed = AndroidGraphicFactory.convertToBitmap(drawableRed);
        /** Tegner en marker */
        Marker m = new Marker(latLng, bitmapRed, 0, 0);
        mapView.getLayerManager().getLayers().add(m);
        mLayers.put(Integer.toString(m.hashCode()), m);
        mapView.getLayerManager().redrawLayers();
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this.getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);

    }

    @Override
    public Set<String> getCategories(XmlRenderThemeStyleMenu style) {
        return null;
    }
}
