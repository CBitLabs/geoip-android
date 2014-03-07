package com.cbitlabs.geoip;

import android.content.Context;

/**
 * Created by jblum on 3/4/14.
 */
public class InfectedNotificationCacheManager extends StringSetPrefManager {

    private static final String PREF_KEY = "infected_notification";
    private static final String EXP_KEY = PREF_KEY + "_expiration";

    public InfectedNotificationCacheManager(Context c) {
        super(c, PREF_KEY);
    }

    public void addString(String string) {
        setExpDate();
        super.addString(string);
    }

    public boolean contains(String string) {
        long expDate = getExpDate();
        if (isExpired(expDate, ONE_DAY)) {
            clearAll();
            return false;
        }
        return super.contains(string);
    }

    private long getExpDate() {
        return prefs.getLong(EXP_KEY, getToday().getTime());
    }

    private void setExpDate() {
        long expDate = getExpDate();
        if (isExpired(expDate, ONE_DAY)) {
            editor.putLong(EXP_KEY, getToday().getTime());
            editor.commit();
        }
    }

}
