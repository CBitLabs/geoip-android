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
public class InfectedNotification {

    public static void buildNotification(final Context c, ArrayList<JsonObject> jsonObjects) {
        ArrayList<String[]> results = getBssids(c, jsonObjects);
        final String[] bssids = results.get(0);
        final String[] ssids = results.get(1);
        String url = Util.getScanRatingUrl(bssids);
        Ion.with(c, url)
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
                        for (int i = 0; i < bssids.length; i++) {
                            String bssid = bssids[i];
                            String ssid = ssids[i];
                            //TODO : REMOVE COMMENTS
                            rating = new Rating(jsonRepsonse.get(bssid).getAsJsonObject(), ssid);
//                            if (rating.isInfected()) {
                            infectedIds.add(ssid);
//                            }
                        }
                        InfectedNotificationBuilder notificationBuilder = new InfectedNotificationBuilder(c, infectedIds);
                        notificationBuilder.build();
                    }
                }

                );

    }

    private static ArrayList<String[]> getBssids(Context c, ArrayList<JsonObject> jsonResults) {
        InfectedNotificationCacheManager cacheManager = new InfectedNotificationCacheManager(c);
        NotificationStorageManager storageManager = new NotificationStorageManager(c);
        Set<String> bssids = new HashSet<String>();
        Set<String> ssids = new HashSet<String>();
        String bssid, ssid;

        for (JsonObject jsonObject : jsonResults) {
            ssid = jsonObject.get("ssid").getAsString();
            if (needsInfectedNotification(cacheManager, storageManager, ssid)) {
                bssid = jsonObject.get("bssid").getAsString();
                bssids.add(bssid);
                ssids.add(ssid);
            }
        }

        ArrayList<String[]> results = new ArrayList<String[]>();
        results.add(bssids.toArray(new String[bssids.size()]));
        results.add(ssids.toArray(new String[bssids.size()]));
        return results;
    }

    private static boolean needsInfectedNotification(InfectedNotificationCacheManager cacheManager,
                                                     NotificationStorageManager storageManager,
                                                     String ssid) {
        return !cacheManager.contains(ssid) && storageManager.contains(ssid);
    }

}