package no.hsn.sailsafe;

import android.graphics.drawable.Drawable;

/**
 * Created by Long Huynh on 02.05.2016.
 */
public class NavItems {
    private String name;
    private Drawable icon;


    public NavItems(String name, Drawable icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }
}
