package com.cbitlabs.geoip;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import java.util.List;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private CheckBoxPreference submit_id;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.pref_general);

        submit_id = (CheckBoxPreference) this.findPreference("submit_device_id");
        submit_id.setOnPreferenceChangeListener(this);
        this.updateDeviceIDHelpText();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.submit_id) {
            boolean doSubmit = (Boolean) newValue;

            if (doSubmit) {
                Util.generateDeviceID(this);
            }
            this.updateDeviceIDHelpText(doSubmit);
        }
        return true;
    }


    private void updateDeviceIDHelpText() {
        this.updateDeviceIDHelpText(this.submit_id.isChecked());
    }

    private void updateDeviceIDHelpText(boolean doSubmit) {

        String description;
        if (doSubmit) {
            String deviceId = Util.getUUID(this);
            description = "Device ID: ".concat(deviceId);
        } else {
            description = "Device ID will not be submitted.";
        }
        this.submit_id.setSummary(description);
    }

    @Override
    public boolean onIsMultiPane() {
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        return;
    }

}
