package com.cbitlabs.geoip;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jblum on 1/10/14.
 * Try getting new scan and wifi reports every X minutes
 */
public class ReportIntentService extends IntentService {

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public ReportIntentService() {
        super("ReportIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        while (true) {
            Log.i(GenUtil.LOG_TAG, "Sending background task.");
            GenUtil.createWifiReportTask(getApplicationContext());
            GenUtil.createScanReportTask(getApplicationContext());
            try {
                Thread.sleep(GenUtil.FIVE_MINUTES);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
