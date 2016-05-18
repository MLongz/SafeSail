package no.hsn.sailsafe;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Long Huynh on 02.05.2016.
 */
public class NavigasjonArrayAdapter extends ArrayAdapter<NavItems> {
    private  Context context;
    private  List<NavItems> navItems;
    private int resources;
    TextView textView;
    ImageView imageView;

    public NavigasjonArrayAdapter(Context context, int resources, List<NavItems> navItems){
        super(context, resources, navItems);
        this.resources = resources;
        this.context = context;
        this.navItems = navItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(this.resources, parent, false);
        textView = (TextView) rowView.findViewById(R.id.text_nav);
        imageView = (ImageView) rowView.findViewById(R.id.icon_nav);

        Drawable d = navItems.get(position).getIcon();
        imageView.setImageDrawable(d);
        textView.setText(navItems.get(position).getNavn());

        return rowView;
    }
}
