package com.cbitlabs.geoip;

import android.content.Context;

/**
 * Created by jblum on 3/6/14.
 */
public class NotificationCacheManager extends StringSetPrefManager {

    private final String expKey;
    private final long expDate;

    public NotificationCacheManager(Context c, String prefKey, long expDate) {
        super(c, prefKey);
        expKey = prefKey + "_expiration";
        this.expDate = expDate;
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
        return prefs.getLong(expKey, getToday().getTime());
    }

    private void setExpDate() {
        long exp = getExpDate();
        if (isExpired(exp, expDate)) {
            editor.putLong(expKey, getToday().getTime());
            editor.commit();
        }
    }
}
