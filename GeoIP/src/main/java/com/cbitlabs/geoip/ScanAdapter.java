package com.cbitlabs.geoip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jblum on 2/20/14.
 */
public class ScanAdapter extends Adapter {

	private List<ScanRating> allResults;

	public ScanAdapter(Context c, int resource) {
		super(c, resource);
		allResults = new ArrayList<ScanRating>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = getConvertView(convertView, R.layout.scan_item);
		ScanRating result = (ScanRating) getItem(position);
		Context c = getContext();
		boolean isCurrentWifiConnection = WifiUtil.isCurrentWifiConnection(c, result.getScanResult());
		String scanConnected = isCurrentWifiConnection ? "Connected" : "";
		convertView = setAdaptorText(convertView, result.getScanResult().SSID, R.id.scan_ssid);
		convertView = setAdaptorText(convertView, scanConnected, R.id.scan_connected);
		convertView = setAdaptorText(convertView, fmtWifiStrength(result), R.id.scan_level);
		convertView = setAdaptorImage(convertView, result.getRating().getIcon(), R.id.rating_icon);
		convertView = setAdaptorImage(convertView, result.getRating().notificationIcon(c), R.id.hasNotification);

		return convertView;
	}

	private String fmtWifiStrength(ScanRating result) {
		return String.format("%d%%", WifiUtil.getWifiStrength(result.getScanResult()));
	}

	public void addAll(List<ScanRating> results) {
		allResults.addAll(results);
		allResults = cleanScanReport(allResults);
		Collections.sort(allResults, new Comparator<ScanRating>() {
			@Override
			public int compare(ScanRating lhs, ScanRating rhs) {
				return -Integer.compare(WifiUtil.getWifiStrength(lhs.getScanResult()),
						WifiUtil.getWifiStrength(rhs.getScanResult()));
			}

		});

		allResults = moveCurrentWifi(allResults);
		super.addAll(allResults);
	}

	private static List<ScanRating> cleanScanReport(List<ScanRating> results) {
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
			if (WifiUtil.isCurrentWifiConnection(getContext(), results.get(i).getScanResult())) {
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

	public Set<String> getBssidSet() {
		Set<String> bssids = new HashSet<String>();
		for (Object item : allResults) {
			bssids.add(((ScanRating) item).getScanResult().BSSID);
		}
		return bssids;
	}

}
