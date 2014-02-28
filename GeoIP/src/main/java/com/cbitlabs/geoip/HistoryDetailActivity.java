package com.cbitlabs.geoip;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by jblum on 2/25/14.
 */
public class HistoryDetailActivity extends DetailActivity {

    @Override
    protected void setupView() {
        HistoryItem historyItem = (HistoryItem) getIntent().getSerializableExtra(HistoryItem.SER_KEY);
        Rating rating = historyItem.getRating();

        setViewText(historyItem.getLoc(), R.id.item_loc);
        setViewText(historyItem.getCreated_at_human(), R.id.item_created_at_human);
        setViewText(historyItem.getSsid(), R.id.detail_ssid);
        setRatingDetails(rating);
    }
}
