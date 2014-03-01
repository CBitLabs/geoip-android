package com.cbitlabs.geoip;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jblum on 3/1/14.
 */
abstract class PrefManager {

    protected static SharedPreferences.Editor getEditor(Context c) {
        return getPreferences(c).edit();
    }

    protected static SharedPreferences getPreferences(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }
}

