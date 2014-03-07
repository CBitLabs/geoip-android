package com.cbitlabs.geoip;

import android.content.Context;

/**
 * Created by jblum on 3/4/14.
 */
public class InfectedNotificationCacheManager extends NotificationCacheManager {

    public InfectedNotificationCacheManager(Context c) {
        super(c, "infected_notification", ONE_DAY);
    }
}
