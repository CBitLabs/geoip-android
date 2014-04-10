package com.cbitlabs.geoip;

/**
 * Created by stuart on 11/25/13.
 * Post a wifiReport to the server, falls back to DNS if HTTP fails.
 */

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class WifiReportTask extends ReportingTask {


    public WifiReportTask(Context context) {
        super(context);
        Log.d(GenUtil.LOG_TAG, "WifiReportTask Created");
    }

    @Override
    public void sendReport() {
        final JsonObject wifiReport = ReportUtil.getWifiReport(this.c);

        if (!ReportUtil.isValidWifiReport(this.c, wifiReport)) {
            Log.i(GenUtil.LOG_TAG, "Duplicate or noWIFI. Wifi Report not sent.");
            return;
        }


        Log.i(GenUtil.LOG_TAG, "Posting json " + wifiReport.toString());
        Ion.with(c, ReportUtil.getWifiReportUrl())
                .setJsonObjectBody(wifiReport)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject res) {
                        if (e != null) {
                            Log.i(GenUtil.LOG_TAG, e.toString());
                            //try a DNS lookup instead
                            GenUtil.createDNSReportTask(c, wifiReport);
                        }
                        if (res != null) {
                            Log.i(GenUtil.LOG_TAG, "Recieved " + res.toString());
                        }
                    }
                });
    }
}

