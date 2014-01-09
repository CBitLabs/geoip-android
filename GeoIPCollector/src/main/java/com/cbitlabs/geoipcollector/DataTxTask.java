package com.cbitlabs.geoipcollector;

/**
 * Created by stuart on 11/25/13.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class DataTxTask extends AsyncTask {

    private Context context;

    public DataTxTask(Context context) {
        Log.d(Util.TAG, "DataTxTask Created");

        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d(Util.TAG, "DataTxTask.doInBackground()");

        this.postReport();
        return null;
    }

    public void postReport() {
        JsonObject json = new JsonObject();
        json.addProperty("foo", "bar");

        Ion.with(context, Util.getReportServerUrl())
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.i(Util.TAG, ("Res: %s, err: %s").format(result.toString(), e.toString()));
                    }
                });
    }
}

