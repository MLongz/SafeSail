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
public class SignArrayAdapter extends ArrayAdapter<Skilt> {
    private final Context context;
    private final List<Skilt> signList;

    public SignArrayAdapter(Context context, List<Skilt> signList){
        super(context, R.layout.skilt_list, signList);
        this.context = context;
        this.signList = signList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.skilt_list, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon_skilt);
        Drawable d = signList.get(position).getIconID();
        imageView.setImageDrawable(d);
        textView.setText(signList.get(position).toString());

        return rowView;
    }
}