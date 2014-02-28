package com.cbitlabs.geoip;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by jblum on 2/25/14.
 */
public class RatingDetailActivity extends DetailActivity {


    @Override
    protected void setupView() {
        Rating rating = (Rating) getIntent().getSerializableExtra(Rating.SER_KEY);
        setRatingDetails(rating);
        setViewText("", R.id.item_loc);
        setViewText("", R.id.item_created_at_human);
    }
}
