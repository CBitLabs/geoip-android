package com.cbitlabs.geoip;

import android.content.Context;

import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by jblum on 3/7/14.
 */
abstract class Notification {
    protected Context c;
    protected StringSetCacheManager cacheManager;
    protected NotificationStorageManager storageManager;
    protected ArrayList<JsonObject> jsonObjects;

    public Notification(Context c, ArrayList<JsonObject> jsonObjects, String prefKey, long expLife) {
        this.c = c;
        cacheManager = new StringSetCacheManager(c, prefKey, expLife);
        storageManager = new NotificationStorageManager(c);
        this.jsonObjects = filterForNotification(jsonObjects);
    }

    abstract void setNotification();

    abstract boolean needsNotification(JsonObject object);

    protected ArrayList<JsonObject> filterForNotification(ArrayList<JsonObject> jsonObjects) {
        ArrayList<JsonObject> cleanObjects = new ArrayList<JsonObject>();

        for (JsonObject jsonObject : jsonObjects) {
            if (needsNotification(jsonObject)) {
                cleanObjects.add(jsonObject);
            }
        }
        return cleanObjects;
    }

    protected String getSsid(JsonObject jsonObject) {
        return getAsString(jsonObject, "ssid");
    }

    protected String getBssid(JsonObject jsonObject) {
        return getAsString(jsonObject, "bssid");
    }

    protected String getAsString(JsonObject jsonObject, String key) {
        return jsonObject.get(key).getAsString();
    }
}
