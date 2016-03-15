package org.mapsforge.applications.android.samples;

import android.os.Bundle;
import android.preference.*;


/**
 * Created by Long Huynh on 10.02.2016.
 */

/**
 * Edit by Knut Johan Hesten on 15.03.2016.
 */
public class SettingsFragment extends PreferenceFragment {

//    private ListPreference mListPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.innstillinger);
    }

//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//        mListPreference = (ListPreference)  getPreferenceManager().findPreference("preference_key");
//        mListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                // insert custom code
//                return true;
//            }
//        });
//        return inflater.inflate(R.layout.settings_fragment, container, false);
//    }
}
