package org.mapsforge.applications.android.samples;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

/**
 * Created by M. Long on 09.03.2016.
 */
public class Rotating extends Marker {
    private float rotation;

    public Rotating(LatLong latLong, Bitmap bitmap, int horizontalOffset,
                          int verticalOffset, float rotation) {
        super(latLong, bitmap, horizontalOffset, verticalOffset);
        this.rotation = rotation;
    }


    @Override
    public void draw(BoundingBox boundingBox, byte zoomLevel, org.mapsforge.core.graphics.Canvas canvas, Point topLeftPoint)
    {

        android.graphics.Canvas androidCanvas = AndroidGraphicFactory.getCanvas(canvas);
        androidCanvas.save();

        long mapSize = MercatorProjection.getMapSize(zoomLevel, this.displayModel.getTileSize());
        double pixelX =  MercatorProjection.longitudeToPixelX(this.getLatLong().longitude, mapSize);
        double pixelY =  MercatorProjection.latitudeToPixelY(this.getLatLong().latitude, mapSize);

        int halfBitmapWidth = this.getBitmap().getWidth() / 2;
        int halfBitmapHeight = this.getBitmap().getHeight() / 2;

        int left = (int) (pixelX - topLeftPoint.x - halfBitmapWidth + this.getHorizontalOffset());
        int top = (int) (pixelY - topLeftPoint.y - halfBitmapHeight + this.getVerticalOffset());
        //androidCanvas.rotate(rotation, canvas.getWidth()/2, canvas.getHeight()/2);
        //androidCanvas.rotate(rotation, pixelX, pixelY);

        androidCanvas.rotate(getRotation(), (float) (pixelX - topLeftPoint.x), (float) (pixelY - topLeftPoint.y));
        canvas.drawBitmap(this.getBitmap(), left, top);

        //super.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
        androidCanvas.restore();

    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
}

