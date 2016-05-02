package no.hsn.sailsafe;

import android.graphics.drawable.Drawable;

/**
 * Created by Long Huynh on 02.05.2016.
 */
public class NavItems {
    private String navn;
    private Drawable icon;

    public NavItems(String navn, Drawable icon) {
        this.navn = navn;
        this.icon = icon;
    }

    public String getNavn() {
        return navn;
    }

    public Drawable getIcon() {
        return icon;
    }
}
