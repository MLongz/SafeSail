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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.android.view.MapView;


import java.io.File;
import java.util.HashMap;

/**
 * Created by Long Huynh on 10.02.2016.
 */
public class KartView extends Fragment implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String MAPFILE = "germany.map";
    private MapView mapView;
    private TileCache tileCache;
    private TileRendererLayer tileRendererLayer;

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
        this.tvHeading = (TextView) rootView.findViewById(R.id.tvHeading);

        this.mapView = (MapView) rootView.findViewById(R.id.mapView);
        this.mapView.getMapScaleBar().setVisible(true);

        this.mapView.setClickable(true);
        this.mapView.setBuiltInZoomControls(false);
        this.mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
        this.mapView.getMapZoomControls().setZoomLevelMax((byte) 20);



        LayerManager layerManager = this.mapView.getLayerManager();
        Layers layers = layerManager.getLayers();

        MapViewPosition mapViewPosition = this.mapView.getModel().mapViewPosition;
        mapViewPosition.setZoomLevel((byte) 16);
        tileCache = AndroidUtil.createTileCache(this.getActivity(),
                "fragments",
                this.mapView.getModel().displayModel.getTileSize(), 1.0f,
                1.5);
        layers.add(AndroidUtil.createTileRendererLayer(this.tileCache,
                mapViewPosition, getMapFile(),
                InternalRenderTheme.OSMARENDER, false, true));


        MapDataStore mapDataStore =  getMapFile();
        this.tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                this.mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

        // only once a layer is associated with a mapView the rendering starts
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);
        return rootView;
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


    public MapFile getMapFile() {
        return new MapFile(new File(Environment.getExternalStorageDirectory(),
                this.getMapFileName()));
    }

    protected String getMapFileName() {
        return "germany.map";
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
        // Do a null check to confirm that we have not already instantiated the map.
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

    private void setUpMap() {
        /** mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker")); */

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

}
