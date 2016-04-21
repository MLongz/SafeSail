package no.hsn.sailsafe;

import android.graphics.drawable.Drawable;

/**
 * Created by M. Long on 15.03.2016.
 */
public class Skilt {
    private String navn;
    private String type;
    private String beskrivelse;
    private Drawable iconID;

    public Skilt(String navn, String type, String beskrivelse, Drawable iconID) {
        this.navn = navn;
        this.type = type;
        this.beskrivelse = beskrivelse;
        this.iconID = iconID;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public Drawable getIconID() {
        return iconID;
    }

    public void setIconID(Drawable iconID) {
        this.iconID = iconID;
    }

    @Override
    public String toString() {
        return navn + "\n" + " Type:" + type + "\n" + " Beskrivelse: " + beskrivelse;
    }
}
