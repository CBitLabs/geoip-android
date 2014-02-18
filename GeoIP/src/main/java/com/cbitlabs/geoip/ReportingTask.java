package com.cbitlabs.geoip;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by jblum on 2/17/14.
 */
public abstract class ReportingTask extends AsyncTask {
    public Context context;

    public ReportingTask(Context context) {
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        this.postReport();
        return null;
    }

    public abstract void postReport();
}
