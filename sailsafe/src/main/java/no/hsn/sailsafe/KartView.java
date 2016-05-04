package no.hsn.sailsafe;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import junit.framework.AssertionFailedError;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Tag;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.rendertheme.AssetsRenderTheme;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.datastore.MapReadResult;
import org.mapsforge.map.datastore.PointOfInterest;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;
import org.mapsforge.map.util.MapViewProjection;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import no.hsn.sailsafe.bearing.NorthProvider;

/**
 * Created by Long Huynh on 10.02.2016.
 */
public class KartView extends Fragment implements XmlRenderThemeMenuCallback, NorthProvider.ChangeEventListener {
    private MapView mapView;
    private TileCache tileCache;
    private TileRendererLayer tileRendererLayer;
    private LatLong myLocationNow;

    public static final String TAG = SailsafeApplication.TAG;


    private static final int TOUCH_RADIUS = 32 / 2;
    private MapViewProjection projection;
    private MainActivity activity;

    private NorthProvider northProvider;
    private ImageView imageCompass;
    private Rotating boatMarker;
    public static final int BOATMARKERINDEX = 2;
    public static final int FARER = 1;
    private static final Paint fareFarge = Utils.createPaint(
            AndroidGraphicFactory.INSTANCE.createColor(Color.BLUE), 0,
            Style.FILL);
    int i;

    public KartView() {
        super();
    }

    @Override
    public void onAttach(Activity myActivity) {
        super.onAttach(myActivity);
        this.activity= (MainActivity) myActivity;
    }

    @Override
    public void onAngleChanged(double angle) {
        //TODO Compass skal ikke rotere men altid vise true north
        //imageCompass.setRotation((float) angle);
        boatMarker.setRotation((float) angle);
        mapView.getLayerManager().getLayers().get(BOATMARKERINDEX).requestRedraw();
    }

    @Override
    public void onNpLocationChanged(Location location) {
        myLocationNow = new LatLong(location.getLatitude(), location.getLongitude());
        boatMarker.setLocation(new LatLong(location.getLatitude(), location.getLongitude()));
        //Log.d(TAG, "onNpLocationChanged " + String.valueOf(location.toString()));
        mapView.getLayerManager().getLayers().get(BOATMARKERINDEX).requestRedraw();
        reverseGeoCode(myLocationNow);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.mapviewer, container, false);
        this.projection = new MapViewProjection(this.mapView);
        this.imageCompass = (ImageView) rootView.findViewById(R.id.imageViewCompass);
        imageCompass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setCenter(myLocationNow);
            }
        });
        this.mapView = (MapView) rootView.findViewById(R.id.mapView);
        this.mapView.getMapScaleBar().setVisible(true);

        this.mapView.setClickable(true);
        this.mapView.setBuiltInZoomControls(false);
        this.mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
        this.mapView.getMapZoomControls().setZoomLevelMax((byte) 20);

        this.projection = new MapViewProjection(this.mapView);

        northProvider = new NorthProvider(rootView.getContext());
        northProvider.setChangeEventListener(this);
        northProvider.start();

        org.mapsforge.core.graphics.Bitmap bm = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.navarrow));
        bm.scaleTo(75,75);

        this.myLocationNow = new LatLong(northProvider.getCurrentLocation().getLatitude(), northProvider.getCurrentLocation().getLongitude());
        boatMarker = new Rotating(myLocationNow, bm, 0,0, 0);

        creatTileCache();
        createLayer();

        mapView.getLayerManager().getLayers().add(boatMarker);
        setCenter(myLocationNow);
        return rootView;
    }

    public void testlol (LatLong location){
        myLocationNow = new LatLong(location.latitude, location.longitude);
        boatMarker.setLocation(new LatLong(location.latitude, location.longitude));
        //Log.d(TAG, "onNpLocationChanged " + String.valueOf(location.toString()));
        mapView.getLayerManager().getLayers().get(BOATMARKERINDEX).requestRedraw();
        reverseGeoCode(myLocationNow);
    }

    public void markingDanger(final LatLong position){
        float circleSize = 20 * this.mapView.getModel().displayModel
                .getScaleFactor();

        i += 1;

        FixedPixelCircle tappableCircle = new FixedPixelCircle(position,
                circleSize, fareFarge, null) {

            int count = i;

            @Override
            public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas
                    canvas, Point topLeftPoint) {
                super.draw(boundingBox, zoomLevel, canvas, topLeftPoint);

                long mapSize = MercatorProjection.getMapSize(zoomLevel, this.displayModel.getTileSize());

                int pixelX = (int) (MercatorProjection.longitudeToPixelX(position.longitude, mapSize) - topLeftPoint.x);
                int pixelY = (int) (MercatorProjection.latitudeToPixelY(position.latitude, mapSize) - topLeftPoint.y);
                String text = Integer.toString(count);
                canvas.drawText(text, pixelX - fareFarge.getTextWidth(text) / 2, pixelY + fareFarge.getTextHeight(text) / 2, fareFarge);
            }
        };
        this.mapView.getLayerManager().getLayers().add(tappableCircle);
        tappableCircle.requestRedraw();
    }


    private void onLongPress(LatLong tapLatLong, Point tapXY) {
          testlol(tapLatLong);
    }

    private void reverseGeoCode(LatLong latlong) {
        // Reads all map data for the area covered by the given tile at the tile zoom level
        int tileX = MercatorProjection.longitudeToTileX(latlong.longitude, mapView.getModel().mapViewPosition.getZoomLevel());
        int tileY = MercatorProjection.latitudeToTileY(latlong.latitude, mapView.getModel().mapViewPosition.getZoomLevel());
        Tile tile = new Tile(tileX, tileY, mapView.getModel().mapViewPosition.getZoomLevel(), mapView.getModel().displayModel.getTileSize());
        MapFile mapFile = new MapFile(getMapFile());
        MapReadResult mapReadResult = mapFile.readMapData(tile);

        LatLongTest latlongtest = null;
        List<PointOfInterest> pointOfInterests = mapReadResult.pointOfInterests;
        try {
            for (PointOfInterest pointOfInterest : pointOfInterests) {
                LatLong latLong = pointOfInterest.position;
                Double meterRetur = latlongtest.sphericalDistance(myLocationNow, latLong);
                if(meterRetur <= 1000){
                    List<Tag> tags = pointOfInterest.tags;
                    for (Tag tag : tags) {
                        String checkKey;
                        checkKey = tag.key.toString();
                        if (checkKey.contains("skjaer")) {
                            activity.getVarsel(1, "Skjaer ahead!");
                            markingDanger(pointOfInterest.position);
                        }
                    }
                }

            }
        } catch (AssertionFailedError ex) {
            Log.d(TAG, " Assertion failed on " + ex.getMessage());
        }
    }




/** Bruker AssesRendertheme klassen til å hente en rendertheme, returnerer null dersom det er feil */
    public XmlRenderTheme getRenderTheme() {
        try {
            return new AssetsRenderTheme(this.getActivity(), getRenderThemePrefix(), getRenderThemeFile(), this);
        } catch (IOException e) {
            Log.e("DD", "Render theme failure " + e.toString());
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
        this.tileRendererLayer = new TileRendererLayer(
                tileCache, mapDataStore,
                this.mapView.getModel().mapViewPosition,
                false, true, AndroidGraphicFactory.INSTANCE){
            @Override
            public boolean onLongPress(LatLong tapLatLong, Point layerXY, Point tapXY) {
                KartView.this.onLongPress(tapLatLong, tapXY);
                return true;
            }
        };


        tileRendererLayer.setXmlRenderTheme(getRenderTheme());
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);

    }

    /** Lager en tilecatch som lagrer layers så vi slipper å tegne den på nytt*/
    private void creatTileCache() {

        this.tileCache = AndroidUtil.createTileCache(this.getActivity(), "mapcache",
                mapView.getModel().displayModel.getTileSize(), 1f,
                this.mapView.getModel().frameBufferModel.getOverdrawFactor());
    }

    /** Henter mapfila i SD kortet*/
    public File getMapFile() {
        return new File(Environment.getExternalStorageDirectory(), getMapFileName());
    }

    protected String getMapFileName() {
        return "germany.map";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mapView.destroyAll();
    }

    @Override
    public void onResume(){
        super.onResume();
        northProvider.start();
        setCenter(myLocationNow);
    }

    @Override
    public void onPause(){
        super.onPause();
        northProvider.stop();
    }

    private void setCenter(LatLong latLng){
        this.mapView.getModel().mapViewPosition.setCenter(latLng);
        this.mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);
    }

    @Override
    public Set<String> getCategories(XmlRenderThemeStyleMenu style) {
        return null;
    }
}