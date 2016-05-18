package no.hsn.sailsafe;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import org.mapsforge.map.rendertheme.XmlRenderThemeStyleLayer;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by hakonst on 01.05.16.
 */
public class FiltreringFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    SharedPreferences prefs;
    public static final String RENDERTHEME_MENU = "renderthememenu";
    PreferenceCategory renderthemeMenu;
    XmlRenderThemeStyleMenu renderthemeOptions;
    ListPreference baseLayerPreference;
    // I AM ULTRON
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.filtrering);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Bundle b = this.getArguments();
        renderthemeOptions = (XmlRenderThemeStyleMenu) b.getSerializable("rendertheme");
        if (renderthemeOptions != null){
            this.renderthemeMenu = (PreferenceCategory) findPreference(RENDERTHEME_MENU);
            createRenderthemeMenu();
        }
    }

    private void createRenderthemeMenu(){

        this.renderthemeMenu.removeAll();

        this.baseLayerPreference = new ListPreference(getActivity());




        Map<String, XmlRenderThemeStyleLayer> baseLayers = renderthemeOptions.getLayers();

        //String language = Locale.getDefault().getLanguage();
        String language = "no";
        Log.d(SailsafeApplication.TAG, language);
        int visibleStyles = 0;
        for (XmlRenderThemeStyleLayer baseLayer : baseLayers.values()) {
            if (baseLayer.isVisible()) {
                ++visibleStyles;
            }
        }

        CharSequence[] entries = new CharSequence[visibleStyles];
        CharSequence[] values = new CharSequence[visibleStyles];
        int i = 0;
        for (XmlRenderThemeStyleLayer baseLayer : baseLayers.values()) {
            if (baseLayer.isVisible()) {
                // build up the entries in the list
                entries[i] = baseLayer.getTitle(language);
                values[i] = baseLayer.getId();
                ++i;
            }
        }

        baseLayerPreference.setEntries(entries);
        baseLayerPreference.setEntryValues(values);
        baseLayerPreference.setEnabled(true);
        baseLayerPreference.setPersistent(true);
        baseLayerPreference.setDefaultValue(renderthemeOptions.getDefaultValue());

        renderthemeMenu.addPreference(baseLayerPreference);

        String selection = baseLayerPreference.getValue();
        // need to check that the selection stored is actually a valid getLayer in the current
        // rendertheme.
        if (selection == null || !renderthemeOptions.getLayers().containsKey(selection)) {
            selection = renderthemeOptions.getLayer(renderthemeOptions.getDefaultValue()).getId();
        }
        // the new Android style is to display information here, not instruction
        baseLayerPreference.setSummary(renderthemeOptions.getLayer(selection).getTitle(language));

        for (XmlRenderThemeStyleLayer overlay : this.renderthemeOptions.getLayer(selection).getOverlays()) {
            SwitchPreference switchPreference = new SwitchPreference(getActivity());
            switchPreference.setKey(overlay.getId());
            switchPreference.setPersistent(true);
            switchPreference.setTitle(overlay.getTitle(language));
            if (findPreference(overlay.getId()) == null)  {
                // value has never been set, so set from default
                switchPreference.setChecked(overlay.isEnabled());
            }
            this.renderthemeMenu.addPreference(switchPreference);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences,
                                          String key) {

        if (this.renderthemeOptions != null && this.renderthemeOptions.getId().equals(key)) {
            createRenderthemeMenu();
        }
    }


}
