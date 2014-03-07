package com.cbitlabs.geoip;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.ArrayList;

/**
 * Created by jblum on 3/6/14.
 */
abstract class NotificationBuilder {
    protected static final int notifyID = 1;
    protected Context c;
    private final int smallIcon;
    private final int largeIcon;
    private final String contentTitle;
    private final String deleteAction;
    private final String extrasKey;

    public NotificationBuilder(Context c, int smallIcon, int largeIcon,
                               String contentTitle,
                               String deleteAction,
                               String extrasKey) {
        this.c = c;
        this.smallIcon = smallIcon;
        this.largeIcon = largeIcon;
        this.contentTitle = contentTitle;
        this.deleteAction = deleteAction;
        this.extrasKey = extrasKey;
    }

    public void build() {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(c)
                        .setLargeIcon(getLargeIcon())
                        .setSmallIcon(getSmallIcon())
                        .setContentTitle(getContentTitle())
                        .setNumber(getNumber())
                        .setStyle(getStyle());

        mBuilder.setContentIntent(getContentIntent(c, MainActivity.class));
        mBuilder.setDeleteIntent(getDeleteIntent(c, getDeleteAction(), getExtrasKey(), getExtras()));

        NotificationManager mNotificationManager =
                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(notifyID, mBuilder.build());
    }

    protected int getSmallIcon() {
        return smallIcon;
    }

    protected Bitmap getLargeIcon() {
        return getBitmapFromResource(c, largeIcon);
    }

    protected String getContentTitle() {
        return contentTitle;
    }

    protected String getDeleteAction() {
        return deleteAction;
    }

    protected String getExtrasKey() {
        return extrasKey;
    }

    abstract int getNumber();

    abstract ArrayList<String> getExtras();

    abstract NotificationCompat.Style getStyle();

    protected static Bitmap getBitmapFromResource(Context c, int icon) {
        return BitmapFactory.decodeResource(c.getResources(),
                icon);
    }

    protected static PendingIntent getContentIntent(Context c, Class cls) {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(c, cls);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating bacvkward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(cls);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected static PendingIntent getDeleteIntent(Context c, String action, String extrasKey, ArrayList<String> extras) {
        Intent intent = new Intent(c, NotificationBroadcastReceiver.class);
        intent.setAction(action);
        intent.putExtra(extrasKey, extras);
        return PendingIntent.getBroadcast(c, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
