package com.cbitlabs.geoipcollector;

/**
 * Created by stuart on 11/25/13.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

    LocationListener llNet;
    LocationListener llGps;

    public void onReceive(final Context context, final Intent intent) {
        Log.d(Util.TAG, "--------------------------------");
        Log.d(Util.TAG, "NetworkStateReceiver.onReceive()");

        if (Util.getWifiConnectionState(context)) {

            GeoPoint p = Util.getLocation(context);

            if (GeoPoint.isValidPoint(p)) {
                createDataTask(context);
            } else {
                Log.i(Util.TAG, "Location isn't accurate enough, registering location listener");
                updateLocation(context);
            }
        }
    }

    public void updateLocation(final Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String method = prefs.getString(Util.PREF_KEY_LOC_METHOD, "network");
        if (method.equals("gps")) {
            updateLocationUsingGps(context);
        } else {
            updateLocationUsingNetwork(context);
        }
    }

    private void updateLocationUsingNetwork(final Context context) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        final LocationListener llNetwork;
        llNet = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.i(Util.TAG, "Received updated location via Network!");
                createDataTask(context);
                locationManager.removeUpdates(NetworkStateReceiver.this.llNet);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, llNet);


    }

    private void updateLocationUsingGps(final Context context) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        llGps = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.i(Util.TAG, "Received updated location via GPS!");
                createDataTask(context);
                locationManager.removeUpdates(NetworkStateReceiver.this.llNet);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, llGps);
    }

    private void createDataTask(Context context) {
        Log.i(Util.TAG, "Creating new DataTX AsyncTask");
        ReportingTask t = new ReportingTask(context);
        t.execute();
    }

}
