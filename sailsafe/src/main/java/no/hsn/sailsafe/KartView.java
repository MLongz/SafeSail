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

import android.widget.TextView;
import android.widget.Toast;
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
import org.mapsforge.map.reader.header.MapFileException;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import no.hsn.sailsafe.bearing.NorthProvider;

/**
 * Created by Long Huynh on 10.02.2016.
 */
public class KartView extends Fragment implements XmlRenderThemeMenuCallback, NorthProvider.ChangeEventListener {
    private static final int    DANGER_INDEX = 1;
    private static final int    BOAT_MARKER_INDEX = 2;
    private static final double METER_PER_SECOND_TO_KNOTS = 1.94384449;
    private static final double YELLOW_WARNING_MPS = 2.57222222545179;
    private static final int    YELLOW_WARNING_COLOR = 0xffffff00;
    private static final int    NORMAL_SPEED_COLOR = 0xff000000;
    private static final String SPEED_UNIT = "knots";
    private static final String TAG = SailsafeApplication.TAG;

    private MapView mapView;
    private TileCache tileCache;
    private LatLong myLocation;

    private MainActivity activity;

    private NorthProvider northProvider;
    private TextView txtSpeedView;
    private Formatter textFormat;
    private Rotating boatMarker;

    private static final Paint colorDanger = Utils.createPaint(
            AndroidGraphicFactory.INSTANCE.createColor(100, 174, 194, 45), 0,
            Style.FILL);
    private static final Paint colorText = Utils.createPaint(
            AndroidGraphicFactory.INSTANCE.createColor(Color.BLACK), 0,
            Style.STROKE);
    private static final Paint colorTransparency = Utils.createPaint(
            AndroidGraphicFactory.INSTANCE.createColor(Color.TRANSPARENT), 0,
            Style.STROKE);

    public KartView() { super(); }

    @Override
    public void onAttach(Activity myActivity) {
        super.onAttach(myActivity);
        this.activity= (MainActivity) myActivity;
    }

    @Override
    public void onAngleChanged(double angle) {
        boatMarker.setRotation((float) angle);
        mapView.getLayerManager().getLayers().get(BOAT_MARKER_INDEX).requestRedraw();
    }

    @Override
    public void onNpLocationChanged(Location location) {
        if (location != null) {
            myLocation = new LatLong(location.getLatitude(), location.getLongitude());
            boatMarker.setLocation(new LatLong(location.getLatitude(), location.getLongitude()));
            mapView.getLayerManager().getLayers().get(BOAT_MARKER_INDEX).requestRedraw();
            reverseGeoCode(myLocation);
            textFormat.format(Locale.GERMANY, "%5.1f", location.getSpeed() * METER_PER_SECOND_TO_KNOTS);
            this.txtSpeedView.setText(textFormat + " " + SPEED_UNIT);
            if (location.getSpeed() >= YELLOW_WARNING_MPS) {
                this.txtSpeedView.setTextColor(YELLOW_WARNING_COLOR);
            } else {
                this.txtSpeedView.setTextColor(NORMAL_SPEED_COLOR);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.mapviewer, container, false);
        ImageView imageCompass = (ImageView) rootView.findViewById(R.id.imageViewCompass);
        this.txtSpeedView = (TextView) rootView.findViewById(R.id.txtSpeedView);
        this.txtSpeedView.setText("0 " + SPEED_UNIT);
        this.textFormat = new Formatter(new StringBuilder());
        imageCompass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setCenter(myLocation);
            }
        });
        this.mapView = (MapView) rootView.findViewById(R.id.mapView);
        this.mapView.getMapScaleBar().setVisible(true);

        this.mapView.setClickable(true);
        this.mapView.setBuiltInZoomControls(false);
        this.mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
        this.mapView.getMapZoomControls().setZoomLevelMax((byte) 20);

        northProvider = new NorthProvider(rootView.getContext());
        northProvider.setChangeEventListener(this);
        northProvider.start();

        org.mapsforge.core.graphics.Bitmap bm = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.navarrow));
        bm.scaleTo(75,75);
        if (northProvider.getCurrentLocation() != null) {
            this.myLocation = new LatLong(northProvider.getCurrentLocation().getLatitude(), northProvider.getCurrentLocation().getLongitude());
        }
        boatMarker = new Rotating(myLocation, bm, 0,0, 0);
        try {
            createTileCache();
            createMapLayer();
            markingDanger(myLocation, "", colorTransparency);
            this.mapView.getLayerManager().getLayers().add(BOAT_MARKER_INDEX, boatMarker);
            setCenter(myLocation);
        } catch (MapFileException mEx) {
            Toast.makeText(getActivity(), "Mapfile not found. Make sure that there exists a map file with the name 'norway.map' in the internal storage location", Toast.LENGTH_LONG).show();
        }
        return rootView;
    }

    private void testLocationOnPress(LatLong location) {
        myLocation = new LatLong(location.latitude, location.longitude);
        boatMarker.setLocation(new LatLong(location.latitude, location.longitude));
        //Log.d(TAG, "onNpLocationChanged " + String.valueOf(location.toString()));
        mapView.getLayerManager().getLayers().get(BOAT_MARKER_INDEX).requestRedraw();
        reverseGeoCode(myLocation);
    }

    private void markingDanger(final LatLong position, final String key, Paint farge){
        float circleSize = 15 * this.mapView.getModel().displayModel.getScaleFactor();

        FixedPixelCircle dangerCircle = new FixedPixelCircle(position,
                circleSize, farge, null) {

            @Override
            public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas
                    canvas, Point topLeftPoint) {
                super.draw(boundingBox, zoomLevel, canvas, topLeftPoint);

                long mapSize = MercatorProjection.getMapSize(zoomLevel, this.displayModel.getTileSize());

                int pixelX = (int) (MercatorProjection.longitudeToPixelX(position.longitude, mapSize) - topLeftPoint.x);
                int pixelY = (int) (MercatorProjection.latitudeToPixelY(position.latitude, mapSize) - topLeftPoint.y);
                String text = key;
                canvas.drawText(text, pixelX - colorText.getTextWidth(text) / 2, pixelY + colorText.getTextHeight(text) / 2, colorText);
            }
        };
        this.mapView.getLayerManager().getLayers().add(DANGER_INDEX, dangerCircle);
        this.mapView.getLayerManager().getLayers().get(DANGER_INDEX).requestRedraw();
    }


    private void onLongPress(LatLong tapLatLong) {
          testLocationOnPress(tapLatLong);
        markingDanger(tapLatLong, "", colorDanger);
        activity.getWarning(1, "Skjaer ahead!");
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
                Double meterRetur = latlongtest.sphericalDistance(myLocation, latLong);
                if(meterRetur <= 1000){
                    List<Tag> tags = pointOfInterest.tags;
                    for (Tag tag : tags) {
                        String checkKey, tagitem;
                        tagitem = tag.key.toString();
                        checkKey = tag.key.toString();
                        if (checkKey.contains("skjaer")) {
                            activity.getWarning(1, "Skjaer ahead!");
                            markingDanger(pointOfInterest.position, tagitem, colorDanger);
                        }
                    }
                }
            }
        } catch (AssertionFailedError ex) {
            Log.d(TAG, " Assertion failed on " + ex.getMessage());
        }
    }




/** Bruker AssesRendertheme klassen til å hente en rendertheme, returnerer null dersom det er feil */
    private XmlRenderTheme getRenderTheme() {
        try {
            return new AssetsRenderTheme(this.getActivity(), getRenderThemePrefix(), getRenderThemeFile(), this);
        } catch (IOException e) {
            Log.e("DD", "Render theme failure " + e.toString());
        }
        return null;
    }

    private String getRenderThemePrefix() {
        return "";
    }

/** Henter rendertheme fila */
    private String getRenderThemeFile() {
        return "renderthemes/rendertheme-v4.xml";
    }

    /** Laqger layer ved bruk av en render theme, tegner opp kartet*/
    private void createMapLayer() {

            MapDataStore mapDataStore = new MapFile(getMapFile());
            TileRendererLayer tileRendererLayer = new TileRendererLayer(
                    tileCache, mapDataStore,
                    this.mapView.getModel().mapViewPosition,
                    false, true, AndroidGraphicFactory.INSTANCE) {
                @Override
                public boolean onLongPress(LatLong tapLatLong, Point thisXY,
                                           Point tapXY) {
                    KartView.this.onLongPress(tapLatLong);
                    return true;
                }
            };
            tileRendererLayer.setXmlRenderTheme(getRenderTheme());
            this.mapView.getLayerManager().getLayers().add(tileRendererLayer);


    }

    /** Lager en tilecatch som lagrer layers så vi slipper å tegne den på nytt*/
    private void createTileCache() {

        this.tileCache = AndroidUtil.createTileCache(this.getActivity(), "mapcache",
                mapView.getModel().displayModel.getTileSize(), 1f,
                this.mapView.getModel().frameBufferModel.getOverdrawFactor());
    }

    /** Henter mapfila i SD kortet*/
    private File getMapFile() {
        return new File(Environment.getExternalStorageDirectory(), getMapFileName());
    }

    private String getMapFileName() {
        return "norway.map";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mapView.getLayerManager().getLayers().isEmpty()) {
            mapView.destroyAll();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        northProvider.start();
        setCenter(myLocation);
    }

    @Override
    public void onPause(){
        super.onPause();
        northProvider.stop();
    }

    private void setCenter(LatLong latLng){
        if (!this.mapView.getLayerManager().getLayers().isEmpty()) {
            this.mapView.getModel().mapViewPosition.setCenter(latLng);
            this.mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);
        }
    }

    @Override
    public Set<String> getCategories(XmlRenderThemeStyleMenu style) {
        return null;
    }
}