package com.cbitlabs.geoip;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by stuart on 11/25/13.
 */
public class Util {

    public static final String LOG_TAG = "CBITLABS_GEOIP";
    public static final String DNS_SERVER = "geo.cbitlabs.com";
    public static final String DNS_RESOLVER = "cb101.public.cbitlabs.com";
    //    private static final String REPORT_SERVER_URL = "http://cb101.public.cbitlabs.com";
    private static final String REPORT_SERVER_URL = "http://18.189.12.102:8000";
    public static final String PREF_KEY_DEVICE_ID = "device_id";

    public static final String NO_IP = "0.0.0.0";
    private static final String DEVICE_ID_UNSET = "no_device_id";

    public static final long TEN_MINUTES = 1000 * 60 * 10l;
    public static final int FIVE_MINUTES = 1000 * 60 * 5;
    private static final String lastWifiReportPref = "lastWifiReport";
    private static final String lastScanReportPref = "lastScanReport";

    private static final String ENTERPRISE_CAPABILITY = "-EAP-";
    // Constants used for different security types
    private static final String PSK = "PSK";
    private static final String WEP = "WEP";
    private static final String EAP = "EAP";
    private static final String OPEN = "Open";

    public static JsonObject getWifiReport(Context c) {

        WifiInfo info = getWiFiInfo(c);
        ScanResult currentWifiResult = getCurrenWifiScanResult(c, info);
        if (currentWifiResult == null) {
            return new JsonObject();
        }
        String security = getScanResultSecurity(currentWifiResult);
        Boolean isEnterprise = scanResultIsEnterprise(currentWifiResult);

        return getReport(c, info.getSSID(),
                info.getBSSID(), getIPAddress(info),
                security, isEnterprise);

    }

    public static ArrayList<JsonObject> getScanReport(Context c) {
        List<ScanResult> results = getAvailableWifiScan(c);
        ArrayList<JsonObject> jsonResults = new ArrayList<JsonObject>();
        if (results != null) {
            for (ScanResult result : results) {
                if (!isDuplicateScanReport(c, result.BSSID)) {

                    JsonObject jsonReport = getReport(c, result.SSID,
                            result.BSSID, NO_IP,
                            getScanResultSecurity(result),
                            scanResultIsEnterprise(result));
                    jsonResults.add(jsonReport);
                }
            }
            Log.i(Util.LOG_TAG, "jsonResults " + jsonResults.toString());
        }
        return jsonResults;
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

    public static boolean isValidWifiReport(Context c, JsonObject report) {
        return report.entrySet().size() != 0
                && isWiFiConnected(c)
                && !isDuplicateWifiReport(c, report.get("bssid").getAsString());
    }

    public static boolean isValidScanReport(Context c, ArrayList<JsonObject> results) {
        return results.size() > 0;
    }

    private static boolean isDuplicateWifiReport(Context c, String bssid) {
        return isDuplicateReport(c, lastWifiReportPref, bssid, isWiFiConnected(c));
    }

    private static boolean isDuplicateScanReport(Context c, String bssid) {
        return isDuplicateReport(c, lastScanReportPref, bssid, true);
    }

    private static boolean isDuplicateReport(Context c, String prefKey,
                                             String bssid,
                                             boolean saveReport) {


        boolean isDuplicate = ReportCacheManager.inReportCache(c, prefKey, bssid);
        if (!isDuplicate && saveReport) {
            ReportCacheManager.putReportCache(c, prefKey, bssid);
        }

        return isDuplicate;
    }

    public static boolean isWiFiConnected(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        boolean state = wifi.isConnected();
        Log.i(LOG_TAG, String.format("WiFi State:%b", state));
        return state;
    }

    public static void enableWifi(Context c) {
        setWifiEnabled(c, true);
    }

    public static void disableWifi(Context c) {
        setWifiEnabled(c, false);
    }

    private static void setWifiEnabled(Context c, boolean status) {
        WifiManager wifiManager = getWifiManger(c);
        wifiManager.setWifiEnabled(status);
    }

    public static boolean isCurrentWifiConnection(Context c, ScanResult result) {
        WifiInfo info = getWiFiInfo(c);
        String eq = String.valueOf(result.SSID.equals(info.getSSID()));
        return quote(result.SSID).equals(info.getSSID());
    }

    public static String getWifiReportUrl() {
        return getUrl("wifi_report");
    }

    public static String getScanReportUrl() {
        return getUrl("scan_report");
    }

    public static String getScanRatingUrl(List<ScanResult> results) {
        String url = getUrl("ratings/scan_ratings?");
        for (ScanResult result : results) {
            url += String.format("bssid=%s&", Util.fmtBSSID(result.BSSID));
        }
        return url;
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

//        Log.i(LOG_TAG, "Got Device ID:" + deviceId);
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

//        Log.i(Util.LOG_TAG, "Got Network SSID:" + ssid);
        return ssid;
    }

    public static String cleanSSID(JsonElement ssid) {
        return ssid.toString().replace("_", " ").replace("\"", "");
    }

    public static String fmtBSSID(String bssid) {
        bssid = bssid.replace(":", "");

//        Log.i(Util.LOG_TAG, "Got Network BSSID:" + bssid);
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
        WifiManager wifiManager = getWifiManger(c);
        return wifiManager.getConnectionInfo();
    }

    public static List<ScanResult> getAvailableWifiScan(Context c) {
        WifiManager wifiManager = getWifiManger(c);
        return wifiManager.getScanResults();
    }

    public static List<ScanResult> getNewScanResults(Context c, ScanAdapter adapter) {
        List<ScanResult> results = getAvailableWifiScan(c);
        Set<String> currentBssids = adapter.getBssidSet();
        List<ScanResult> cleanedResults = new ArrayList<ScanResult>();
        for (ScanResult result : results) {
            if (!currentBssids.contains(result.BSSID)) {
                cleanedResults.add(result);
            }
        }
        return cleanedResults;
    }

    public static boolean connectToNetwork(Context c, String ssid) {
        WifiManager wifiManager = getWifiManger(c);
        List<WifiConfiguration> confs = getConfiguredNetworks(wifiManager);
        for (WifiConfiguration conf : confs) {
            if (conf.SSID.equals(quote(ssid))) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(conf.networkId, true);
                wifiManager.reconnect();
                return true;
            }
        }
        return false;
    }

    private static List<WifiConfiguration> getConfiguredNetworks(WifiManager wifiManager) {
        return wifiManager.getConfiguredNetworks();
    }

    private static WifiManager getWifiManger(Context c) {
        return (WifiManager) c.getSystemService(c.WIFI_SERVICE);
    }

    private static String quote(String string) {
        return String.format("\"%s\"", string);
    }

    public static int getWifiStrength(ScanResult result) {
        try {
            int level = WifiManager.calculateSignalLevel(result.level, 10);
            int percentage = (int) ((level / 10.0) * 100);
            return percentage;
        } catch (Exception e) {
            return 0;
        }
    }

    public static GeoPoint getLocation(final Context c) {

        Location netLoc = getNetworkLocation(c);
        Location gpsLoc = getGPSLocation(c);
        Location l;

        if (isBetterLocation(gpsLoc, netLoc))
            l = gpsLoc;
        else
            l = netLoc;

        GeoPoint p;
        if (l == null) {
            p = GeoPoint.getNullPoint();
//            Log.d(Util.LOG_TAG, "Failed to get location information");
        } else {
            if (Util.isRecentLocation(l)) {
                p = GeoPoint.getNullPoint();
//                Log.d(Util.LOG_TAG, "Failed to get recent location information!");
            } else
                p = new GeoPoint(l.getLatitude(), l.getLongitude());
        }
        return p;
    }

    protected static Location getNetworkLocation(final Context c) {
        LocationManager locationManager = (LocationManager) c.getSystemService(c.LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    protected static Location getGPSLocation(final Context c) {
        LocationManager locationManager = (LocationManager) c.getSystemService(c.LOCATION_SERVICE);
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
        boolean isSignificantlyNewer = timeDelta > FIVE_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -FIVE_MINUTES;
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


    public static void createWifiReportTask(Context c) {
        Log.i(Util.LOG_TAG, "Creating new Wifi AsyncTask");
        WifiReportTask t = new WifiReportTask(c);
        t.execute();
    }

    public static void createScanReportTask(Context c) {
        Log.i(Util.LOG_TAG, "Creating new ScanReport AsyncTask");
        ScanReportTask t = new ScanReportTask(c);
        t.execute();
    }

    public static void createDNSReportTask(Context c, JsonObject report) {
        Log.i(Util.LOG_TAG, "Creating new DNS AsyncTask");
        DNSReportTask t = new DNSReportTask(c, report);
        t.execute();
    }

}
