package com.cbitlabs.geoip;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jblum on 3/1/14.
 */
abstract class PrefManager {

    protected static final long ONE_DAY = 24 * 60 * 60 * 1000;
    protected SharedPreferences prefs;
    protected SharedPreferences.Editor editor;

    public PrefManager(Context c) {
        prefs = PreferenceManager.getDefaultSharedPreferences(c);
        editor = prefs.edit();
    }

    protected SharedPreferences getPreferences() {
        return prefs;
    }

    protected SharedPreferences.Editor getEditor() {
        return editor;
    }

    protected Date getToday() {
        return Calendar.getInstance().getTime();
    }

    protected boolean isExpired(long cacheDate, long lifetime) {
        return getToday().getTime() - cacheDate > lifetime;
    }

    protected void remove(String prefKey) {
        getEditor().remove(prefKey);
    }
}

