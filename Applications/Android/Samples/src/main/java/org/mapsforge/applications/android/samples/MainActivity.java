package org.mapsforge.applications.android.samples;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.*;
import android.widget.*;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Long Huynh on 09.02.2016.
 */
public class MainActivity extends Activity  { //implements NavigationDrawerFragment.NavigationDrawerCallbacks
    ListView varselListView;
    List<String> varselList;
    ArrayAdapter<String> warningAdapter;
    NotificationManager notificationManager;
    boolean varselOpen = false;
    static Button notifCount;
    static int mNotifCount = 0;

    private CharSequence mTitle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private String[] getmNavigationTitles() {
        return new String[] {
                getString(R.string.title_section1),
                getString(R.string.title_section2),
                getString(R.string.title_section3),
                getString(R.string.action_settings)

        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        AndroidGraphicFactory.createInstance(this.getApplication());
        setContentView(R.layout.activity_main);

        this.mTitle = "SafeSail";

        this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.mDrawerList = (ListView) findViewById(R.id.navigation_drawer);

//        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        this.mDrawerList.setAdapter(
                new ArrayAdapter<>(
                        this,
                        R.layout.drawer_list_item,
                        this.getmNavigationTitles()
                )
        );
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

        //Kjører en test varsel
//        getVarsel(1, "Death incoming");
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
                break;
            case 3:
                fragment = new SettingsFragment();
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
        mNotifCount = +count;
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
    private void getVarsel(int mNotificationId, String innmelding){
        String time = DateFormat.getDateTimeInstance().format(new Date());

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String melding = innmelding + "         " + time;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.varsel);
        mBuilder.setContentTitle("SafeSail");
        mBuilder.setContentText(melding);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setPriority(2);
        // builder.setSound(Uri.parse("uri://sadfasdfasdf.mp3"));
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        notificationManager.notify(mNotificationId, mBuilder.build());
        setNotifCount(1);
        varselList.add(melding);
        warningAdapter.notifyDataSetChanged();
    }


}