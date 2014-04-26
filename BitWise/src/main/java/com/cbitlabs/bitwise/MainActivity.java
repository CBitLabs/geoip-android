package com.cbitlabs.bitwise;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.cbitlabs.bitwise.fragments.HistoryFragment;
import com.cbitlabs.bitwise.fragments.ScanFragment;

public class MainActivity extends Activity implements ActionBar.TabListener {

	private static final String CURRENT_TAB = "com.cbitlabs.geoip.MainActivity.CURRENT_TAB";
	private Tab networks;
	private Tab history;
	private int currentTab = 0;
	private ScanFragment scanFragment;
	private HistoryFragment historyFragment;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(getApplicationContext(), ReportIntentService.class));
		setContentView(R.layout.activity_main);
		Fragment content = getFragmentManager().findFragmentById(R.id.content);
		if (content != null) {
			if (content instanceof ScanFragment) {
				scanFragment = (ScanFragment) content;
				historyFragment = new HistoryFragment();
			} else {
				scanFragment = new ScanFragment();
				historyFragment = (HistoryFragment) content;
			}
		} else {
			scanFragment = new ScanFragment();
			historyFragment = new HistoryFragment();
		}

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		networks = actionBar.newTab().setText(R.string.available_networks);
		history = actionBar.newTab().setText(R.string.history);
		networks.setTabListener(this);
		history.setTabListener(this);

		// workaround to hide icon and prevent tabs from appearing above the
		// actionbar (which setDisplayShowHomeEnabled(false) does in some
		// android versions);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setLogo(new ColorDrawable(Color.TRANSPARENT));

		int tab = currentTab; // we need to save this because the addTab()
								// method triggers the callback.
		actionBar.removeAllTabs();
		actionBar.addTab(networks);
		actionBar.addTab(history);
		currentTab = tab;
		// keep these 2 lines last
		currentTab = savedInstanceState == null ? currentTab : savedInstanceState.getInt(CURRENT_TAB, currentTab);

		actionBar.setSelectedNavigationItem(currentTab);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CURRENT_TAB, currentTab);
	}

	@Override
	public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
		if (tab == networks) {
			if (getFragmentManager().findFragmentById(R.id.content) != scanFragment) {
				ft.replace(R.id.content, scanFragment);
			}
		} else if (tab == history) {
			if (getFragmentManager().findFragmentById(R.id.content) != historyFragment) {
				ft.replace(R.id.content, historyFragment);
			}
		}
		currentTab = tab.getPosition();
		Log.d("MAINACT", "currentTab: " + currentTab);
	}

	@Override
	public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {
		// don't care
	}

	@Override
	public void onTabReselected(final Tab tab, final FragmentTransaction ft) {
		// don't care
	}

	public void onToggleClicked(View v) {
		try {
			Switch sw = (Switch) v;
			scanFragment.onToggleClicked(sw.isChecked());
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}
}
