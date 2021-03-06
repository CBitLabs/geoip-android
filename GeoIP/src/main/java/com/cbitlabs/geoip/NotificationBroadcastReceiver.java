package com.cbitlabs.geoip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by jblum on 3/4/14.
 * Adds ssids to the cache when a notification is cancelled.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context c, final Intent intent) {
        String action = intent.getAction();
        String extrasKey = "";
        StringSetCacheManager cacheManager;
        if (action.equals(InfectedNotificationBuilder.DELETE_ACTION)) {
            extrasKey = InfectedNotificationBuilder.EXTRAS_KEY;
            cacheManager = InfectedNotification.getCacheManager(c);


        } else if (action.equals(OpenNetworkNotificationBuilder.DELETE_ACTION)) {
            extrasKey = OpenNetworkNotificationBuilder.EXTRAS_KEY;
            cacheManager = OpenNetworkNotification.getCacheManager(c);
        } else {
            return;
        }

        ArrayList<String> ssids = intent.getStringArrayListExtra(extrasKey);
        Log.i(GenUtil.LOG_TAG, "DeleteIntent. Action: " + action
                + " extrasKey: " + extrasKey + " extras: " + ssids);
        if (ssids != null) {
            for (String ssid : ssids) {
                cacheManager.addString(ssid);
            }
        }

    }
}
