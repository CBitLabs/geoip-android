package com.cbitlabs.geoip;

import android.content.Context;

/**
 * Created by jblum on 3/1/14.
 */
public class NotificationStorageManager extends StringSetPrefManager {
    private static final String PREF_KEY = "notification_networks";

    public NotificationStorageManager(Context c){
        super(c, PREF_KEY);
    }
}
