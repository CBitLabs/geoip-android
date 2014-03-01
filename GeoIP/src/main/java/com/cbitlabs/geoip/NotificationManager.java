package com.cbitlabs.geoip;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jblum on 3/1/14.
 */
public class NotificationManager extends PrefManager {
    private static final String NOTIFICATION_PREF = "notification_networks";

    public static void addNetworkNotification(Context c, String ssid) {
        Set<String> networks = getNetworks(c);
        networks.add(ssid);
        edit(c, networks);
        Log.i(Util.LOG_TAG, "adding: " + ssid + " networks: " + networks);
    }

    public static void rmNetworkNotification(Context c, String ssid) {
        Set<String> networks = getNetworks(c);
        networks.remove(ssid);
        edit(c, networks);
        Log.i(Util.LOG_TAG, "removing: " + ssid + " networks: " + networks);
    }

    public static boolean hasNotification(Context c, String ssid) {
        Set<String> networks = getNetworks(c);
        return networks.contains(ssid);
    }

    public static Set<String> getNetworks(Context c) {
        SharedPreferences prefs = getPreferences(c);
        return prefs.getStringSet(NOTIFICATION_PREF, new HashSet<String>());
    }

    private static void edit(Context c, Set<String> networks) {
        SharedPreferences.Editor editor = getEditor(c);
        editor.putStringSet(NOTIFICATION_PREF, networks);
        editor.commit();
    }
}
