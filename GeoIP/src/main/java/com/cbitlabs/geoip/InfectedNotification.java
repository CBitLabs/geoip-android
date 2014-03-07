package com.cbitlabs.geoip;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jblum on 3/6/14.
 */
public class InfectedNotification extends Notification {

    public static final String TAG = "infected_notification";
    public static final long CACHE_LIFE = PrefManager.ONE_DAY;

    public InfectedNotification(Context c, ArrayList<JsonObject> jsonObjects) {
        super(c, jsonObjects, TAG, PrefManager.ONE_DAY);
    }

    public void setNotification() {
        Ion.with(c, getUrl())
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject jsonRepsonse) {
                        if (e != null) {
                            Log.i(Util.LOG_TAG, e.toString());
                            return;
                        }
                        ArrayList<String> infectedIds = new ArrayList<String>();
                        Rating rating;
                        String bssid, ssid;
                        for (JsonObject jsonObject : jsonObjects) {
                            bssid = getBssid(jsonObject);
                            ssid = getSsid(jsonObject);
                            rating = new Rating(jsonRepsonse.get(bssid).getAsJsonObject(), ssid);
                            if (rating.isInfected()) {
                                infectedIds.add(ssid);
                            }
                        }
                        InfectedNotificationBuilder notificationBuilder = new InfectedNotificationBuilder(c, infectedIds);
                        notificationBuilder.build();
                    }
                }

                );

    }

    protected boolean needsNotification(JsonObject jsonObject) {
        String ssid = getSsid(jsonObject);
        return !cacheManager.contains(ssid) && storageManager.contains(ssid);
    }


    protected String getUrl() {
        Set<String> bssids = new HashSet<String>();
        String bssid;

        for (JsonObject jsonObject : jsonObjects) {
            bssid = getBssid(jsonObject);
            bssids.add(bssid);
        }

        return ReportUtil.getScanRatingUrl(bssids.toArray(new String[bssids.size()]));
    }

    public static StringSetCacheManager getCacheManager(Context c) {
        return new StringSetCacheManager(c, TAG, CACHE_LIFE);
    }
}
