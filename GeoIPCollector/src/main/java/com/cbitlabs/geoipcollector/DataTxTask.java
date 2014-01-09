package com.cbitlabs.geoipcollector;

/**
 * Created by stuart on 11/25/13.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.xbill.DNS.Address;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

import java.net.UnknownHostException;

public class DataTxTask extends AsyncTask {

    private Context context;

    public DataTxTask(Context context) {
        Log.d(Util.TAG, "DataTxTask Created");

        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d(Util.TAG, "DataTxTask.doInBackground()");

        this.txDataViaDNSLookup();
        return null;
    }

    protected void executeLookupUsingResolver(String h, Resolver r) {

        Lookup.setDefaultResolver(r);
        executeLookup(h);
    }

    protected void executeLookup(String h) {
        Log.i(Util.TAG, "Looking up:" + h);

        try {
            Address.getByName(h);
        } catch (UnknownHostException e) {
            Log.e(Util.TAG, "DNS Lookup failed for host:" + h, e);
        }
    }

    public void txDataViaDNSLookup() {

        String info = Util.getReportInformation(context).toString();
        String host = info.concat(".").concat(Util.getDNSServerURL(this.context));
        Log.i(Util.TAG, "Created Hostname for Lookup:" + host);

        Resolver defaultResolver = Lookup.getDefaultResolver();
        String simpleResolverUrl = Util.getDNSResolverURL(this.context);
        executeLookupUsingResolver("d." + host, defaultResolver);

        try {
            Log.i(Util.TAG, "Setting DNS Resolver to use:" + simpleResolverUrl);
            Resolver simpleResolver = new SimpleResolver(simpleResolverUrl);
            executeLookupUsingResolver("s." + host, simpleResolver);
            Lookup.setDefaultResolver(defaultResolver);
        } catch (UnknownHostException e) {
            Log.e(Util.TAG, "A problem occurred while setting the custom resolver. UnknownHostException:" + simpleResolverUrl, e);
        }
    }
}

