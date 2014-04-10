package com.cbitlabs.geoip;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.UUID;

/**
 * Created by stuart on 11/25/13.
 * General utilies
 */
public class Util {

    public static final String LOG_TAG = "CBITLABS_GEOIP";
    public static final String PREF_KEY_DEVICE_ID = "device_id";

    private static final String DEVICE_ID_UNSET = "no_device_id";

    public static final long TEN_MINUTES = 1000 * 60 * 10l;
    public static final int FIVE_MINUTES = 1000 * 60 * 5;

    /**
     *
     * @param c
     * @return Unique per device
     */
    public static String getUUID(Context c) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String deviceId = prefs.getString(Util.PREF_KEY_DEVICE_ID, DEVICE_ID_UNSET);
        if (deviceId.equals(DEVICE_ID_UNSET))
            deviceId = generateDeviceID(c);
        return deviceId;
    }

    private static String generateDeviceID(Context c) {

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

    public static String cleanSSID(JsonElement ssid) {
        return cleanSSID(ssid.toString());
    }

    /**
     *
     * @param ssid
     * @return Deserialize ssid
     */
    public static String cleanSSID(String ssid) {
        return ssid.replace("_", " ").replace("\"", "");
    }

    /**
     *
     * @param ssid
     * @return Serialize ssid
     */
    public static String fmtSSID(String ssid) {
        ssid = ssid == null ? "" : ssid;
        ssid = ssid.replace("\"", "").replace(" ", "_");
        return ssid;
    }

    /**
     *
     * @param bssid
     * @return serialize bssid
     */
    public static String fmtBSSID(String bssid) {
        bssid = bssid.replace(":", "");
        return bssid;
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
