package com.cbitlabs.geoipcollector;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by stuart on 11/25/13.
 */
public class Util {

    public static final String TAG = "CBITLABS_GEOIP";

    public static final String PREF_KEY_DEVICE_ID = "device_id";
    public static final String PREF_KEY_SUBMIT_UUID = "submit_device_id";
    public static final String PREF_KEY_SUBMIT_SSID = "submit_ssid";
    public static final String PREF_KEY_SUBMIT_BSSID = "submit_bssid";
    public static final String PREF_KEY_LOC_METHOD = "pref_location";

    private static final String DEVICE_ID_UNSET = "no_device_id";

    private static final long TEN_MINUTES = 1000 * 60 * 10l;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private static final String REPORT_SERVER_URL = "http://cbitslab-geoip.herokuapp.com/geoip";

    private static Map<String, String> lastReport = null;
    public static boolean isReportValid = true;

    public static boolean isValidReport(Context c, Map<String, String> report) {
        return isWiFiConnected(c) && !isDuplicateReport(c, report);
    }

    private static boolean isDuplicateReport(Context c, Map<String, String> report) {
        boolean isDuplicate = true;
        if (lastReport != null) {
            isDuplicate = report.equals(lastReport);
        }
        if (isWiFiConnected(c)) {
            lastReport = new HashMap<String, String>(report);
        }
        return isDuplicate;
    }

    public static String getReportServerUrl() {
        return REPORT_SERVER_URL;
    }

    public static Map<String, String> getReportInfo(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        boolean submitUUID = prefs.getBoolean(Util.PREF_KEY_SUBMIT_UUID, true);
        boolean submitSSID = prefs.getBoolean(Util.PREF_KEY_SUBMIT_SSID, true);
        boolean submitBSSID = prefs.getBoolean(Util.PREF_KEY_SUBMIT_BSSID, true);

        Map<String, String> reportMap = new HashMap<String, String>();
        GeoPoint loc = getLocation(c);
        reportMap.put("lat", loc.getLat());
        reportMap.put("lng", loc.getLng());
        reportMap.put("ssid", submitSSID ? getSSID(c) : null);
        reportMap.put("bssid", submitBSSID ? getBSSID(c) : null);
        reportMap.put("uuid", submitUUID ? getUUID(c) : null);

        Log.i(TAG, reportMap.toString());
        return reportMap;

    }

    public static String getUUID(Context c) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String deviceId = prefs.getString(Util.PREF_KEY_DEVICE_ID, DEVICE_ID_UNSET);
        if (deviceId.equals(DEVICE_ID_UNSET))
            deviceId = genDevID(c);

        Log.i(TAG, "Got Device ID:" + deviceId);
        return deviceId;
    }

    public static String genDevID(Context c) {

        Long id = UUID.randomUUID().getMostSignificantBits();
        String deviceId;
        deviceId = Long.toString(id, Character.MAX_RADIX);
        deviceId = deviceId.replace("-", "");

        Log.i(TAG, "Generated a new Device ID:".concat(deviceId));
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
        editor.putString(PREF_KEY_DEVICE_ID, deviceId);
        editor.commit();

        return deviceId;
    }

    public static String getSSID(Context c) {

        if (!Util.isWiFiConnected(c))
            return "";

        WifiManager wifiManager = (WifiManager) c.getSystemService(c.WIFI_SERVICE);
        String ssid = wifiManager.getConnectionInfo().getSSID();
        ssid = ssid.replace("\"", "").replace(" ", "_");

        Log.i(Util.TAG, "Got Network SSID:" + ssid);

        return ssid;
    }


    public static String getBSSID(Context c) {
        if (!Util.isWiFiConnected(c))
            return "";

        WifiManager wifiManager = (WifiManager) c.getSystemService(c.WIFI_SERVICE);
        String bssid = wifiManager.getConnectionInfo().getBSSID();
        bssid = bssid.replace(":", "");

        Log.i(Util.TAG, "Got Network BSSID:" + bssid);
        return bssid;
    }


    public static boolean isWiFiConnected(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        boolean state = wifi.isConnected();
        Log.i(TAG, String.format("WiFi State:%b", state));
        return state;
    }


    public static GeoPoint getLocation(final Context context) {

        Location netLoc = getNetworkLocation(context);
        Location gpsLoc = getGPSLocation(context);
        Location l;

        if (isBetterLocation(gpsLoc, netLoc))
            l = gpsLoc;
        else
            l = netLoc;

        GeoPoint p;
        if (l == null) {
            p = GeoPoint.getNullPoint();
            Log.d(Util.TAG, "Failed to get location information");
        } else {
            if (Util.isRecentLocation(l)) {
                p = GeoPoint.getNullPoint();
                Log.d(Util.TAG, "Failed to get recent location information!");
            } else
                p = new GeoPoint(l.getLatitude(), l.getLongitude());
        }
        return p;
    }

    protected static Location getNetworkLocation(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    protected static Location getGPSLocation(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    protected static boolean isRecentLocation(Location l) {
        long locAge = (new Date()).getTime() - l.getTime();
        return locAge > TEN_MINUTES;
    }


    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
        if (location == null) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    public static void createReportingTask(Context context) {
        Log.i(Util.TAG, "Creating new Reporting AsyncTask");
        ReportingTask t = new ReportingTask(context);
        t.execute();
    }

}
