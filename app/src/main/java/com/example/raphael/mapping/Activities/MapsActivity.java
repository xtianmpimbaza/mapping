package com.example.raphael.mapping.Activities;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.raphael.mapping.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    ArrayList<LatLng> pointsArr = null;
    PolylineOptions polylineOptions;

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

        //collecting co-ordinate points
        pointsArr = new ArrayList<LatLng>();
        pointsArr.add(new LatLng(-37.81319, 144.96298));
        pointsArr.add(new LatLng(-31.95285, 115.85734));
        pointsArr.add(new LatLng(-20.95285, 105.85734));
        pointsArr.add(new LatLng(-21.95285, 117.85734));
        pointsArr.add(new LatLng(-37.81319, 144.96298));

        drawPolyline();
    }

    private void drawPolyline() {

        mMap.addPolygon(new PolygonOptions()
                .addAll(pointsArr)
                .strokeColor(Color.RED)
                .fillColor(Color.parseColor("#51000000")).strokeWidth(2));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.81319, 144.96298), 4));


    }
}
