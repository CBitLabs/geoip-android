package com.cbitlabs.geoip;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.cbitlabs.geoip.fragments.ScanFragment;

public class MainActivity extends Activity implements ActionBar.TabListener {

	private static final String CURRENT_TAB = "com.cbitlabs.geoip.MainActivity.CURRENT_TAB";
	private Tab networks;
	private Tab history;
	private int currentTab;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		networks = actionBar.newTab().setText(R.string.available_networks);
		history = actionBar.newTab().setText(R.string.history);
		history.setTabListener(this);
		networks.setTabListener(this);
		actionBar.addTab(networks);
		actionBar.addTab(history);
		currentTab = savedInstanceState == null ? 0 : savedInstanceState.getInt(CURRENT_TAB, 0);
		actionBar.setSelectedNavigationItem(currentTab);
		actionBar.setDisplayShowHomeEnabled(false);
		startService(new Intent(getApplicationContext(), ReportIntentService.class));
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CURRENT_TAB, currentTab);
	}

	@Override
	public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
		currentTab = tab.getPosition();
		if (tab == networks) {
			ft.replace(R.id.content, new ScanFragment());
		}
	}

	@Override
	public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {
		// don't care
	}

	@Override
	public void onTabReselected(final Tab tab, final FragmentTransaction ft) {
		// don't care
	}
}
