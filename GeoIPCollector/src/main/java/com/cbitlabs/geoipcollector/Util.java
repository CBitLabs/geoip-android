package com.cbitlabs.geoipcollector;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;

/**
 * Created by stuart on 11/25/13.
 */
public class Util {

    public static final String LOG_TAG = "CBITLABS_GEOIP";
    //    private static final String REPORT_SERVER_URL = "http://cbitlabs-geoip.herokuapp.com";
    private static final String REPORT_SERVER_URL = "http://172.16.0.18:8000";
    public static final String PREF_KEY_DEVICE_ID = "device_id";
    public static final String PREF_KEY_SUBMIT_IP = "submit_device_ip";
    public static final String PREF_KEY_SUBMIT_UUID = "submit_device_id";
    public static final String PREF_KEY_SUBMIT_SSID = "submit_ssid";
    public static final String PREF_KEY_SUBMIT_BSSID = "submit_bssid";
    public static final String PREF_KEY_LOC_METHOD = "pref_location";

    private static final String DEVICE_ID_UNSET = "no_device_id";

    public static final long TEN_MINUTES = 1000 * 60 * 10l;
    public static final int TWO_MINUTES = 5000;//1000 * 60 * 2;

    private static JsonObject lastReport = null;

    public static boolean isValidReport(Context c, JsonObject report) {
        return isWiFiConnected(c) && !isDuplicateReport(c, report);
    }

    private static boolean isDuplicateReport(Context c, JsonObject report) {
        boolean isDuplicate = false;
        if (lastReport != null) {
            isDuplicate = report.toString().equals(lastReport.toString());
            Log.i(Util.LOG_TAG, "isDuplicate " + isDuplicate + " report: " + report.toString() + " last report:" + lastReport.toString());
        } else {
            Log.i(Util.LOG_TAG, "Null last report");
        }

        if (isWiFiConnected(c)) {

            lastReport = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : report.entrySet()) {
                lastReport.addProperty(entry.getKey(), entry.getValue().getAsString());
            }
            Log.i(Util.LOG_TAG, "Setting lastReport" + lastReport.toString());
        }
        return isDuplicate;
    }

    public static String getReportUrl() {
        return REPORT_SERVER_URL + "/add";
    }

    public static String getHistoryUrl(String uuid) {
        return REPORT_SERVER_URL + "/history/" + uuid;
    }

    public static JsonObject getReport(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        boolean submitIP = prefs.getBoolean(Util.PREF_KEY_SUBMIT_IP, true);
        boolean submitUUID = prefs.getBoolean(Util.PREF_KEY_SUBMIT_UUID, true);
        boolean submitSSID = prefs.getBoolean(Util.PREF_KEY_SUBMIT_SSID, true);
        boolean submitBSSID = prefs.getBoolean(Util.PREF_KEY_SUBMIT_BSSID, true);

        JsonObject reportMap = new JsonObject();
        GeoPoint loc = getLocation(c);
        reportMap.addProperty("lat", loc.getLat());
        reportMap.addProperty("lng", loc.getLng());
        reportMap.addProperty("ssid", submitSSID ? getSSID(c) : null);
        reportMap.addProperty("bssid", submitBSSID ? getBSSID(c) : null);
        reportMap.addProperty("uuid", submitUUID ? getUUID(c) : null);
        reportMap.addProperty("ip", submitIP ? getIPAddress(c) : null);

        Log.i(LOG_TAG, reportMap.toString());
        return reportMap;

    }

    public static String getUUID(Context c) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String deviceId = prefs.getString(Util.PREF_KEY_DEVICE_ID, DEVICE_ID_UNSET);
        if (deviceId.equals(DEVICE_ID_UNSET))
            deviceId = generateDeviceID(c);

        Log.i(LOG_TAG, "Got Device ID:" + deviceId);
        return deviceId;
    }

    public static String generateDeviceID(Context c) {

        Long id = UUID.randomUUID().getMostSignificantBits();
        String deviceId;
        deviceId = Long.toString(id, Character.MAX_RADIX);
        deviceId = deviceId.replace("-", "");

        Log.i(LOG_TAG, "Generated a new Device ID:".concat(deviceId));
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
        editor.putString(PREF_KEY_DEVICE_ID, deviceId);
        editor.commit();

        return deviceId;
    }

    public static boolean isWiFiConnected(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        boolean state = wifi.isConnected();
        Log.i(LOG_TAG, String.format("WiFi State:%b", state));
        return state;
    }

    public static String getSSID(Context c) {
        WifiInfo info = getWiFiInfo(c);
        if (info == null) {
            return "";
        }
        String ssid = info.getSSID();
        ssid = ssid.replace("\"", "").replace(" ", "_");

        Log.i(Util.LOG_TAG, "Got Network SSID:" + ssid);

        return ssid;
    }


    public static String getBSSID(Context c) {
        WifiInfo info = getWiFiInfo(c);
        if (info == null) {
            return "";
        }
        String bssid = info.getBSSID();
        bssid = bssid.replace(":", "");

        Log.i(Util.LOG_TAG, "Got Network BSSID:" + bssid);
        return bssid;
    }

    public static String getIPAddress(Context c) {
        WifiInfo info = getWiFiInfo(c);
        if (info == null) {
            return "";
        }
        int ip = info.getIpAddress();

        String ipString = String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));

        return ipString;
    }

    private static WifiInfo getWiFiInfo(Context c) {
        if (!Util.isWiFiConnected(c))
            return null;

        WifiManager wifiManager = (WifiManager) c.getSystemService(c.WIFI_SERVICE);
        return wifiManager.getConnectionInfo();
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
            Log.d(Util.LOG_TAG, "Failed to get location information");
        } else {
            if (Util.isRecentLocation(l)) {
                p = GeoPoint.getNullPoint();
                Log.d(Util.LOG_TAG, "Failed to get recent location information!");
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
        Log.i(Util.LOG_TAG, "Creating new Reporting AsyncTask");
        ReportingTask t = new ReportingTask(context);
        t.execute();
    }

}
