package org.mapsforge.applications.android.samples;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Long Huynh on 23.02.2016.
 */
public class NotificationView extends Fragment {
    public NotificationView(){
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.notification, container, false);
        return v;
    }
}
