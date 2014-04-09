package com.cbitlabs.geoip;

import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by jblum on 3/7/14.
 */
public class WifiUtil {

	public static boolean isWiFiConnected(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		boolean state = wifi.isConnected();
		// Log.i(LOG_TAG, String.format("WiFi State:%b", state));
		return state;
	}

	public static void enableWifi(Context c) {
		setWifiEnabled(c, true);
	}

	public static void disableWifi(Context c) {
		setWifiEnabled(c, false);
	}

	private static void setWifiEnabled(Context c, boolean status) {
		WifiManager wifiManager = getWifiManger(c);
		wifiManager.setWifiEnabled(status);
	}

	public static boolean isWifiEnabled(Context c) {
		WifiManager wifiManager = getWifiManger(c);
		return wifiManager.isWifiEnabled();
	}

	public static boolean isCurrentWifiConnection(Context c, ScanResult result) {
		// TODO uncomment
		// WifiInfo info = getWiFiInfo(c);
		// String eq = String.valueOf(result.SSID.equals(info.getSSID()));
		// return quote(result.SSID).equals(info.getSSID());
		return true;
	}

	public static WifiInfo getWiFiInfo(Context c) {
		WifiManager wifiManager = getWifiManger(c);
		return wifiManager.getConnectionInfo();
	}

	public static List<ScanResult> getAvailableWifiScan(Context c) {
		WifiManager wifiManager = getWifiManger(c);
		return wifiManager.getScanResults();
	}

	public static int getWifiStrength(ScanResult result) {
		try {
			int level = WifiManager.calculateSignalLevel(result.level, 10);
			int percentage = (int) ((level / 10.0) * 100);
			return percentage;
		} catch (Exception e) {
			return 0;
		}
	}

	public static boolean connectToNetwork(Context c, String ssid) {
		WifiManager wifiManager = getWifiManger(c);
		List<WifiConfiguration> confs = getConfiguredNetworks(wifiManager);
		for (WifiConfiguration conf : confs) {
			if (conf.SSID.equals(quote(ssid))) {
				wifiManager.disconnect();
				wifiManager.enableNetwork(conf.networkId, true);
				wifiManager.reconnect();
				return true;
			}
		}
		return false;
	}

	private static List<WifiConfiguration> getConfiguredNetworks(WifiManager wifiManager) {
		return wifiManager.getConfiguredNetworks();
	}

	private static WifiManager getWifiManger(Context c) {
		return (WifiManager) c.getSystemService(c.WIFI_SERVICE);
	}

	private static String quote(String string) {
		return String.format("\"%s\"", string);
	}
}
