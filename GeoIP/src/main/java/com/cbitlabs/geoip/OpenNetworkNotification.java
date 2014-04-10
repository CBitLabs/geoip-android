package com.cbitlabs.geoip;

import android.content.Context;

import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by jblum on 3/6/14.
 * Create notification for open networks.
 */
public class OpenNetworkNotification extends Notification {

    public static final String TAG = "open_network_notification";
    public static final long CACHE_LIFE = 1000 * 60 * 60;

    public OpenNetworkNotification(Context c, ArrayList<JsonObject> jsonObjects) {
        super(c, jsonObjects, TAG, CACHE_LIFE);
    }

    public void setNotification() {
        ArrayList<String> ssids = new ArrayList<String>();

        for (JsonObject jsonObject : jsonObjects) {
            ssids.add(getSsid(jsonObject));
        }

        OpenNetworkNotificationBuilder builder = new OpenNetworkNotificationBuilder(c, ssids);
        builder.build();
    }

    protected boolean needsNotification(JsonObject jsonObject) {
        String ssid = getSsid(jsonObject);
        return !WifiUtil.isWiFiConnected(c) && WifiUtil.isWifiEnabled(c) && isOpenNetwork(jsonObject) && !cacheManager.contains(ssid);
    }


    private boolean isOpenNetwork(JsonObject jsonObject) {
        return getAsString(jsonObject, "security") == ReportUtil.OPEN;
    }

    public static StringSetCacheManager getCacheManager(Context c) {
        return new StringSetCacheManager(c, TAG, CACHE_LIFE);
    }
}
