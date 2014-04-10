package com.cbitlabs.geoip;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by jblum on 2/17/14.
 * Post scan report and issue notifications if necessary for scanResults
 */
public class ScanReportTask extends ReportingTask {

    public ScanReportTask(Context c) {
        super(c);
        Log.d(Util.LOG_TAG, "ScanReportTask Created");
    }

    public void sendReport() {
        final ArrayList<JsonObject> jsonObjects = ReportUtil.getScanReport(c);

        if (!ReportUtil.isValidScanReport(jsonObjects)) {
            Log.i(Util.LOG_TAG, "Duplicate report. Scan Report not sent.");
            return;
        }

        postReport(jsonObjects);
        setNotifications(jsonObjects);
    }

    private void postReport(ArrayList<JsonObject> jsonObjects) {
        Log.i(Util.LOG_TAG, "Posting json " + jsonObjects.toString());
        Ion.with(c, ReportUtil.getScanReportUrl())
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

    private void setNotifications(ArrayList<JsonObject> jsonObjects) {
        InfectedNotification infectedNotification = new InfectedNotification(c, jsonObjects);
        infectedNotification.setNotification();

        OpenNetworkNotification openNetworkNotification = new OpenNetworkNotification(c, jsonObjects);
        openNetworkNotification.setNotification();

    }
}
