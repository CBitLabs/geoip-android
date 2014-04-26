package com.cbitlabs.bitwise;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

/**
 * Created by jblum on 3/6/14.
 */
public class OpenNetworkNotificationBuilder extends NotificationBuilder {

    private ArrayList<String> ssids;

    private static final int LARGE_ICON = R.drawable.ic_action_network_wifi;
    private static final int SMALL_ICON = R.drawable.ic_action_network_wifi;
    private static final String CONTENT_TITLE = "See Wi-Fi security ratings";

    private static final String BIG_CONTENT_TITLE = "View security ratings for networks";
    private static final String SUMMARY_TEXT = "Wi-Fi networks available.";

    public static final String EXTRAS_KEY = "ssids";
    public static final String DELETE_ACTION = OpenNetworkNotification.TAG + "_cancelled";

    public OpenNetworkNotificationBuilder(Context c, ArrayList<String> ssids) {
        super(c, SMALL_ICON, LARGE_ICON, CONTENT_TITLE, DELETE_ACTION, EXTRAS_KEY);
        this.ssids = ssids;
    }

    protected int getNumber() {
        return ssids.size();
    }

    protected NotificationCompat.BigTextStyle getStyle() {
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();

        style.setBigContentTitle(BIG_CONTENT_TITLE);
        style.setSummaryText(SUMMARY_TEXT);
        return style;
    }

    protected ArrayList<String> getExtras() {
        return ssids;
    }

}
