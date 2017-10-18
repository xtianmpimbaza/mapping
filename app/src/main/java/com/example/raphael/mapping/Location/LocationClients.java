package com.example.raphael.mapping.Location;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


/**
 * A helper class for getting a LocationClient based on whether or not Google Play Services are
 * available on the device.
 */
public class LocationClients {

    @Nullable
    private static LocationClient testClient = null;

    public static LocationClient clientForContext(@NonNull Context context) {

        return testClient != null
                ? testClient
                : areGooglePlayServicesAvailable(context)
                ? new GoogleLocationClient(context)
                : new AndroidLocationClient(context);
    }


    public static void setTestClient(@NonNull LocationClient testClient) {
        LocationClients.testClient = testClient;
    }

    private static boolean areGooglePlayServicesAvailable(@NonNull Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(context);

        return status == ConnectionResult.SUCCESS;
    }
}