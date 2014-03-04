package com.cbitlabs.geoip;

import android.content.Context;

import com.google.gson.JsonObject;

import java.io.Serializable;

/**
 * Created by jblum on 2/21/14.
 */

public class Rating implements Serializable {
    private static final long serialVersionUID = 7526472295622776147L;
    public static final String SER_KEY = "com.cbitlabs.geoip.Rating";
    private static final int infectedIcon = R.drawable.ic_action_warning;
    private static final int notInfectedIcion = R.drawable.ic_action_accept;
    private final int spam_count;
    private final int spam_freq;
    private final int bot_count;
    private final int bot_freq;
    private final int unexp_count;
    private final int unexp_freq;
    private final int raw_score;
    private final boolean is_infected;
    private final int icon;
    private final String ssid;

    public Rating(JsonObject rating, String ssid) {
        this.ssid = ssid;
        spam_count = rating.get("spam_count").getAsInt();
        spam_freq = rating.get("spam_freq").getAsInt();
        bot_count = rating.get("bot_count").getAsInt();
        bot_freq = rating.get("bot_freq").getAsInt();
        unexp_count = rating.get("unexp_count").getAsInt();
        unexp_freq = rating.get("unexp_freq").getAsInt();
        raw_score = rating.get("raw_score").getAsInt();
        is_infected = rating.get("is_infected").getAsBoolean();
        icon = is_infected ? infectedIcon : notInfectedIcion;

    }

    public int getSpam_count() {
        return spam_count;
    }

    public int getSpam_freq() {
        return spam_freq;
    }

    public int getBot_count() {
        return bot_count;
    }

    public int getBot_freq() {
        return bot_freq;
    }

    public int getUnexp_count() {
        return unexp_count;
    }

    public int getUnexp_freq() {
        return unexp_freq;
    }

    public int getRaw_score() {
        return raw_score;
    }

    public boolean isIs_infected() {
        return is_infected;
    }

    public int getIcon() {
        return icon;
    }

    public String getSsid() {
        return ssid;
    }

    public int notificationIcon(Context c) {
        boolean hasNotification = NotificationManager.hasNotification(c, ssid);
        return hasNotification ? R.drawable.ic_action_about : 0;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "spam_count=" + spam_count +
                ", spam_freq=" + spam_freq +
                ", bot_count=" + bot_count +
                ", bot_freq=" + bot_freq +
                ", unexp_count=" + unexp_count +
                ", unexp_freq=" + unexp_freq +
                ", raw_score=" + raw_score +
                ", is_infected=" + is_infected +
                ", icon=" + icon +
                ", ssid='" + ssid + '\'' +
                '}';
    }
}
