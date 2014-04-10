package com.cbitlabs.geoip;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

/**
 * Created by jblum on 3/1/14.
 */
public class NotificationStorageManager extends StringSetPrefManager {
    private static final String PREF_KEY = "notification_networks";

    public NotificationStorageManager(Context c) {
        super(c, PREF_KEY);
    }

    @Override
    public void addString(String prefSsid) {
        JsonObject jsonObject = ReportUtil.getPrefReport(c, prefSsid);
        Log.i(Util.LOG_TAG, "Posting pref " + jsonObject
                + " prefUrl " + ReportUtil.getPrefReportUrl());
        Ion.with(c, ReportUtil.getPrefReportUrl())
                .setJsonObjectBody(jsonObject).asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject res) {
                        if (e != null) {
                            Log.i(Util.LOG_TAG, e.toString());
                        }
                        if (res != null) {
                            Log.i(Util.LOG_TAG, "Recieved " + res.toString());
                        }
                    }
                });
        super.addString(prefSsid);
    }

}
