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
 * Created by jblum on 2/17/14.
 */
public class ScanReportTask extends ReportingTask {

    public ScanReportTask(Context c) {
        super(c);
        Log.d(Util.LOG_TAG, "ScanReportTask Created");
    }

    public void sendReport() {
        final ArrayList<JsonObject> jsonResults = Util.getScanReport(c);

        if (!Util.isValidScanReport(jsonResults)) {
            Log.i(Util.LOG_TAG, "Duplicate report. Scan Report not sent.");
            return;
        }

        postReport(jsonResults);
        InfectedNotification.buildNotification(c, jsonResults);
        OpenNetworkNotification.buildNotification(c, jsonResults);

    }

    private void postReport(ArrayList<JsonObject> jsonObjects) {
        Log.i(Util.LOG_TAG, "Posting json " + jsonObjects.toString());
        Ion.with(c, Util.getScanReportUrl())
                .setJsonObjectBody(jsonObjects)
                .asJsonObject()
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
    }
}
