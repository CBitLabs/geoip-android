package com.cbitlabs.bitwise;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.util.Log;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by jblum on 3/7/14.
 * Utility class for all reporting related tasks.
 * IMPORTANT:
 * Production app MUST use production url.
 */
public class ReportUtil {

    public static final String DNS_SERVER = "geo.cbitlabs.com";
    public static final String DNS_RESOLVER = "cb101.public.cbitlabs.com";
    private static final String REPORT_SERVER_URL = "http://54.235.252.38"; //dev url
//    private static final String REPORT_SERVER_URL = "http://cb101.public.cbitlabs.com"; //prod url

    public static final String NO_IP = "0.0.0.0";

    public static final String lastWifiReportPref = "lastWifiReport";
    public static final String lastScanReportPref = "lastScanReport";

    private static final String ENTERPRISE_CAPABILITY = "-EAP-";
    public static final String PSK = "PSK";
    public static final String WEP = "WEP";
    public static final String EAP = "EAP";
    public static final String OPEN = "Open";

    /**
     * WifiReport is a Json serialized current wifi result.
     *
     * @param c
     * @return JsonObject for a wifiReport
     */
    public static JsonObject getWifiReport(Context c) {

        WifiInfo info = WifiUtil.getWiFiInfo(c);
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

    /**
     *
     * @param c
     * @return ArrayList of report objects for results from the wifi scan.
     */
    public static ArrayList<JsonObject> getScanReport(Context c) {
        List<ScanResult> results = WifiUtil.getAvailableWifiScan(c);
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
            Log.i(GenUtil.LOG_TAG, "jsonResults " + jsonResults.toString());
        }
        return jsonResults;
    }

    /**
     *
     * @param c
     * @param ssid Preference ssid
     * @return JsonObject of saved preference
     */
    public static JsonObject getPrefReport(Context c, String ssid) {
        JsonObject report = new JsonObject();
        report.addProperty("ssid", ssid);
        report.addProperty("uuid", GenUtil.getUUID(c));
        return report;

    }

    /**
     *
     * @param c
     * @param ssid
     * @param bssid
     * @param ip
     * @param security
     * @param isEnterprise
     * @return JsonObject representing a serialized GeoIP object on the server.
     */
    private static JsonObject getReport(Context c, String ssid,
                                        String bssid, String ip,
                                        String security, Boolean isEnterprise) {
        GeoPoint loc = GeoUtil.getLocation(c);

        JsonObject reportMap = new JsonObject();
        reportMap.addProperty("lat", loc.getLat());
        reportMap.addProperty("lng", loc.getLng());
        reportMap.addProperty("ssid", GenUtil.fmtSSID(ssid));
        reportMap.addProperty("bssid", GenUtil.fmtBSSID(bssid));
        reportMap.addProperty("uuid", GenUtil.getUUID(c));
        reportMap.addProperty("ip", ip);
        reportMap.addProperty("security", security);
        reportMap.addProperty("isEnterprise", isEnterprise);
        return reportMap;

    }

    /**
     *
     * @param report
     * @return Stringified report for sending over DNS
     */
    public static String getReportAsString(JsonObject report) {
        String info = String.format("%s.%s.%s.%s.%s.%s",
                report.get("lat").getAsString(), report.get("lng").getAsString(),
                report.get("ssid").getAsString().replace(".", "-"), report.get("bssid").getAsString(),
                report.get("uuid").getAsString(), report.get("ip").getAsString());
        return info;
    }

    /**
     *
     * @param c
     * @param info
     * @return ScanResult of current wifi connection or null.
     */
    private static ScanResult getCurrenWifiScanResult(Context c, WifiInfo info) {
        List<ScanResult> results = WifiUtil.getAvailableWifiScan(c);
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

    /**
     *
     * @param scanResult
     * @return Security type
     */
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

    /**
     *
     * @param scanResult
     * @return Enterprise property
     */
    private static boolean scanResultIsEnterprise(ScanResult scanResult) {
        return scanResult.capabilities.contains(ENTERPRISE_CAPABILITY);
    }

    /**
     *
     * @param c
     * @param report
     * @return Validates wifiReport. Requires entries present, wifi on and not a duplicate
     */
    public static boolean isValidWifiReport(Context c, JsonObject report) {
        return report.entrySet().size() != 0
                && WifiUtil.isWiFiConnected(c)
                && !isDuplicateWifiReport(c, report.get("bssid").getAsString());
    }

    /**
     *
     * @param results
     * @return Validates scanReport, requires entries.
     */
    public static boolean isValidScanReport(ArrayList<JsonObject> results) {
        return results.size() > 0;
    }

    /**
     *
     * @param c
     * @param bssid
     * @return Checkes against cached version of last report.
     * New reports are saved if wifi is on.
     */
    private static boolean isDuplicateWifiReport(Context c, String bssid) {
        return isDuplicateReport(c, lastWifiReportPref, bssid, WifiUtil.isWiFiConnected(c));
    }

    /**
     *
     * @param c
     * @param bssid
     * @return Checks against cached version. New report is always saved.
     */
    private static boolean isDuplicateScanReport(Context c, String bssid) {
        return isDuplicateReport(c, lastScanReportPref, bssid, true);
    }

    /**
     *
     * @param c
     * @param prefKey
     * @param bssid
     * @param saveReport
     * @return checks cache for duplicate reports. Can update the cache with the saveReport flag.
     */
    private static boolean isDuplicateReport(Context c, String prefKey,
                                             String bssid,
                                             boolean saveReport) {

        ReportCacheManager cacheManager = new ReportCacheManager(c);
        boolean isDuplicate = cacheManager.inReportCache(prefKey, bssid);
        if (!isDuplicate && saveReport) {
            cacheManager.putReportCache(prefKey, bssid);
        }

        return isDuplicate;
    }

    /**
     *
     * @return Url to POST wifiReports
     */
    public static String getWifiReportUrl() {
        return getUrl("wifi_report");
    }

    /**
     *
     * @return Url to POST prefReports
     */
    public static String getPrefReportUrl() {
        return getUrl("pref_report");
    }

    /**
     *
     * @return Url to POST scanReport
     */
    public static String getScanReportUrl() {
        return getUrl("scan_report");
    }

    /**
     *
     * @param results
     * @return Url to GET ratings based on bssid
     */
    public static String getScanRatingUrl(List<ScanResult> results) {
        String[] bssids = new String[results.size()];
        for (int i = 0; i < results.size(); i++) {
            bssids[i] = results.get(i).BSSID;
        }
        return getScanRatingUrl(bssids);
    }

    /**
     *
     * @param bssids
     * @return Url to GET ratings based on bssid
     */
    public static String getScanRatingUrl(String[] bssids) {
        String url = getUrl("ratings/scan_ratings?");
        for (String bssid : bssids) {
            url += String.format("bssid=%s&", GenUtil.fmtBSSID(bssid));
        }
        return url;
    }

    /**
     *
     * @param bssids
     * @param ssids
     * @return Url to GET ratings based on bssid and ssid
     */
    public static String getScanRatingUrl(String[] bssids, String[] ssids) {
        String url = getScanRatingUrl(bssids);
        for (String ssid : ssids) {
            url += String.format("ssid=%s&", GenUtil.fmtSSID(ssid));
        }
        return url;
    }

    /**
     *
     * @param uuid
     * @param pageNum
     * @return Url to GET history results
     */
    public static String getHistoryUrl(String uuid, int pageNum) {
        return String.format("%s/%s?page=%d", getUrl("history"), uuid, pageNum);
    }

    /**
     *
     * @param baseUrl
     * @return Helper to format url with baseUrl
     */
    private static String getUrl(String baseUrl) {
        return String.format("%s/%s", REPORT_SERVER_URL, baseUrl);
    }

    /**
     *
     * @param info
     * @return Stringify an IPAddress
     */
    private static String getIPAddress(WifiInfo info) {
        int ip = info.getIpAddress();

        String ipString = String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));

        return ipString;
    }

    /**
     *
     * @param c
     * @param adapter
     * @return Updates ScanAdaptor with any new wifiresults
     */
    public static List<ScanResult> getNewScanResults(Context c, ScanAdapter adapter) {
        List<ScanResult> results = cleanScanReport(WifiUtil.getAvailableWifiScan(c));
        Set<String> currentBssids = adapter.getBssidSet();
        List<ScanResult> cleanedResults = new ArrayList<ScanResult>();
        for (ScanResult result : results) {
            if (!currentBssids.contains(result.BSSID)) {
                cleanedResults.add(result);
            }
        }
        return cleanedResults;
    }

    /**
     *
     * @param results
     * @return Cleans the result of scanning for Wifi by adding the result with
     * the best level and removing duplicates
     */
    private static List<ScanResult> cleanScanReport(List<ScanResult> results) {
        HashMap<String, ScanResult> cleanResults = new HashMap<String, ScanResult>();
        for (ScanResult result : results) {
            String ssid = result.SSID;
            if (ssid == "") {
                continue;
            }
            if (cleanResults.containsKey(ssid)) {
                ScanResult el = cleanResults.get(ssid);
                if (el.level > result.level) {
                    cleanResults.put(ssid, result);
                }

            } else {
                cleanResults.put(ssid, result);
            }
        }
        return new ArrayList<ScanResult>(cleanResults.values());
    }
}
