package com.project.sc2006.controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.project.sc2006.R;

import java.util.List;

public class DirectionsListAdapter extends ArrayAdapter<RouteController.direction> {
    private int resourceLayout;
    private Context mContext;
    public DirectionsListAdapter(Context context, List<RouteController.direction> items) {
        super(context, R.layout.direction_list_one, items);
        this.resourceLayout = R.layout.direction_list_one;
        this.mContext = context;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(resourceLayout, null);
        }
        RouteController.direction p = getItem(position);
        if (p != null) {
            TextView tt1 = view.findViewById(R.id.direction_mode);
            TextView tt2 = view.findViewById(R.id.direction_text);
            if (tt1 != null) {
                tt1.setText(p.mode);
            }
            if (tt2 != null) {
                tt2.setText(p.instructions);
            }
        }
        return view;
    }
}

