package com.cbitlabs.bitwise;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jblum on 3/4/14.
 * Wrapper for string set preferences
 */
public class StringSetPrefManager extends PrefManager {

    private String prefKey;

    public StringSetPrefManager(Context c, String prefKey) {
        super(c);
        this.prefKey = prefKey;
    }

    public String getPrefKey() {
        return prefKey;
    }

    public void addString(String string) {
        Set<String> set = getSet();
        set.add(string);
        edit(set);
    }

    public void rmString(String string) {
        Set<String> set = getSet();
        set.remove(string);
        edit(set);
    }

    public boolean contains(String string) {
        Set<String> set = getSet();
        return set.contains(string);
    }

    public void clearAll() {
        edit(new HashSet<String>());
    }

    public Set<String> getSet() {
        return new HashSet<String>(prefs.getStringSet(getPrefKey(), new HashSet<String>()));
    }

    private void edit(Set<String> set) {
        editor.putStringSet(getPrefKey(), set);
        editor.commit();
    }
}
