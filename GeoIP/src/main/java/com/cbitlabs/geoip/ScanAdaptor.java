package com.cbitlabs.geoip;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jblum on 2/20/14.
 */
public class ScanAdaptor extends ArrayAdapter {

    private ArrayList<ScanResult> objects;

    public ScanAdaptor(Context c, int resource, ArrayList<ScanResult> objects) {
        super(c, resource, objects);
        this.objects = objects;
        ;
    }

    public ScanAdaptor(Context c, int resource) {
        super(c, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {


            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.scan_item, null);
        }
        ScanResult result = (ScanResult) getItem(position);

        boolean isCurrentWifiConnection = Util.isCurrentWifiConnection(getContext(), result);
        String scanConnected = isCurrentWifiConnection ? "Connected" : "";
        convertView = setAdaptorText(convertView, result.SSID, R.id.scan_ssid);
        convertView = setAdaptorText(convertView, scanConnected, R.id.scan_connected);
        convertView = setAdaptorText(convertView, fmtWifiStrength(result), R.id.scan_level);

        return convertView;
    }

    private View setAdaptorText(View convertView, String text, int id) {
        TextView textView = (TextView) convertView.findViewById(id);
        textView.setText(text);
        return convertView;
    }

    private String fmtWifiStrength(ScanResult result) {
        return String.format("%d%%", Util.getWifiStrength(result));
    }

    public void addAll(List<ScanResult> results) {
        List<ScanResult> cleanResults = cleanScanReport(results);
        Collections.sort(cleanResults, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return -Integer.compare(Util.getWifiStrength(lhs), Util.getWifiStrength(rhs));
            }

        });

        cleanResults = moveCurrentWifi(cleanResults);
        super.addAll(cleanResults);
    }

    private List<ScanResult> cleanScanReport(List<ScanResult> results) {
        HashMap<String, ScanResult> cleanResults = new HashMap<String, ScanResult>();
        for (ScanResult result : results) {
            String ssid = result.SSID;
            if (ssid == "") {
                continue;
            }
            if (cleanResults.containsKey(ssid)) {
                ScanResult el = cleanResults.get(ssid);
                if (el.level > result.level) {
                    cleanResults.put(ssid, result);
                }

            } else {
                cleanResults.put(ssid, result);
            }
        }
        return new ArrayList<ScanResult>(cleanResults.values());
    }

    private List<ScanResult> moveCurrentWifi(List<ScanResult> results) {
        int index = getCurrentWifiIndex(results);
        return swapTop(results, index);
    }

    private int getCurrentWifiIndex(List<ScanResult> results) {
        int index = -1;
        for (int i = 0; i < results.size(); i++) {
            if (Util.isCurrentWifiConnection(getContext(), results.get(i))) {
                index = i;
                break;
            }
        }
        return index;
    }

    private List<ScanResult> swapTop(List<ScanResult> results, int index) {
        if (index != -1) {
            ScanResult item = results.remove(index);
            results.add(0, item);
        }
        return results;
    }

}
