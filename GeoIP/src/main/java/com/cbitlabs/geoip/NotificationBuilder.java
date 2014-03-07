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

    public NotificationBuilder(Context c) {
        this.c = c;
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

    abstract int getSmallIcon();

    abstract Bitmap getLargeIcon();

    abstract String getContentTitle();

    abstract int getNumber();

    abstract NotificationCompat.Style getStyle();

    abstract String getDeleteAction();

    abstract String getExtrasKey();

    abstract ArrayList<String> getExtras();

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
