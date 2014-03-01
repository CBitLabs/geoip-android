package com.cbitlabs.geoip;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by jblum on 2/25/14.
 */
public abstract class DetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rating_detail);
        setupView();
    }

    protected abstract void setupView();

    protected void setRatingDetails(Rating rating) {
        setAdaptorImage(rating.getIcon(), R.id.rating_icon);
        setViewText(rating.getSsid(), R.id.detail_ssid);
        prependCount(rating.getSpam_count(), R.id.spam_count);
        prependCount(rating.getBot_count(), R.id.bot_count);
        prependCount(rating.getUnexp_count(), R.id.unexp_count);
    }

    protected void prependCount(int count, int id) {
        TextView textView = (TextView) findViewById(id);
        CharSequence text = String.format("%d %s", count, textView.getText());
        textView.setText(text);
    }

    protected void setViewText(String text, int id) {
        TextView textView = (TextView) findViewById(id);
        textView.setText(text);
    }

    protected void setAdaptorImage(int resource, int id) {
        ImageView imageView = (ImageView) findViewById(id);
        imageView.setImageResource(resource);
    }

    protected void setupBtn(Rating rating) {

        Button btn = (Button) findViewById(R.id.button);
        final String ssid = rating.getSsid();
        setBtnText(ssid);
        btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Context c = getApplicationContext();
                boolean hasNotification = NotificationManager.hasNotification(getApplicationContext(), ssid);
                if (hasNotification) {
                    NotificationManager.rmNetworkNotification(c, ssid);
                } else {
                    NotificationManager.addNetworkNotification(c, ssid);
                }
                setBtnText(ssid);
            }
        });
    }

    private void setBtnText(String ssid) {
        Button btn = (Button) findViewById(R.id.button);
        boolean hasNotification = NotificationManager.hasNotification(getApplicationContext(), ssid);
        if (hasNotification) {
            btn.setText(R.string.rm_notification_btn);
        } else {
            btn.setText(R.string.set_notification_btn);
        }
    }


}
