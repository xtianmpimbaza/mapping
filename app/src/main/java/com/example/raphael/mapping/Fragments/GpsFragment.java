package com.example.raphael.mapping.Fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.provider.Settings;
import android.os.Bundle;

import java.io.Serializable;
import java.lang.Math;

import static java.lang.Math.cos;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.raphael.mapping.Activities.MainActivity;
import com.example.raphael.mapping.Activities.MapsActivity;
import com.example.raphael.mapping.Adapter.GpsAdapter;
import com.example.raphael.mapping.Couchdb.CouchdbGPS;
import com.example.raphael.mapping.Globals.GlobalFunctions;
import com.example.raphael.mapping.Globals.Utils;
import com.example.raphael.mapping.Location.LocationClient;
import com.example.raphael.mapping.Location.LocationClients;
import com.example.raphael.mapping.R;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class GpsFragment extends Fragment implements com.google.android.gms.location.LocationListener,
        LocationClient.LocationClientListener {
    double clat, clon;
    TextView TOTAL_ACRES;

    CouchdbGPS couchdbPost;

    // Default values for requesting Location updates.
    private static final long LOCATION_UPDATE_INTERVAL = 100;
    private static final long LOCATION_FASTEST_UPDATE_INTERVAL = 50;
    public static final double DEFAULT_LOCATION_ACCURACY = 5.0;

    private static final String LOCATION_COUNT = "locationCount";

    private ProgressDialog locationDialog;

    private LocationClient locationClient;
    private Location location;

    private double locationAccuracy;
    private int locationCount = 0;

    ArrayList<JsonObject> cordsList = new ArrayList<>();
    ArrayList<Double> latitude = new ArrayList<>();
    ArrayList<Double> longitude = new ArrayList<>();


    public RelativeLayout addGpsLayout, getAcreageLayout, saveLayout, _RecyclerViewLayout, _EmptyRecyclerViewLayout, TOTAL, MapViewLayout;


    RecyclerView recyclerView;
    GpsAdapter adapter;
    CouchdbGPS couchdbGPS;
    private ProgressDialog dialog = null;
    JSONObject jsonObj;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Saving Mpped points...");
//        dialog.setCancelable(true);

        if (savedInstanceState != null) {
            locationCount = savedInstanceState.getInt(LOCATION_COUNT);
        }
        locationAccuracy = DEFAULT_LOCATION_ACCURACY;

        locationClient = LocationClients.clientForContext(getActivity());
        if (locationClient.canSetUpdateIntervals()) {
            locationClient.setUpdateIntervals(LOCATION_UPDATE_INTERVAL, LOCATION_FASTEST_UPDATE_INTERVAL);
        }

        locationClient.setListener(this);


        setupLocationDialog();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gps, container, false);
        addGpsLayout = (RelativeLayout) view.findViewById(R.id.button_home);
        getAcreageLayout = (RelativeLayout) view.findViewById(R.id.button_addcart);
        saveLayout = (RelativeLayout) view.findViewById(R.id.button_buy);
        TOTAL = (RelativeLayout) view.findViewById(R.id.total);
        MapViewLayout = (RelativeLayout) view.findViewById(R.id.map_view);

        TOTAL_ACRES = (TextView) view.findViewById(R.id.acre);


        _RecyclerViewLayout = (RelativeLayout) view.findViewById(R.id.main_content);
        _EmptyRecyclerViewLayout = (RelativeLayout) view.findViewById(R.id.empty_rv);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_insurance);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        couchdbGPS = new CouchdbGPS(getActivity());
        getInsurance();

        addGpsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResume();
            }
        });


        //-------------------------------------------------------------------- calc areage
        getAcreageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (longitude.size() < 4) {
                    Toast.makeText(getActivity(), "You need five points and above to get acreage", Toast.LENGTH_LONG).show();
                } else {

                    getArea(latitude, longitude);
                    TOTAL.setVisibility(View.VISIBLE);
                }
            }
        });

        //-------------------------------------------------------------------- map fragment
        MapViewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (longitude.size() < 2) {
                    Toast.makeText(getActivity(), "You need atleast 3 points", Toast.LENGTH_LONG).show();
                } else {
                    //Toast.makeText(getActivity(), "Go to mapp", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), MapsActivity.class);
                    intent.putExtra("gps_points", cordsList.toString());
                    startActivity(intent);
//                    getArea(latitude, longitude);
//                    TOTAL.setVisibility(View.VISIBLE);
                }
            }
        });


        //-------------------------------------------------------------------- save points to the couch
        saveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (longitude.size() < 4) {
                    Toast.makeText(getActivity(), "A minimum of 5 points is required", Toast.LENGTH_LONG).show();
                } else {
//                    Toast.makeText(getActivity(), "Saving---------------", Toast.LENGTH_LONG).show();
                    uploadPoints();
//                    getArea(latitude, longitude);
//                    TOTAL.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    public void uploadPoints() {
        final JSONObject params = new JSONObject();

        dialog.show();

        try {
            params.put("gps_points", cordsList);
            params.put("unique_id", " ");
            params.put("time", GlobalFunctions.getCurrentTime());

        } catch (JSONException e) {
            Log.e("JSONObject Here", e.toString());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Utils.urlUpload + "/save_mapping", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.e("Message from server", jsonObject.toString());
                        dialog.dismiss();
                        try {
                            if (jsonObject.getInt("status") == 201 && !jsonObject.getJSONObject("result").isNull("id") && !jsonObject.getJSONObject("result").isNull("rev")) {
                                Toast.makeText(getActivity(), "Saved successfully", Toast.LENGTH_LONG).show();
                                Log.e("PARAMS", params.toString());

                            } else {
                                Toast.makeText(getActivity(), "Internet Connection Lost! ", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Internet Connection Lost! ", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);

    }


    @Override
    public void onStart() {
        super.onStart();
        locationClient.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (locationDialog != null) {
            locationDialog.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // We're not using managed dialogs, so we have to dismiss the dialog to prevent it from
        // leaking memory.
        if (locationDialog != null && locationDialog.isShowing()) {
            locationDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        locationClient.stop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LOCATION_COUNT, locationCount);
    }

    // LocationClientListener:


    @Override
    public void onClientStart() {
        locationClient.requestLocationUpdates(this);

        if (locationClient.isLocationAvailable()) {
            logLastLocation();

        } else {
            finishOnError();
        }
    }


    @Override
    public void onClientStartFailure() {
        finishOnError();
    }

    @Override
    public void onClientStop() {

    }

    /**
     * Sets up the look and actions for the progress dialog while the GPS is searching.
     */
    private void setupLocationDialog() {
        // dialog displayed while fetching gps location
        locationDialog = new ProgressDialog(getActivity());
        DialogInterface.OnClickListener geoPointButtonListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                returnLocation();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                location = null;
                                break;
                        }
                    }
                };

        // back button doesn't cancel
        locationDialog.setCancelable(false);
        locationDialog.setIndeterminate(true);
        locationDialog.setIcon(android.R.drawable.ic_dialog_info);
        locationDialog.setTitle(getString(R.string.getting_location));
        locationDialog.setMessage(getString(R.string.please_wait_long));
        locationDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.save_point),
                geoPointButtonListener);
        locationDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel_location),
                geoPointButtonListener);
    }

    private void logLastLocation() {
        Location loc = locationClient.getLastLocation();
        if (loc != null) {
//            Timber.i("lastKnownLocation() lat: %f long: %f acc: %f", loc.getLatitude(), loc.getLongitude(), loc.getAccuracy());

        } else {
//            Timber.i("lastKnownLocation() null location");
        }
    }

    private void returnLocation() {
        if (location != null) {
            getResultStringForLocation(location);

            clat = location.getLatitude();
            clon = location.getLongitude();
            double acc = location.getAccuracy();
            double alt = location.getAltitude();


            //Displaying latitude, longitude, accuracy and altitude
            Log.e("clat", String.valueOf(clat));
            Log.e("Altitude", String.valueOf(alt));
            Log.e("clon", String.valueOf(clon));
            Log.e("accuracy", String.valueOf(acc));


            String point = String.valueOf(clat) + "," + String.valueOf(clon);
            Double Longitude = (clon);
            Double Latitude = (clat);

            //Displaying the coordinates on the screen
            JsonObject pos = new JsonObject();
            pos.addProperty("Latitude", clat);
            pos.addProperty("Longitude", clon);
            pos.addProperty("Accuracy", Math.round(acc * 100d) / 100d);


            //add coordinates in the array
            cordsList.add(pos);
            adapter.notifyDataSetChanged();
            change();
            Log.e("NEW", cordsList.toString());

            latitude.add(Latitude);

//            latitude.add(0.549035);
//            latitude.add(0.548615);
//            latitude.add(0.548473333);
//            latitude.add(0.548778333);
//            latitude.add(0.549035);

//            latitude.add(0.3283567);
//            latitude.add(0.3283783);
//            latitude.add(0.3282117);
//            latitude.add(0.3283233);
//            latitude.add(0.3283567);

//            latitude.add(0.3284567);
//            latitude.add(0.3283933);
//            latitude.add(0.3283133);
//            latitude.add(0.32846);
//            latitude.add(0.3284567);

            Log.e("Latitudes", latitude.toString());

            longitude.add(Longitude);


//            longitude.add(33.561665);
//            longitude.add(33.56371167);
//            longitude.add(33.56363667);
//            longitude.add(33.56164167);
//            longitude.add(33.561665);

//            longitude.add(32.57608);
//            longitude.add(32.57614);
//            longitude.add(32.576055);
//            longitude.add(32.5760117);
//            longitude.add(32.57608);

//            longitude.add(32.5760917);
//            longitude.add(32.5761333);
//            longitude.add(32.5760533);
//            longitude.add(32.5760683);
//            longitude.add(32.5760917);
            Log.e("Longitudes", longitude.toString());

        }

    }

    private void finishOnError() {
        Toast.makeText(getActivity(), R.string.provider_disabled_error, Toast.LENGTH_LONG).show();
        Intent onGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(onGPSIntent);
//        getActivity().finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;

        if (location != null) {
            // Bug report: cached GeoPoint is being returned as the first value.
            // Wait for the 2nd value to be returned, which is hopefully not cached?
            ++locationCount;
//            Timber.i("onLocationChanged(%d) location: %s", locationCount, location);

//            Toast.makeText(this, locationCount, Toast.LENGTH_LONG).show();


            if (locationCount > 1) {
                locationDialog.setMessage(getProviderAccuracyMessage(location));

                if (location.getAccuracy() == locationAccuracy) {
//                    returnLocation();
//                    locationDialog.dismiss();
                }

            } else {
                locationDialog.setMessage(getAccuracyMessage(location));
            }

        } else {
//            Timber.i("onLocationChanged(%d)", locationCount);

//            Toast.makeText(this, locationCount, Toast.LENGTH_LONG).show();

        }
    }

    public String getAccuracyMessage(@NonNull Location location) {
        return getString(R.string.location_accuracy, location.getAccuracy());
    }

    public String getProviderAccuracyMessage(@NonNull Location location) {
        return getString(R.string.location_provider_accuracy, location.getProvider(), truncateDouble(location.getAccuracy()));
    }

    public String getResultStringForLocation(@NonNull Location location) {
        return String.format("%s %s %s %s", location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy());
    }

    private String truncateDouble(float number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }

    public ProgressDialog getLocationDialog() {
        return locationDialog;
    }

    public double getArea(ArrayList<Double> latitudes, ArrayList<Double> longitudes) {
        if (latitudes.size() > 0 && longitudes.size() > 0) {
            Log.e("SIZE", String.valueOf(latitudes.size()));

            double acerage_constant = 0.00024711;
            int e_circum = 40091147;

            latitudes.add(latitudes.get(0));
            longitudes.add(longitudes.get(0));

            Log.e("Final Latitude", latitudes.toString());
            Log.e("Final Longitude", longitudes.toString());

            if (latitudes.size() == longitudes.size() && latitudes.get(0).equals(latitudes.get(latitudes.size() - 1)) && longitudes.get(0).equals(longitudes.get(longitudes.size() - 1))) {

                ArrayList<Double> y = new ArrayList<>();
                for (int i = 0; i < latitudes.size() - 2; i++) {
                    double y_value = ((latitudes.get(i + 1) - latitudes.get(0)) / 360) * e_circum;
                    y.add(y_value);
                }

                Log.e("LATITUDES", y.toString());


                ArrayList<Double> x = new ArrayList<>();
                for (int i = 0; i < longitudes.size() - 2; i++) {
                    double x_cosine = cos((latitudes.get(i + 1) / 180) * (22 / 7));
                    double x_value = ((longitudes.get(i + 1) - longitudes.get(0)) / 360) * e_circum * x_cosine;
                    x.add(x_value);
                }
                Log.e("LONGITUDES", x.toString());


                ArrayList<Double> area = new ArrayList<>();
                for (int i = 0; i < x.size() - 1; i++) {
                    Double area_value = ((y.get(i) * x.get(i + 1)) - (x.get(i) * y.get(i + 1))) / 2;
                    area.add(area_value);
                }
                Log.e("AREA", area.toString());

                double sum = 0;
                for (int j = 0; j < area.size(); j++) {
                    sum += area.get(j);
                }
                double AREA = sum * acerage_constant;
                Log.e("TOTAL AREA", String.valueOf(Math.abs(AREA)));


                TOTAL_ACRES.setText("TOTAL ACRE IS : " + String.valueOf(Math.round(AREA * 10000000d) / 10000000d));
                return AREA;

            } else {

                return 0;
            }

        } else {
            return 0;
        }
    }

    public void getInsurance() {
        try {
            adapter = new GpsAdapter(getActivity(), cordsList);
            recyclerView.setAdapter(adapter);
            cordsList.clear();
            cordsList.addAll(couchdbGPS.allCrops());
            Log.e("TAG", cordsList.toString());
            adapter.notifyDataSetChanged();
            couchdbGPS.sync();
            change();
        } catch (Exception e) {
        }
    }

    void change() {
        if (cordsList.isEmpty()) {
            _RecyclerViewLayout.setVisibility(View.GONE);
            _EmptyRecyclerViewLayout.setVisibility(View.VISIBLE);
        } else {
            _RecyclerViewLayout.setVisibility(View.VISIBLE);
            _EmptyRecyclerViewLayout.setVisibility(View.GONE);
        }
    }
}
