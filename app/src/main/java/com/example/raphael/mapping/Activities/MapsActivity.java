package com.example.raphael.mapping.Activities;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.raphael.mapping.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Serializable {
    private GoogleMap mMap;
    ArrayList<LatLng> pointsArr = null;
    PolylineOptions polylineOptions;
    JSONObject jsonObj;
    JSONArray jsonarray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setPadding(0, 0, 0, 100);

        try {
            Log.e("GPS", getIntent().getStringExtra("gps_points"));
            jsonarray = new JSONArray(getIntent().getStringExtra("gps_points"));
            JSONObject first_point = new JSONObject(jsonarray.getString(0));
            pointsArr = new ArrayList<LatLng>();
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonOb = new JSONObject(jsonarray.getString(i));
                pointsArr.add(new LatLng(Double.valueOf(jsonOb.get("Latitude").toString()), Double.valueOf(jsonOb.get("Longitude").toString())));
            }
            pointsArr.add(new LatLng(Double.valueOf(first_point.get("Latitude").toString()), Double.valueOf(first_point.get("Longitude").toString())));
            drawPolyline(first_point);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void drawPolyline(JSONObject first_point) throws JSONException {

        mMap.addPolygon(new PolygonOptions()
                .addAll(pointsArr)
                .strokeColor(Color.RED)
                .fillColor(Color.parseColor("#51000000")).strokeWidth(2));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(first_point.get("Latitude").toString()), Double.valueOf(first_point.get("Longitude").toString())), 20));

    }
}
