package com.cbitlabs.geoipcollector;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.List;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private static final boolean ALWAYS_SIMPLE_PREFS = true;

    private CheckBoxPreference submit_id;
    private SharedPreferences prefs;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        addPreferencesFromResource(R.xml.pref_general);

        submit_id = (CheckBoxPreference) this.findPreference("submit_device_id");
        submit_id.setOnPreferenceChangeListener(this);
        this.updateDeviceIDHelpText();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.submit_id){
            boolean doSubmit = (Boolean) newValue;

            if (doSubmit){
                Util.genDevID(this);
            }
            this.updateDeviceIDHelpText(doSubmit);
        }
        return true;
    }


    private void updateDeviceIDHelpText(){
        this.updateDeviceIDHelpText(this.submit_id.isChecked());
    }

    private void updateDeviceIDHelpText(boolean doSubmit){

        String description;
        if (doSubmit){
            String deviceId = Util.getUUID(this);
            description = "Device ID: ".concat(deviceId);
        }
        else{
            description = "Device ID will not be submitted.";
        }
        this.submit_id.setSummary(description);
    }

    @Override
    public boolean onIsMultiPane() {return false;}

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    private static boolean isSimplePreferences(Context context) {return true;}

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        return;
    }


}
