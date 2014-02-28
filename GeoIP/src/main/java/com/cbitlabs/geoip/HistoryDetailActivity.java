package com.cbitlabs.geoip;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by jblum on 2/25/14.
 */
public class HistoryDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rating_detail);
        HistoryItem historyItem = (HistoryItem) getIntent().getSerializableExtra(HistoryItem.SER_KEY);

        if (historyItem == null) {
            return;
        }

        Log.i(Util.LOG_TAG, historyItem.toString());


        setViewText(historyItem.getLoc(), R.id.item_loc);
        setViewText(historyItem.getCreated_at_human(), R.id.item_created_at_human);
        setViewText(historyItem.getSsid(), R.id.detail_ssid);

        Rating rating = historyItem.getRating();
        setAdaptorImage(rating.getIcon(), R.id.rating_icon);
        prependCount(rating.getSpam_count(), R.id.spam_count);
        prependCount(rating.getBot_count(), R.id.bot_count);
        prependCount(rating.getUnexp_count(), R.id.unexp_count);
    }

    private void prependCount(int count, int id) {
        TextView textView = (TextView) findViewById(id);
        CharSequence text = String.format("%d %s", count, textView.getText());
        textView.setText(text);
    }

    private void setViewText(String text, int id) {
        TextView textView = (TextView) findViewById(id);
        textView.setText(text);
    }

    private void setAdaptorImage(int resource, int id) {
        ImageView imageView = (ImageView) findViewById(id);
        imageView.setImageResource(resource);
    }
}
