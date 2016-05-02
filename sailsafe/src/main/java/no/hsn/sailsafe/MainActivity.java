package no.hsn.sailsafe;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static no.hsn.sailsafe.R.drawable.varsel;

/**
 * Created by Long Huynh on 09.02.2016.
 */
public class MainActivity extends Activity  { //implements NavigationDrawerFragment.NavigationDrawerCallbacks
    ListView varselListView;
    List<String> varselList;
    ArrayAdapter<String> warningAdapter;
    ArrayAdapter<NavItems> navAdapter;
    NotificationManager notificationManager;
    boolean varselOpen = false;
    static Button notifCount;
    static int mNotifCount = 0;

    private CharSequence mTitle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    SharedPreferences prefs;

    private List<NavItems> getNavigasjonItems() {
        List<NavItems> navItemList = new ArrayList<>();
        Resources res = getResources();//Get resource
        TypedArray ta = res.obtainTypedArray(R.array.navigation_drawer_items_array);//Get array tabellen som inneholder alle de andre tabell id'ene
        int arraySize = ta.length();

        for(int i = 0; i < arraySize; i++){
            int id = ta.getResourceId(i, 0);//Hent id'en som ligger i array overview_signs tabellen
            if(id > 0){
                String [] array = res.getStringArray(id);//Hent den riktige string tabellen og legger den i array
                TypedArray ta2 = res.obtainTypedArray(id);//Henter en typedarray for å kunne få tak i icon ID
                Drawable d = ta2.getDrawable(0);
                String navn = array[1].toString();
                navItemList.add(new NavItems(navn, d));
            }
        }
        ta.recycle();
        return navItemList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        AndroidGraphicFactory.createInstance(this.getApplication());
        setContentView(R.layout.activity_main);

        prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        this.mTitle = "SafeSail";

        this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.mDrawerList = (ListView) findViewById(R.id.navigation_drawer);


//        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        navAdapter = new NavigasjonArrayAdapter(this, getNavigasjonItems());
        this.mDrawerList.setAdapter(navAdapter);
        this.mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                this.mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mTitle);
                mDrawerList.bringToFront();
//                mDrawerLayout.requestLayout();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }

         notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //Setter opp notifikasjon lista og arraList og warningAdapter for å legge inn data dynamisk
        varselListView = (ListView) findViewById(R.id.notification_list_view);
        varselList = new ArrayList<>();
        warningAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, varselList);
        varselListView.setAdapter(warningAdapter);


    }

    /** Lager varsel icon og en button som kan åpne varsel lista*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        final View count = menu.findItem(R.id.varselicon).getActionView();
        notifCount = (Button) count.findViewById(R.id.notif_count);
        notifCount.setText(String.valueOf(mNotifCount));
        notifCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!varselOpen) {
                    varselListView.setVisibility(View.VISIBLE);
                    varselOpen = true;
                }else {
                    varselListView.setVisibility(View.GONE);
                    varselOpen = false;
                    mNotifCount = 0;
                    notifCount.setText(String.valueOf(mNotifCount));
                }
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        Toast.makeText(this, item.getItemId(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

    }

    private void selectItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new KartView();
                break;
            case 1:
                fragment = null;
                break;
            case 2:
                fragment = new SkiltView();
                break;
            case 10:
                fragment = new SettingsFragment();
                break;
            case 4:
                break;
            default:
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    private void setNotifCount(int count){
        //En teller for varsel
        mNotifCount = mNotifCount + count;
        notifCount.setText(String.valueOf(mNotifCount));
        invalidateOptionsMenu();
    }

//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        // Sync the toggle state after onRestoreInstanceState has occurred.
//        mDrawerToggle.syncState();
//    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        mDrawerToggle.onConfigurationChanged(newConfig);
//    }

    /** Her blir varsel laget. Alt av icon, lyd osv kan gjøres her*/
    public void getVarsel(int mNotificationId, String innmelding) {
        try {
            if (prefs.getBoolean(getString(R.string.pref_key_varsel), true)) {
                String time = DateFormat.getDateTimeInstance().format(new Date());

                Intent intent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                String melding = innmelding + "         " + time;
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                mBuilder.setSmallIcon(varsel);
                mBuilder.setContentTitle("SafeSail");
                mBuilder.setContentText(melding);
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setPriority(2);
                // Checks if the sound is turned on in settings:
                if (prefs.getBoolean(getString(R.string.pref_key_varsel_lyd), true)) {
                    // builder.setSound(Uri.parse("uri://sadfasdfasdf.mp3"));
                }
                // Checks if the vibration is turned on in settings:
                if (prefs.getBoolean(getString(R.string.pref_key_varsel_vibrering), true)) {
                    mBuilder.setVibrate(new long[]{1000, 1000, 1000});
                }
                notificationManager.notify(mNotificationId, mBuilder.build());
                setNotifCount(1);
                varselList.add(melding);
                warningAdapter.notifyDataSetChanged();
            }
        } catch (Exception ex) {
            Log.d(SailsafeApplication.TAG, ex.getMessage());
        }
    }
}