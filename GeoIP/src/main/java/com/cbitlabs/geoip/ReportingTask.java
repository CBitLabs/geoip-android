package com.cbitlabs.geoip;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by jblum on 2/17/14.
 */
public abstract class ReportingTask extends AsyncTask {
    protected Context c;

    public ReportingTask(Context c) {
        this.c = c;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        this.sendReport();
        return null;
    }

    public abstract void sendReport();
}
