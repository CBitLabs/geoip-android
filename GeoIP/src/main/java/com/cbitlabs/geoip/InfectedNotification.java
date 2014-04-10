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
 * Nofication to alert when a network is infected
 */
public class InfectedNotification extends Notification {

    public static final String TAG = "infected_notification";
    public static final long CACHE_LIFE = PrefManager.ONE_DAY;

    public InfectedNotification(Context c, ArrayList<JsonObject> jsonObjects) {
        super(c, jsonObjects, TAG, PrefManager.ONE_DAY);
        addWatchedNetworks();
    }

    /**
     * Update the objects with the user's watched newtorks
     */
    private void addWatchedNetworks() {
        NotificationStorageManager manager = new NotificationStorageManager(c);
        Set<String> watchedSsids = manager.getSet();
        for (String ssid : watchedSsids) {
            JsonObject watchedNetwork = new JsonObject();
            watchedNetwork.addProperty("ssid", ssid);
            watchedNetwork.addProperty("bssid", "");
            if (needsNotification(watchedNetwork)) {
                jsonObjects.add(watchedNetwork);
            }
        }
    }


    /*
        Get ratings for the given bssid/ssid ids.
        Build a notifcation of infected ssids.
     */
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
                                     Set<String> infectedIds = new HashSet<String>();
                                     Rating rating;
                                     String bssid, ssid;
                                     for (JsonObject jsonObject : jsonObjects) {
                                         bssid = getBssid(jsonObject);
                                         ssid = getSsid(jsonObject);
                                         if (jsonRepsonse.has(bssid)) {
                                             rating = new Rating(jsonRepsonse.get(bssid).getAsJsonObject(), ssid);
                                         } else {
                                             rating = new Rating(jsonRepsonse.get(ssid).getAsJsonObject(), ssid);
                                         }
                                         if (rating.isInfected()) {
                                             infectedIds.add(ssid);
                                         }
                                     }
                                     InfectedNotificationBuilder notificationBuilder = new InfectedNotificationBuilder(c, new ArrayList<String>(infectedIds));
                                     notificationBuilder.build();
                                 }
                             }
                );

    }

    /**
     *
     * @param jsonObject
     * @return Prevent duplicate notifications!
     */
    protected boolean needsNotification(JsonObject jsonObject) {
        String ssid = getSsid(jsonObject);
        return !cacheManager.contains(ssid) && storageManager.contains(ssid);
    }


    /**
     *
     * @return Request ratings by bssid and ssid
     */
    protected String getUrl() {
        Set<String> bssids = new HashSet<String>();
        Set<String> ssids = new HashSet<String>();
        String bssid, ssid;

        for (JsonObject jsonObject : jsonObjects) {
            bssid = getBssid(jsonObject);
            ssid = getSsid(jsonObject);
            if (bssid.equals("")) {
                ssids.add(ssid);
            } else {
                bssids.add(bssid);
            }
        }

        return ReportUtil.getScanRatingUrl(bssids.toArray(new String[bssids.size()]), ssids.toArray(new String[ssids.size()]));
    }

    public static StringSetCacheManager getCacheManager(Context c) {
        return new StringSetCacheManager(c, TAG, CACHE_LIFE);
    }
}
