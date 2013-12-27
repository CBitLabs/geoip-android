package com.cbitlabs.geoipcollector;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by stuart on 11/25/13.
 */
public class Util {

    public static final Map<String, String> dnsServerMap = new HashMap<String, String>();
    public static final Map<String, String> dnsResolverMap = new HashMap<String,String>();

    static{
        dnsServerMap.put("CBL", "geo.cbitlabs.com");
        dnsServerMap.put("GSF", "geo.spf.gladstonefamily.net");

        dnsResolverMap.put("CBL", "cb101.public.cbitlabs.com");
        dnsResolverMap.put("GSF", "charon.gladstonefamily.net");
    }

    public static final String TAG = "CBITLABS_GEOIP";

    public static final String PREF_KEY_DEVICE_ID   = "device_id";
    public static final String PREF_KEY_SUBMIT_UUID = "submit_device_id";
    public static final String PREF_KEY_SUBMIT_SSID = "submit_ssid";
    public static final String PREF_KEY_SUBMIT_BSSID = "submit_bssid";
    public static final String PREF_KEY_LOC_METHOD  = "pref_location";

    public static final String PREF_KEY_REPORT_SERVER  = "reporting_server";

    private static final String DEVICE_ID_UNSET = "no device id";

    private static final long TEN_MINUTES = 1000 * 60 * 10l;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public static String getDNSResolverURL(Context c){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String serverKey = prefs.getString(Util.PREF_KEY_REPORT_SERVER, "GSF");
        return dnsResolverMap.get(serverKey);
    }

    public static String getDNSServerURL(Context c){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String serverKey = prefs.getString(Util.PREF_KEY_REPORT_SERVER, "GSF");
        return dnsServerMap.get(serverKey);
    }

    public static String getReportInformation(Context c){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        boolean submitUUID = prefs.getBoolean(Util.PREF_KEY_SUBMIT_UUID, true);
        boolean submitSSID = prefs.getBoolean(Util.PREF_KEY_SUBMIT_SSID, true);
        boolean submitBSSID= prefs.getBoolean(Util.PREF_KEY_SUBMIT_BSSID, true);
        Log.i(Util.TAG, String.format("%b %b %b", submitUUID, submitSSID, submitBSSID));

        StringBuilder s = new StringBuilder();

        s.append(getLocation(c).toString());
        s.append(".");

        if (submitSSID)
            s.append(getSSID(c));
        else
            s.append("nossid");

        s.append(".");

        if (submitBSSID)
            s.append(getBSSID(c));
        else
            s.append("nomac");

        s.append(".");

        if (submitUUID)
            s.append(getDeviceID(c));
        else
            s.append("nodevid");

        Log.i(TAG, s.toString());
        return s.toString();

    }

    public static String getDeviceID(Context c){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String deviceId = prefs.getString(Util.PREF_KEY_DEVICE_ID, DEVICE_ID_UNSET);
        if (deviceId.equals(DEVICE_ID_UNSET))
            deviceId = generateNewDeviceID(c);

        Log.i(TAG, "Got Device ID:" + deviceId);
        return deviceId;
    }

    public static String generateNewDeviceID(Context c){

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

    public static String getSSID(Context c){

        if (!Util.getWifiConnectionState(c))
            return "";

        WifiManager wifiManager = (WifiManager) c.getSystemService(c.WIFI_SERVICE);
        String ssid = wifiManager.getConnectionInfo().getSSID();
        ssid = ssid.replace("\"","").replace(" ", "_");

        Log.i(Util.TAG, "Got Network SSID:" + ssid );

        return ssid;
    }


    public static String getBSSID(Context c){
        if (!Util.getWifiConnectionState(c))
            return "";

        WifiManager wifiManager = (WifiManager) c.getSystemService(c.WIFI_SERVICE);
        String bssid = wifiManager.getConnectionInfo().getBSSID();
        bssid = bssid.replace(":", "");

        Log.i(Util.TAG, "Got Network BSSID:" + bssid );
        return bssid;
    }


    public static boolean getWifiConnectionState(final Context context){
        ConnectivityManager cm = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        boolean state =  wifi.isConnected();
        Log.i(TAG, String.format("WiFi State:%b", state));
        return state;
    }


    public static GeoPoint getLocation(final Context context){

        Location netLoc = getNetworkLocation(context);
        Location gpsLoc = getGPSLocation(context);
        Location l;

        if (isBetterLocation(gpsLoc, netLoc))
            l = gpsLoc;
        else
            l = netLoc;

        GeoPoint p;
        if (l==null){
            p = GeoPoint.getNullPoint();
            String providers = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            Log.d(Util.TAG, "Failed to get location information");
        }
        else{
            long locAge = (new Date()).getTime() - l.getTime();
            if (locAge > TEN_MINUTES)
            {
                p = GeoPoint.getNullPoint();
                Log.d(Util.TAG, "Failed to get recent location information!");
            }
            else
                p =  new GeoPoint(l.getLatitude (), l.getLongitude ());
        }
        return p;
    }

    protected static Location getNetworkLocation(final Context context)
    {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    protected static Location getGPSLocation(final Context context)
    {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }


    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
        if (location == null){
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

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
