package com.cbitlabs.geoip;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by jblum on 2/17/14.
 */
public class ScanReportTask extends ReportingTask {

    public ScanReportTask(Context context) {
        super(context);
        Log.d(Util.LOG_TAG, "ScanReportTask Created");
    }

    public void sendReport() {
        final ArrayList<JsonObject> jsonResults = Util.getScanReport(this.context);

        if (!Util.isValidScanReport(this.context, jsonResults)) {
            Log.i(Util.LOG_TAG, "Duplicate report. Scan Report not sent.");
            return;
        }


        Log.i(Util.LOG_TAG, "Posting json " + jsonResults.toString());
        Ion.with(context, Util.getScanReportUrl())
                .setJsonObjectBody(jsonResults)
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
