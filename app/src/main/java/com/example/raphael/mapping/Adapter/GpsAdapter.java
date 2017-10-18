package com.example.raphael.mapping.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.raphael.mapping.R;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by Raphael on 10/1/2017.
 */

public class GpsAdapter extends RecyclerView.Adapter<GpsAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<JsonObject> albumList = new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView FirstName, LastName,Accuracy;

        public MyViewHolder(View view) {
            super(view);
            FirstName = (TextView) view.findViewById(R.id.first_Name);
            LastName = (TextView) view.findViewById(R.id.last_Name);
            Accuracy = (TextView) view.findViewById(R.id.accuracy);
        }
    }


    public GpsAdapter(Context mContext, ArrayList<JsonObject> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gps_lists, parent, false);

//        .inflate(R.layout.farmer_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        try {
            JsonObject ins = albumList.get(position);
            holder.FirstName.setText("Latitude: " + ins.get("Latitude").getAsString());
            holder.LastName.setText("Longitude: " + ins.get("Longitude").getAsString());
            holder.Accuracy.setText("Accuracy: " + ins.get("Accuracy").getAsString() +   "  Metres");
//
        } catch (Exception e) {
            Log.e("GPS_exp", e.toString());
        }
//        holder.overflow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showPopupMenu(holder.overflow);
//            }
//      });
    }


    @Override
    public int getItemCount() {
        return albumList.size();
    }
}