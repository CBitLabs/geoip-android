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
        Log.d(Util.LOG_TAG, "--------------------------------");
        Log.d(Util.LOG_TAG, "NetworkStateReceiver.onReceive()");

        if (Util.isWiFiConnected(context)) {

            GeoPoint p = Util.getLocation(context);

            if (GeoPoint.isValidPoint(p)) {
                Util.createReportingTask(context);
                Intent report_intent = new Intent(context, ReportIntentService.class);
                context.startService(report_intent);
            } else {
                Log.i(Util.LOG_TAG, "Location isn't accurate enough, registering location listener");
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

        llNet = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.i(Util.LOG_TAG, "Received updated location via Network!");
                Util.createReportingTask(context);
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
                Log.i(Util.LOG_TAG, "Received updated location via GPS!");
                Util.createReportingTask(context);
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
}
