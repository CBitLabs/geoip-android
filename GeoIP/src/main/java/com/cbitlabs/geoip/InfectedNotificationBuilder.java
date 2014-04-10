package com.cbitlabs.geoip;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by jblum on 3/4/14.
 */
public class InfectedNotificationBuilder extends NotificationBuilder {

    private ArrayList<String> ssids;
    private static final String CONTENT_TITLE = "Malicious events on watched network!";
    private static final int LARGE_ICON = R.drawable.ic_action_warning_dark;
    private static final int SMALL_ICON = R.drawable.ic_action_warning_dark;
    private static final String BIG_CONTENT_TITLE = "Affected networks:";

    public static final String DELETE_ACTION = InfectedNotification.TAG + "_cancelled";
    public static final String EXTRAS_KEY = "ssids";

    public InfectedNotificationBuilder(Context c, ArrayList<String> ssids) {
        super(c, SMALL_ICON, LARGE_ICON, CONTENT_TITLE, DELETE_ACTION, EXTRAS_KEY);
        this.ssids = ssids;
    }

    public void build() {

        Log.i(GenUtil.LOG_TAG, "Building notification for ssids: " + ssids);

        super.build();
    }

    protected int getNumber() {
        return ssids.size();
    }

    protected ArrayList<String> getExtras() {
        return ssids;
    }

    protected NotificationCompat.InboxStyle getStyle() {
        NotificationCompat.InboxStyle style =
                new NotificationCompat.InboxStyle();

        style.setBigContentTitle(BIG_CONTENT_TITLE);
        for (String ssid : ssids) {
            style.addLine(GenUtil.cleanSSID(ssid));
        }
        return style;
    }

}
