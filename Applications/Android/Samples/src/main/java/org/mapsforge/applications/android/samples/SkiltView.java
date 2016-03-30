package org.mapsforge.applications.android.samples;

import android.app.Fragment;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by M. Long on 15.03.2016.
 */
public class SkiltView extends Fragment {
    ListView skiltlistView;
    List<String> skiltliste;
    ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.skilt_view, container, false);

        skiltlistView = (ListView)v.findViewById(R.id.skilt_list_view);
        skiltliste = new ArrayList<>();
        adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, skiltliste);
        skiltlistView.setAdapter(adapter);

        Resources res = getResources();//Get resource
        TypedArray ta = res.obtainTypedArray(R.array.overview_signs);//Get array tabellen som inneholder alle de andre tabell id'ene
        int arraySize = ta.length();

        for(int i = 0; i < arraySize; i++){
            int id = ta.getResourceId(i, 0);//Hent id'en som ligger i array overview_signs tabellen
            if(id > 0){
                String [] array = res.getStringArray(id);//Hent den riktige string tabellen og legger den i array
                TypedArray ta2 = res.obtainTypedArray(id);//Henter en typedarray for å kunne få tak i icon ID
                int iconID = ta2.getResourceId(1, 0);
                String type = array[1].toString();
                String navn = array[2].toString();
                String beskrivelse = array[3].toString();
                skiltliste.add(new Skilt(navn, type, beskrivelse, iconID).toString());
                adapter.notifyDataSetChanged();
            }
        }
        ta.recycle();

        return v;
    }

    private void hentSkilt() {

    }



}
