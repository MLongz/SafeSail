/*
 * Copyright 2013-2014 Ludwig M Brinckmann
 * Copyright 2014 devemux86
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.applications.android.samples;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;

/**
 * Start screen for the sample activities.
 */
public class Samples extends Activity {
	// name of the map file in the external storage
	private static final String MAPFILE = "germany.map";

	private MapView mapView;
	private TileCache tileCache;
	private TileRendererLayer tileRendererLayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidGraphicFactory.createInstance(this.getApplication());

		this.mapView = new MapView(this);
		setContentView(this.mapView);

		this.mapView.setClickable(true);
		this.mapView.getMapScaleBar().setVisible(true);
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
		this.mapView.getMapZoomControls().setZoomLevelMax((byte) 20);

		// create a tile cache of suitable size
		this.tileCache = AndroidUtil.createTileCache(this, "mapcache",
				mapView.getModel().displayModel.getTileSize(), 1f,
				this.mapView.getModel().frameBufferModel.getOverdrawFactor());
	}

	@Override
	protected void onStart() {
		super.onStart();

		this.mapView.getModel().mapViewPosition.setCenter(new LatLong(52.517037, 13.38886));
		this.mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);

		// tile renderer layer using internal render theme
		MapDataStore mapDataStore = new MapFile(getMapFile());
		this.tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
				this.mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE);
		tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

		// only once a layer is associated with a mapView the rendering starts
		this.mapView.getLayerManager().getLayers().add(tileRendererLayer);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.mapView.destroyAll();
	}

	private File getMapFile() {
		File file = new File(Environment.getExternalStorageDirectory(), MAPFILE);
		return file;
	}

}