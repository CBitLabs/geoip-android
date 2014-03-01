package com.cbitlabs.geoip;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jblum on 2/21/14.
 */
public class ReportCacheManager extends PrefManager{
    private static final int ONE_DAY = 24 * 60 * 60 * 1000;

    public static void putReportCache(Context c, String baseKey, String bssid) {
        SharedPreferences.Editor editor = getEditor(c);
        editor.putLong(serialize(baseKey, bssid), getToday().getTime());
        editor.commit();
    }

    public static boolean inReportCache(Context c, String baseKey, String bssid) {
        SharedPreferences prefs = getPreferences(c);
        String prefKey = serialize(baseKey, bssid);
        if (!prefs.contains(prefKey)) {
            return false;
        }

        long cacheDate = deserialize(prefs, prefKey);
        if (getToday().getTime() - cacheDate > ONE_DAY) {
            SharedPreferences.Editor editor = getPreferences(c).edit();
            editor.remove(prefKey);
            editor.commit();
            return false;
        }
        return true;
    }

    private static Date getToday() {
        return Calendar.getInstance().getTime();
    }

    private static String serialize(String baseKey, String bssid) {
        return String.format("%s_%s", baseKey, Util.fmtBSSID(bssid));
    }

    private static long deserialize(SharedPreferences prefs, String prefKey) {
        return prefs.getLong(prefKey, 0);
    }

}
