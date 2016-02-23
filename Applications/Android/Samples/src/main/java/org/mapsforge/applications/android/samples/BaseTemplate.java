package org.mapsforge.applications.android.samples;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.AndroidPreferences;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.MapWorkerPool;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;
import org.mapsforge.map.scalebar.ImperialUnitAdapter;
import org.mapsforge.map.scalebar.MetricUnitAdapter;
import org.mapsforge.map.scalebar.NauticalUnitAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Long Huynh on 10.02.2016.
 */
public abstract class BaseTemplate extends MainActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected MapView mapView;
    protected PreferencesFacade preferencesFacade;
    protected XmlRenderThemeStyleMenu renderThemeStyleMenu;
    protected List<TileCache> tileCaches = new ArrayList<TileCache>();

	/*
	 * Abstract methods that must be implemented.
	 */

    /**
     * @return the layout to be used,
     */
    /**protected abstract int getLayoutId();*/

    /**
     * @return the id of the mapview inside the layout.
     */
    /**protected abstract int getMapViewId();*/

    /**
     * Gets the name of the map file.
     * The directory for the file is supplied by getMapFileDirectory()
     *
     * @return the map file name to be used
     */
    /**protected abstract String getMapFileName();*/

    /**
     * @return the rendertheme for this viewer
     */
    protected abstract XmlRenderTheme getRenderTheme();

    /**
     * Hook to create map layers. You will need to create at least one layer to
     * have something visible on the map.
     */
    /**protected abstract void createLayers();*/

    /**
     * Hook to create tile caches. For most map viewers you will need a tile cache
     * for the renderer. If you do not need tilecaches just provide an empty implementation.
     */
    /**protected abstract void createTileCaches();*/

    /**
     * Hook to create controls, such as scale bars.
     * You can add more controls.
     */
  /** protected void createControls() {
        // hook for control creation
    }*/

    /**
     * The MaxTextWidthFactor determines how long a text may be before it is line broken. The
     * default setting should be good enough for most apps.
     * @return the maximum text width factor for line breaking captions
     */
    protected float getMaxTextWidthFactor() {
        return 0.7f;
    }

    /**
     * @return the default starting zoom level if nothing is encoded in the map file.
     */
    protected byte getZoomLevelDefault() {
        return (byte) 12;
    }

    /**
     * @return the minimum zoom level of the map view.
     */
    protected byte getZoomLevelMin() {
        return (byte) 0;
    }

    /**
     * @return the maximum zoom level of the map view.
     */
    protected byte getZoomLevelMax() {
        return (byte) 24;
    }

    /**
     * Template method to create the map views.
     */
    protected void createMapViews() {
        mapView = getMapView();
        mapView.getModel().init(this.preferencesFacade);
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(hasZoomControls());
        mapView.getMapZoomControls().setAutoHide(isZoomControlsAutoHide());
        mapView.getMapZoomControls().setZoomLevelMin(getZoomLevelMin());
        mapView.getMapZoomControls().setZoomLevelMax(getZoomLevelMax());
        initializePosition(mapView.getModel().mapViewPosition);
    }

    /**
     * Creates the shared preferences that are being used to store map view data over
     * activity restarts.
     */
    /**protected void createSharedPreferences() {
        this.preferencesFacade = new AndroidPreferences(this.getSharedPreferences(getPersistableId(), MODE_PRIVATE));
    }*/

    /**
     * Gets the default initial position of a map view if nothing is set in the map file. This
     * operation is used as a fallback only. Override this if you are not sure if your map file
     * will always have an initial position.
     * @return the fallback initial position of the mapview.
     */
   /** protected MapPosition getDefaultInitialPosition() {
        return new MapPosition(new LatLong(0, 0), getZoomLevelDefault());
    }*/

    /**
     * Extracts the initial position from the map file, falling back onto the value supplied
     * by getDefaultInitialPosition if there is no initial position coded into the map file.
     * You will only need to override this method if you do not want the initial position extracted
     * from the map file.
     * @return the initial position encoded in the map file or a fallback value.
     */
    protected MapPosition getInitialPosition() {
        MapDataStore mapFile = getMapFile();

        if (mapFile.startPosition() != null) {
            Byte startZoomLevel = mapFile.startZoomLevel();
            if (startZoomLevel == null) {
                // it is actually possible to have no start zoom level in the file
                startZoomLevel = new Byte((byte) 12);
            }
            return new MapPosition(mapFile.startPosition(), startZoomLevel);
        } else {
            return getDefaultInitialPosition();
        }
    }

    /**
     * Provides the directory of the map file, by default the Android external storage
     * directory (e.g. sdcard).
     * @return
     */
    protected File getMapFileDirectory() {
        return Environment.getExternalStorageDirectory();
    }

    /**
     * Combines map file directory and map file to a map file.
     * This method usually will not need to be changed.
     * @return a map file interface
     */
    protected MapDataStore getMapFile() {
        return new MapFile(new File(getMapFileDirectory(), this.getMapFileName()));
    }

    /**
     * The persistable ID is used to store settings information, like the center of the last view
     * and the zoomlevel. By default the simple name of the class is used. The value is not user
     * visibile.
     * @return the id that is used to save this mapview.
     */
    protected String getPersistableId() {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns the relative size of a map view in relation to the screen size of the device. This
     * is used for cache size calculations.
     * By default this returns 1.0, for a full size map view.
     * @return the screen ratio of the mapview
     */
    protected float getScreenRatio() {
        return 1.0f;
    }

    /**
     * Configuration method to set if a map view activity will have zoom controls.
     * @return true if the map has standard zoom controls.
     */
    protected boolean hasZoomControls() {
        return true;
    }

    /**
     * Configuration method to set if map view activity's zoom controls hide automatically.
     * @return true if zoom controls hide automatically.
     */
    protected boolean isZoomControlsAutoHide() {
        return true;
    }

    /**
     * initializes the map view position.
     *
     * @param mvp
     *            the map view position to be set
     * @return the mapviewposition set
     */
    protected MapViewPosition initializePosition(MapViewPosition mvp) {
        LatLong center = mvp.getCenter();

        if (center.equals(new LatLong(0, 0))) {
            mvp.setMapPosition(this.getInitialPosition());
        }
        mvp.setZoomLevelMax(getZoomLevelMax());
        mvp.setZoomLevelMin(getZoomLevelMin());
        return mvp;
    }

    /**
     * Android Activity life cycle method.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createSharedPreferences();
        createMapViews();
        createTileCaches();
        createLayers();
        createControls();
    }

    /**
     * Android Activity life cycle method.
     */
    @Override
    protected void onPause() {
        mapView.getModel().save(this.preferencesFacade);
        this.preferencesFacade.save();
        super.onPause();
    }

    /**
     * Android Activity life cycle method.
 */

    /**
     * Hook to purge tile caches.
     * By default we purge every tile cache that has been added to the tileCaches list.
     */
    protected void purgeTileCaches() {
        for (TileCache tileCache : tileCaches) {
            tileCache.purge();
        }
        tileCaches.clear();
    }

    protected void redrawLayers() {
        mapView.getLayerManager().redrawLayers();
    }

    /**
     * sets the content view if it has not been set already.
     */
    protected void setContentView() {
        setContentView(mapView);
    }

    /**
     * Creates a map view using an XML layout file supplied by getLayoutId() and finds
     * the map view component inside it with getMapViewId().
     * @return the Android MapView for this activity.
     */
    protected MapView getMapView() {
        setContentView(getLayoutId());
        return (MapView) findViewById(getMapViewId());
    }
    
    /**   -------------------------------------------------------- */

    public static final String SETTING_SCALEBAR = "scalebar";
    public static final String SETTING_SCALEBAR_METRIC = "metric";
    public static final String SETTING_SCALEBAR_IMPERIAL = "imperial";
    public static final String SETTING_SCALEBAR_NAUTICAL = "nautical";
    public static final String SETTING_SCALEBAR_BOTH = "both";
    public static final String SETTING_SCALEBAR_NONE = "none";

    protected static final int DIALOG_ENTER_COORDINATES = 2923878;
    protected SharedPreferences sharedPreferences;


    protected int getLayoutId() {
        return R.layout.mapviewer;
    }


    protected int getMapViewId() {
        return R.id.mapView;
    }


    protected MapPosition getDefaultInitialPosition() {
        return new MapPosition(new LatLong(52.517037, 13.38886), (byte) 12);
    }


    protected void createLayers() {
        TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(this.tileCaches.get(0),
                mapView.getModel().mapViewPosition, getMapFile(), getRenderTheme(), false, true);
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);

        // needed only for samples to hook into Settings.
        setMaxTextWidthFactor();
    }


    protected void createControls() {
        setMapScaleBar();
    }

    protected void createTileCaches() {
        boolean persistent = sharedPreferences.getBoolean(SamplesApplication.SETTING_TILECACHE_PERSISTENCE, true);

        this.tileCaches.add(AndroidUtil.createTileCache(this, getPersistableId(),
                this.mapView.getModel().displayModel.getTileSize(), this.getScreenRatio(),
                this.mapView.getModel().frameBufferModel.getOverdrawFactor(), persistent
        ));
    }

    /**
     * @return the map file name to be used
     */
    protected String getMapFileName() {
        return "germany.map";
    }

    protected void onDestroy() {
        this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        mapView.destroyAll();
        tileCaches.clear();
        super.onDestroy();
    }

	/*
	 * Settings related methods.
	 */


    protected void createSharedPreferences() {
        this.preferencesFacade = new AndroidPreferences(this.getSharedPreferences(getPersistableId(), MODE_PRIVATE));

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // problem that the first call to getAll() returns nothing, apparently the
        // following two calls have to be made to read all the values correctly
        // http://stackoverflow.com/questions/9310479/how-to-iterate-through-all-keys-of-shared-preferences
        this.sharedPreferences.edit().clear();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressLint("InflateParams")
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        switch (id) {
            case DIALOG_ENTER_COORDINATES:
                builder.setIcon(android.R.drawable.ic_menu_mylocation);
                builder.setTitle(R.string.dialog_location_title);
                final View view = factory.inflate(R.layout.dialog_enter_coordinates, null);
                builder.setView(view);
                builder.setPositiveButton(R.string.okbutton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double lat = Double.parseDouble(((EditText) view.findViewById(R.id.latitude)).getText()
                                .toString());
                        double lon = Double.parseDouble(((EditText) view.findViewById(R.id.longitude)).getText()
                                .toString());
                        byte zoomLevel = (byte) ((((SeekBar) view.findViewById(R.id.zoomlevel)).getProgress()) +
                                BaseTemplate.this.mapView.getModel().mapViewPosition.getZoomLevelMin());

                        BaseTemplate.this.mapView.getModel().mapViewPosition.setMapPosition(
                                new MapPosition(new LatLong(lat, lon), zoomLevel));
                    }
                });
                builder.setNegativeButton(R.string.cancelbutton, null);
                return builder.create();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_preferences:
                intent = new Intent(this, Settings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                if (renderThemeStyleMenu != null) {
                    intent.putExtra(Settings.RENDERTHEME_MENU, renderThemeStyleMenu);
                }
                startActivity(intent);
                return true;
            case R.id.menu_position_enter_coordinates:
                showDialog(DIALOG_ENTER_COORDINATES);
                break;
            case R.id.menu_svgclear:
                AndroidGraphicFactory.clearResourceFileCache();
                break;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPrepareDialog(int id, final Dialog dialog) {
        if (id == DIALOG_ENTER_COORDINATES) {
            MapViewPosition currentPosition = BaseTemplate.this.mapView.getModel().mapViewPosition;
            LatLong currentCenter = currentPosition.getCenter();
            EditText editText = (EditText) dialog.findViewById(R.id.latitude);
            editText.setText(Double.toString(currentCenter.latitude));
            editText = (EditText) dialog.findViewById(R.id.longitude);
            editText.setText(Double.toString(currentCenter.longitude));
            SeekBar zoomlevel = (SeekBar) dialog.findViewById(R.id.zoomlevel);
            zoomlevel.setMax(currentPosition.getZoomLevelMax() - currentPosition.getZoomLevelMin());
            zoomlevel.setProgress(BaseTemplate.this.mapView.getModel().mapViewPosition.getZoomLevel()
                    - currentPosition.getZoomLevelMin());
            final TextView textView = (TextView) dialog.findViewById(R.id.zoomlevelValue);
            textView.setText(String.valueOf(zoomlevel.getProgress()));
            zoomlevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textView.setText(String.valueOf(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                    // nothing
                }

                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                    // nothing
                }
            });
        } else {
            super.onPrepareDialog(id, dialog);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (SamplesApplication.SETTING_SCALE.equals(key)) {
            this.mapView.getModel().displayModel.setUserScaleFactor(DisplayModel.getDefaultUserScaleFactor());
            Log.d(SamplesApplication.TAG, "Tilesize now " + this.mapView.getModel().displayModel.getTileSize());
            AndroidUtil.restartActivity(this);
        }
        if (SamplesApplication.SETTING_TILECACHE_PERSISTENCE.equals(key)) {
            if (!preferences.getBoolean(SamplesApplication.SETTING_TILECACHE_PERSISTENCE, false)) {
                Log.d(SamplesApplication.TAG, "Purging tile caches");
                for (TileCache tileCache : this.tileCaches) {
                    tileCache.purge();
                }
            }
            AndroidUtil.restartActivity(this);
        }
        if (SamplesApplication.SETTING_TEXTWIDTH.equals(key)) {
            AndroidUtil.restartActivity(this);
        }
        if (SETTING_SCALEBAR.equals(key)) {
            setMapScaleBar();
        }
        if (SamplesApplication.SETTING_DEBUG_TIMING.equals(key)) {
            MapWorkerPool.DEBUG_TIMING = preferences.getBoolean(SamplesApplication.SETTING_DEBUG_TIMING, false);
        }
        if (SamplesApplication.SETTING_RENDERING_THREADS.equals(key)) {
            MapWorkerPool.NUMBER_OF_THREADS = Integer.parseInt(preferences.getString(SamplesApplication.SETTING_RENDERING_THREADS, Integer.toString(MapWorkerPool.DEFAULT_NUMBER_OF_THREADS)));
            AndroidUtil.restartActivity(this);
        }
        if (SamplesApplication.SETTING_WAYFILTERING_DISTANCE.equals(key) ||
                SamplesApplication.SETTING_WAYFILTERING.equals(key)) {
            MapFile.wayFilterEnabled = preferences.getBoolean(SamplesApplication.SETTING_WAYFILTERING, true);
            if (MapFile.wayFilterEnabled) {
                MapFile.wayFilterDistance = Integer.parseInt(preferences.getString(SamplesApplication.SETTING_WAYFILTERING_DISTANCE, "20"));
            }
        }
    }

    /**
     * Sets the scale bar from preferences.
     */
    protected void setMapScaleBar() {
        String value = this.sharedPreferences.getString(SETTING_SCALEBAR, SETTING_SCALEBAR_BOTH);

        if (SETTING_SCALEBAR_NONE.equals(value)) {
            AndroidUtil.setMapScaleBar(this.mapView, null, null);
        } else {
            if (SETTING_SCALEBAR_BOTH.equals(value)) {
                AndroidUtil.setMapScaleBar(this.mapView, MetricUnitAdapter.INSTANCE, ImperialUnitAdapter.INSTANCE);
            } else if (SETTING_SCALEBAR_METRIC.equals(value)) {
                AndroidUtil.setMapScaleBar(this.mapView, MetricUnitAdapter.INSTANCE, null);
            } else if (SETTING_SCALEBAR_IMPERIAL.equals(value)) {
                AndroidUtil.setMapScaleBar(this.mapView, ImperialUnitAdapter.INSTANCE, null);
            } else if (SETTING_SCALEBAR_NAUTICAL.equals(value)) {
                AndroidUtil.setMapScaleBar(this.mapView, NauticalUnitAdapter.INSTANCE, null);
            }
        }
    }

    /**
     * sets the value for breaking line text in labels.
     */
    protected void setMaxTextWidthFactor() {
        mapView.getModel().displayModel.setMaxTextWidthFactor(Float.valueOf(sharedPreferences.getString(SamplesApplication.SETTING_TEXTWIDTH, "0.7")));
    }
    
}
