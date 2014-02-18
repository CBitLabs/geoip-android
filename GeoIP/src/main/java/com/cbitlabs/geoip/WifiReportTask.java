package com.cbitlabs.geoip;

/**
 * Created by stuart on 11/25/13.
 */

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class WifiReportTask extends ReportingTask {


    public WifiReportTask(Context context) {
        super(context);
        Log.d(Util.LOG_TAG, "WifiReportTask Created");
    }

    @Override
    public void postReport() {
        final JsonObject geoReport = Util.getWifiReport(this.context);

        if (!Util.isValidWifiReport(this.context, geoReport)) {
            Log.i(Util.LOG_TAG, "Duplicate or noWIFI. Wifi Report not sent.");
            return;
        }


        Log.i(Util.LOG_TAG, "Posting json " + geoReport.toString());
        Ion.with(context, Util.getWifiReportUrl())
                .setJsonObjectBody(geoReport)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject res) {
                        if (e != null) {
                            Log.i(Util.LOG_TAG, e.toString());
                            //try a DNS lookup instead
                            Util.createDNSReportTask(context, geoReport);
                        }
                        if (res != null) {
                            Log.i(Util.LOG_TAG, "Recieved " + res.toString());
                        }
                    }
                });
    }
}

