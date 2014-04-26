package com.cbitlabs.bitwise;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jblum on 2/21/14.
 */
public class ReportCacheManager extends PrefManager {

    public ReportCacheManager(Context c) {
        super(c);
    }

    public void putReportCache(String baseKey, String bssid) {
        editor.putLong(serialize(baseKey, bssid), getToday().getTime());
        editor.commit();
    }

    public boolean inReportCache(String baseKey, String bssid) {
        SharedPreferences prefs = getPreferences();
        String prefKey = serialize(baseKey, bssid);
        if (!prefs.contains(prefKey)) {
            return false;
        }

        long cacheDate = deserialize(prefs, prefKey);
        if (isExpired(cacheDate, ONE_DAY)) {
            editor.remove(prefKey);
            editor.commit();
            return false;
        }
        return true;
    }

    private String serialize(String baseKey, String bssid) {
        return String.format("%s_%s", baseKey, GenUtil.fmtBSSID(bssid));
    }

    private long deserialize(SharedPreferences prefs, String prefKey) {
        return prefs.getLong(prefKey, 0);
    }

}
