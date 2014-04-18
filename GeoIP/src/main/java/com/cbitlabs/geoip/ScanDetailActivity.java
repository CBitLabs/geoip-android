package com.cbitlabs.geoip;

/**
 * Created by jblum on 2/25/14.
 */
public class ScanDetailActivity extends DetailActivity {

	@Override
	protected void setupView() {
		Rating rating = (Rating) getIntent().getSerializableExtra(Rating.SER_KEY);
		setupBtn(rating);
		setRatingDetails(rating);
	}
}
