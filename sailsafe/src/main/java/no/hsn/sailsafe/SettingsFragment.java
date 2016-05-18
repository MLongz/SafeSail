package no.hsn.sailsafe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;


/**
 * Created by Long Huynh on 10.02.2016.
 */

/**
 * Edit by Knut Johan Hesten on 15.03.2016.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.innstillinger);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final Preference cachePreference = (Preference) findPreference(getString(R.string.pref_key_cache));
        cachePreference.setSummary(getCacheSizeText(getActivity()));
        cachePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.clearcache_content)
                        .setTitle(R.string.clearcache_title);
                builder.setPositiveButton(R.string.okbutton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Kode for å tømme hurtigbuffer, clear cache
                        deleteCache(getActivity());
                        cachePreference.setSummary(getCacheSizeText(getActivity()));
                        Toast.makeText(getActivity(), "Hurtigbuffer slettet", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.cancelbutton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }
        else if(dir!= null && dir.isFile())
            return dir.delete();
        else {
            return false;
        }
    }

    public String getCacheSizeText(Context context){
        File dir = context.getCacheDir();
        long size = 0;
        File[] files = dir.listFiles();
        for(File f:files){
            size = size + f.length();
        }
        String r = "Hurtigbuffer: " + String.valueOf(size);
        return r;
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
