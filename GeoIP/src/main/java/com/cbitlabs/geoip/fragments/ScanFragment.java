package com.cbitlabs.geoip.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.cbitlabs.geoip.R;
import com.cbitlabs.geoip.ReportUtil;
import com.cbitlabs.geoip.ScanAdapter;
import com.cbitlabs.geoip.ScanDetailActivity;
import com.cbitlabs.geoip.ScanRating;
import com.cbitlabs.geoip.SettingsActivity;
import com.cbitlabs.geoip.Util;
import com.cbitlabs.geoip.WifiUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;

public class ScanFragment extends Fragment {

	private ScanAdapter scanAdaptor = null;
	private Timer autoUpdate;
	private final int TWENTY_SECONDS = 20 * 1000;
	private Menu menu;

	// This "Future" tracks loading operations.
	// A Future is an object that manages the state of an operation
	// in progress that will have a "Future" result.
	// You can attach callbacks (setCallback) for when the result is ready,
	// or cancel() it if you no longer need the result.
	Future<JsonObject> loading;
	private TextView empty;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_scan, null);
		scanAdaptor = new ScanAdapter(getActivity(), 0);
		empty = (TextView) view.findViewById(R.id.empty_text);

		setListView(view);
		loadNetworks();
		return view;
	}

	private void setListView(final View view) {
		// basic setup of the ListView and adapter
		final ListView lv = (ListView) view.findViewById(R.id.list);
		lv.setAdapter(scanAdaptor);
		lv.setEmptyView(view.findViewById(R.id.empty_element));
		lv.setClickable(true);
		lv.setLongClickable(true);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {

				TextView textView = (TextView) view.findViewById(R.id.scan_connected);

				if (textView.getText().equals("Connected")) {
					return;
				}

				final ScanRating result = (ScanRating) parent.getItemAtPosition(position);

				if (!WifiUtil.connectToNetwork(getActivity().getApplicationContext(), result.getScanResult().SSID)) {
					startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
				}
				for (int i = 0; i < lv.getCount(); i++) {
					View child = lv.getChildAt(i);
					if (child != null) {
						TextView clearText = (TextView) view.findViewById(R.id.scan_connected);
						clearText.setText("");
					}
				}

				textView.setText("Connecting...");
			}

		});

		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position,
					final long id) {
				ScanRating scanRating = (ScanRating) parent.getItemAtPosition(position);
				Intent intent = new Intent(view.getContext(), ScanDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable(scanRating.getRating().SER_KEY, scanRating.getRating());
				intent.putExtras(bundle);
				startActivity(intent);

				return true;
			}
		});
	}

	public void onToggleClicked(boolean enable) {
		if (enable) {
			WifiUtil.enableWifi(getActivity());
			loadNetworks();
		} else {
			setnoWifiText();
			scanAdaptor.clear();
			WifiUtil.disableWifi(getActivity());
		}
	}

	private void loadNetworks() {

		if (WifiUtil.isWifiEnabled(getActivity())) {
		} else {
			setnoWifiText();
			scanAdaptor.clear();
			return;
		}

		final List<ScanResult> results = ReportUtil.getNewScanResults(getActivity(), scanAdaptor);
		if (results.size() == 0) {
			setEmptyText();
			return;
		}

		String url = ReportUtil.getScanRatingUrl(results);

		Builders.Any.B ion = Ion.with(getActivity(), url);
		ion.setTimeout(2000);
		loading = ion.asJsonObject().setCallback(new FutureCallback<JsonObject>() {
			@Override
			public void onCompleted(final Exception e, final JsonObject jsonRepsonse) {
				List<ScanRating> ratings = new ArrayList<ScanRating>();
				if (e != null) {
					Log.i(Util.LOG_TAG, e.toString());

					for (ScanResult result : results) {
						ratings.add(new ScanRating(result, result.SSID));
					}
				} else {
					Log.i(Util.LOG_TAG, "Found ratings: " + jsonRepsonse.toString());

					for (ScanResult result : results) {
						JsonElement rating = jsonRepsonse.get(Util.fmtBSSID(result.BSSID));
						if (rating != null) {
							ratings.add(new ScanRating(result, rating.getAsJsonObject()));
						}
					}
				}
				scanAdaptor.clear();
				scanAdaptor.addAll(ratings);
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main, menu);
		this.menu = menu;

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		Intent i;
		switch (item.getItemId()) {
		case R.id.refreshScan:
			loadNetworks();
			return true;
		case R.id.action_settings:
			i = new Intent(getActivity(), SettingsActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setnoWifiText() {
		setText(R.string.no_wifi);
	}

	private void setEmptyText() {
		setText(R.string.no_networks);
	}

	private void setText(final int resource) {
		if (empty != null) {
			empty.setText(resource);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Switch sw = (Switch) getView().findViewById(R.id.wifi_toggle);
		sw.setChecked(WifiUtil.isWifiEnabled(getActivity()));
		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							loadNetworks();
						}
					});
				}
			}
		}, 0, TWENTY_SECONDS);
	}

	@Override
	public void onPause() {
		if (autoUpdate != null) {
			autoUpdate.cancel();
		}
		super.onPause();
	}
}
