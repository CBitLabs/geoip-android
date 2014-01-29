package com.cbitlabs.geoip;

/**
 * Created by stuart on 11/25/13.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class ReportingTask extends AsyncTask {

    private Context context;

    public ReportingTask(Context context) {
        Log.d(Util.LOG_TAG, "ReportingTask Created");
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d(Util.LOG_TAG, "ReportingTask.doInBackground()");
        this.postReport();
        return null;
    }

    public void postReport() {
        final JsonObject geoReport = Util.getReport(this.context);

        if (!Util.isValidReport(this.context, geoReport)) {
            Log.i(Util.LOG_TAG, "Duplicate or noWIFI. Report not sent.");
            return;
        }


        Log.i(Util.LOG_TAG, "Posting json " + geoReport.toString());
        Ion.with(context, Util.getReportUrl())
                .setJsonObjectBody(geoReport)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject res) {
                        if (e != null) {
                            Log.i(Util.LOG_TAG, e.toString());
                            //try a DNS lookup instead
                            Util.createDNSTask(context, geoReport);
                        }
                        if (res != null) {
                            Log.i(Util.LOG_TAG, "Recieved " + res.toString());
                        }
                    }
                });
    }
}

