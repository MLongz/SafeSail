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
 * Created by M. Long on 20.04.2016.
 * Lager en costumized arrayadapter for å vå frem den visningen vi vil ha. Det vil si tekst og icon.
 */
public class SkiltArrayAdapter extends ArrayAdapter<Skilt> {
    private final Context context;
    private final List<Skilt> skilt;

    public SkiltArrayAdapter(Context context,  List<Skilt> skilt){
        super(context, R.layout.skilt_list, skilt);
        this.context = context;
        this.skilt = skilt;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.skilt_list, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon_skilt);
        Drawable d = skilt.get(position).getIconID();
        imageView.setImageDrawable(d);
        textView.setText(skilt.get(position).toString());

        return rowView;
    }
}