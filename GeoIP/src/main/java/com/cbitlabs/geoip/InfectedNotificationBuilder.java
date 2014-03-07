package com.cbitlabs.geoip;

import android.content.Context;
import android.graphics.Bitmap;
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

    public static final String DELETE_ACTION = "infected_notification_cancelled";
    public static final String EXTRAS_KEY = "ssids";

    public InfectedNotificationBuilder(Context c, ArrayList<String> ssids) {
        super(c);
        this.ssids = ssids;
    }

    public void build() {

        Log.i(Util.LOG_TAG, "Building notification for ssids: " + ssids);

        if (ssids.size() == 0) {
            return;
        }

        super.build();
    }

    protected Bitmap getLargeIcon() {
        return getBitmapFromResource(c, LARGE_ICON);
    }

    protected int getSmallIcon() {
        return SMALL_ICON;
    }

    protected String getContentTitle() {
        return CONTENT_TITLE;
    }

    protected int getNumber() {
        return ssids.size();
    }

    protected ArrayList<String> getExtras() {
        return ssids;
    }

    protected String getExtrasKey() {
        return EXTRAS_KEY;
    }

    protected String getDeleteAction() {
        return DELETE_ACTION;
    }

    protected NotificationCompat.InboxStyle getStyle() {
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle(BIG_CONTENT_TITLE);
        for (String ssid : ssids) {
            inboxStyle.addLine(ssid);
        }
        return inboxStyle;
    }

}
