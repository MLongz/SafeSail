package no.hsn.sailsafe;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

/**
 * Extension of the Marker class to support rotation of drawable objects
 * Created by Long Hyunh on 09.03.2016.
 */
public class Rotating extends Marker {
    private float rotation;

    /**
     *
     * @param latLong Object containing latitude and longitude coordinates of the rotating object
     * @param bitmap Bitmap object containing the drawable resource
     * @param horizontalOffset Horizontal offset of the bitmap object
     * @param verticalOffset Vertical offset of the bitmap object
     * @param rotation Degrees rotation to apply to the bitmap object
     */
    public Rotating(LatLong latLong, Bitmap bitmap, int horizontalOffset,
                    int verticalOffset, float rotation) {
        super(latLong, bitmap, horizontalOffset, verticalOffset);
        this.rotation = rotation;
    }

    /**
     * Draw method override to support rotating of map marker objects
     * @param boundingBox An area defined by two sets of latitude and longitude coordinates
     * @param zoomLevel Set zoom level from 1-21 for the drawn item
     * @param canvas The canvas object to draw the marker on
     * @param topLeftPoint The screen coordinate of the top leftmost point of the visible area
     */
    @Override
    public void draw(BoundingBox boundingBox, byte zoomLevel, org.mapsforge.core.graphics.Canvas canvas, Point topLeftPoint) {
        android.graphics.Canvas androidCanvas = AndroidGraphicFactory.getCanvas(canvas);
        androidCanvas.save();

        long mapSize = MercatorProjection.getMapSize(zoomLevel, this.displayModel.getTileSize());
        double pixelX =  MercatorProjection.longitudeToPixelX(this.getLatLong().longitude, mapSize);
        double pixelY =  MercatorProjection.latitudeToPixelY(this.getLatLong().latitude, mapSize);

        int halfBitmapWidth = this.getBitmap().getWidth() / 2;
        int halfBitmapHeight = this.getBitmap().getHeight() / 2;

        int left = (int) (pixelX - topLeftPoint.x - halfBitmapWidth + this.getHorizontalOffset());
        int top = (int) (pixelY - topLeftPoint.y - halfBitmapHeight + this.getVerticalOffset());

        androidCanvas.rotate(getRotation(), (float) (pixelX - topLeftPoint.x), (float) (pixelY - topLeftPoint.y));
        canvas.drawBitmap(this.getBitmap(), left, top);

        androidCanvas.restore();
    }

    /**
     * Gets the current rotation of the drawable object
     * @return float value representing the rotation in degrees
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Sets the current rotation of the drawable object
     * @param rotation float value representing the rotation in degrees
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    /**
     * Sets the current location of the drawable object
     * @param location The latitude and longitude location of the drawable object
     */
    public void setLocation(LatLong location) {
        super.setLatLong(location);
    }
}

