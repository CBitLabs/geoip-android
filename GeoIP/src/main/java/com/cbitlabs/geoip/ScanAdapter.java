package com.cbitlabs.geoip;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jblum on 2/20/14.
 */
public class ScanAdapter extends Adapter {

    public ScanAdapter(Context c, int resource) {
        super(c, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = getConvertView(convertView, R.layout.scan_item);
        ScanRating result = (ScanRating) getItem(position);

        boolean isCurrentWifiConnection = Util.isCurrentWifiConnection(getContext(), result.getScanResult());
        String scanConnected = isCurrentWifiConnection ? "Connected" : "";
        convertView = setAdaptorText(convertView, result.getScanResult().SSID, R.id.scan_ssid);
        convertView = setAdaptorText(convertView, scanConnected, R.id.scan_connected);
        convertView = setAdaptorText(convertView, fmtWifiStrength(result), R.id.scan_level);
        convertView = setAdaptorImage(convertView, result.getRating().getIcon(), R.id.rating_icon);

        return convertView;
    }

    private String fmtWifiStrength(ScanRating result) {
        return String.format("%d%%", Util.getWifiStrength(result.getScanResult()));
    }

    public void addAll(List<ScanRating> results) {
        List<ScanRating> cleanResults = cleanScanReport(results);
        Collections.sort(cleanResults, new Comparator<ScanRating>() {
            @Override
            public int compare(ScanRating lhs, ScanRating rhs) {
                return -Integer.compare(Util.getWifiStrength(lhs.getScanResult()),
                        Util.getWifiStrength(rhs.getScanResult()));
            }

        });

        cleanResults = moveCurrentWifi(cleanResults);
        super.addAll(cleanResults);
    }

    private List<ScanRating> cleanScanReport(List<ScanRating> results) {
        HashMap<String, ScanRating> cleanResults = new HashMap<String, ScanRating>();
        for (ScanRating result : results) {
            String ssid = result.getScanResult().SSID;
            if (ssid == "") {
                continue;
            }
            if (cleanResults.containsKey(ssid)) {
                ScanRating el = cleanResults.get(ssid);
                if (el.getScanResult().level > result.getScanResult().level) {
                    cleanResults.put(ssid, result);
                }

            } else {
                cleanResults.put(ssid, result);
            }
        }
        return new ArrayList<ScanRating>(cleanResults.values());
    }

    private List<ScanRating> moveCurrentWifi(List<ScanRating> results) {
        int index = getCurrentWifiIndex(results);
        return swapTop(results, index);
    }

    private int getCurrentWifiIndex(List<ScanRating> results) {
        int index = -1;
        for (int i = 0; i < results.size(); i++) {
            if (Util.isCurrentWifiConnection(getContext(), results.get(i).getScanResult())) {
                index = i;
                break;
            }
        }
        return index;
    }

    private List<ScanRating> swapTop(List<ScanRating> results, int index) {
        if (index != -1) {
            ScanRating item = results.remove(index);
            results.add(0, item);
        }
        return results;
    }

}
