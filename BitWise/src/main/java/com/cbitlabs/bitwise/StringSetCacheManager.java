package com.cbitlabs.bitwise;

import android.content.Context;

/**
 * Created by jblum on 3/6/14.
 * String set with caching.
 */
public class StringSetCacheManager extends StringSetPrefManager {

    private final String expKey;
    private final long expLife;

    public StringSetCacheManager(Context c, String prefKey, long expDate) {
        super(c, prefKey);
        expKey = prefKey + "_expiration";
        this.expLife = expDate;
    }

    public void addString(String string) {
        setExpDate();
        super.addString(string);
    }

    public boolean contains(String string) {
        long expDate = getExpLife();
        if (isExpired(expDate, ONE_DAY)) {
            clearAll();
            return false;
        }
        return super.contains(string);
    }

    private long getExpLife() {
        return prefs.getLong(expKey, getToday().getTime());
    }

    private void setExpDate() {
        long exp = getExpLife();
        if (isExpired(exp, expLife)) {
            editor.putLong(expKey, getToday().getTime());
            editor.commit();
        }
    }
}