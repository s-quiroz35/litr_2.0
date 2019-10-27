package com.trashboys.litr;

import android.app.Activity;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class LitterAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> locations;
    private final ArrayList<String> imageId;
    public LitterAdapter(Activity context,
                         ArrayList<String> locations, ArrayList<String> imageId) {
        super(context, R.layout.list_unit, locations);
        this.context = context;
        this.locations = locations;
        this.imageId = imageId;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_unit, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(locations.get(position));
        Picasso.get().load(imageId.get(position)).resize(50,50).into(imageView);
        return rowView;
    }

}
