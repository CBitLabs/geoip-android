package com.cbitlabs.geoip;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonObject;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by stuart on 11/25/13.
 */
public class Util {

    public static final String LOG_TAG = "CBITLABS_GEOIP";
    public static final String DNS_SERVER = "geo.cbitlabs.com";
    public static final String DNS_RESOLVER = "cb101.public.cbitlabs.com";
    //    private static final String REPORT_SERVER_URL = "http://cb101.public.cbitlabs.com";
    private static final String REPORT_SERVER_URL = "http://18.189.61.100:8000";
    public static final String PREF_KEY_DEVICE_ID = "device_id";

    public static final String NO_IP = "0.0.0.0";
    private static final String DEVICE_ID_UNSET = "no_device_id";

    public static final long TEN_MINUTES = 1000 * 60 * 10l;
    public static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final String lastReportPref = "lastReport";

    private static final String ENTERPRISE_CAPABILITY = "-EAP-";
    // Constants used for different security types
    public static final String PSK = "PSK";
    public static final String WEP = "WEP";
    public static final String EAP = "EAP";
    public static final String OPEN = "Open";

    public static JsonObject getCurrentReport(Context c) {

        WifiInfo info = getWiFiInfo(c);
        ScanResult currentWifiResult = getCurrenWifiScanResult(c, info);
        String security = getScanResultSecurity(currentWifiResult);
        Boolean isEnterprise = scanResultIsEnterprise(currentWifiResult);
        return getReport(c, info.getSSID(),
                info.getBSSID(), getIPAddress(info),
                security, isEnterprise);

    }

    public static void getScanReport(Context c) {
        List<ScanResult> results = getAvailableWifiScan(c);
        if (results != null) {
            for (ScanResult result : results) {
                Log.i(Util.LOG_TAG, String.format("Found result " +
                        "ssid: %s, bssid: %s, capabilities: %s",
                        result.SSID, result.BSSID, result.capabilities));
            }

        }
    }

    private static ScanResult getCurrenWifiScanResult(Context c, WifiInfo info) {
        List<ScanResult> results = getAvailableWifiScan(c);
        String bssid = info.getBSSID();
        if (results != null) {
            for (ScanResult result : results) {
                if (result.BSSID.equals(bssid)) {
                    return result;
                }
            }
        }
        return null;
    }

    private static String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = {WEP, PSK, EAP};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return OPEN;
    }

    private static boolean scanResultIsEnterprise(ScanResult scanResult) {
        return scanResult.capabilities.contains(ENTERPRISE_CAPABILITY);
    }

    private static JsonObject getReport(Context c, String ssid,
                                        String bssid, String ip,
                                        String security, Boolean isEnterprise) {
        GeoPoint loc = getLocation(c);

        JsonObject reportMap = new JsonObject();
        reportMap.addProperty("lat", loc.getLat());
        reportMap.addProperty("lng", loc.getLng());
        reportMap.addProperty("ssid", fmtSSID(ssid));
        reportMap.addProperty("bssid", fmtBSSID(bssid));
        reportMap.addProperty("uuid", getUUID(c));
        reportMap.addProperty("ip", ip);
        reportMap.addProperty("security", security);
        reportMap.addProperty("isEnterprise", isEnterprise);
        return reportMap;

    }

    public static String getReportAsString(JsonObject report) {
        String info = String.format("%s.%s.%s.%s.%s.%s",
                report.get("lat").getAsString(), report.get("lng").getAsString(),
                report.get("ssid").getAsString().replace(".", "-"), report.get("bssid").getAsString(),
                report.get("uuid").getAsString(), report.get("ip").getAsString());
        return info;
    }

    public static boolean isValidReport(Context c, JsonObject report) {
        return isWiFiConnected(c) && !isDuplicateReport(c, report);
    }

    private static boolean isDuplicateReport(Context c, JsonObject report) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String lastReport = prefs.getString(lastReportPref, "");
        boolean isDuplicate = report.toString().equals(lastReport);

        if (isWiFiConnected(c)) {
            lastReport = report.toString();

            Log.i(Util.LOG_TAG, "Setting lastReport" + lastReport);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(lastReportPref, lastReport);
            editor.commit();

        }

        return isDuplicate;
    }

    public static boolean isWiFiConnected(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        boolean state = wifi.isConnected();
        Log.i(LOG_TAG, String.format("WiFi State:%b", state));
        return state;
    }

    public static String getReportUrl() {
        return getUrl("add");
    }

    public static String getHistoryUrl(String uuid, int pageNum) {
        return String.format("%s/%s?page=%d", getUrl("history"), uuid, pageNum);
    }

    private static String getUrl(String baseUrl) {
        return String.format("%s/%s", REPORT_SERVER_URL, baseUrl);
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

    public static String fmtSSID(String ssid) {
        ssid = ssid.replace("\"", "").replace(" ", "_");

        Log.i(Util.LOG_TAG, "Got Network SSID:" + ssid);
        return ssid;
    }


    private static String fmtBSSID(String bssid) {
        bssid = bssid.replace(":", "");

        Log.i(Util.LOG_TAG, "Got Network BSSID:" + bssid);
        return bssid;
    }

    public static String getIPAddress(WifiInfo info) {
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
        WifiManager wifiManager = (WifiManager) c.getSystemService(c.WIFI_SERVICE);
        return wifiManager.getConnectionInfo();
    }

    public static List<ScanResult> getAvailableWifiScan(Context c) {
        WifiManager wifiManager = (WifiManager) c.getSystemService(c.WIFI_SERVICE);
        return wifiManager.getScanResults();
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

    public static void createDNSTask(Context context, JsonObject report) {
        Log.i(Util.LOG_TAG, "Creating new DNS AsyncTask");
        DNSTask t = new DNSTask(context, report);
        t.execute();
    }

}
