package no.hsn.sailsafe;

import android.graphics.drawable.Drawable;

/**
 * Created by M. Long on 15.03.2016.
 */
public class Skilt {
    private String name;
    private String type;
    private String description;
    private Drawable iconID;

    public Skilt(String name, String type, String description, Drawable iconID) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.iconID = iconID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Drawable getIconID() {
        return iconID;
    }

    @Override
    public String toString() {
        return name + "\n" + " Type:" + type + "\n" + " Beskrivelse: " + description;
    }
}
