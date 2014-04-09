package com.cbitlabs.geoip;

import android.app.Activity;
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
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rating_detail);
		getActionBar().setDisplayShowHomeEnabled(false);

		setupView();
	}

	protected abstract void setupView();

	protected void setRatingDetails(final Rating rating) {
		setAdaptorImage(rating.getIcon(), R.id.rating_icon);
		setViewText(rating.getRawSsid(), R.id.detail_ssid);
		((TextView) findViewById(R.id.spam_count)).setText("" + rating.getSpam_count());
		((TextView) findViewById(R.id.bot_count)).setText("" + rating.getBot_count());
		((TextView) findViewById(R.id.unexp_count)).setText("" + rating.getUnexp_count());
	}

	protected void setViewText(final String text, final int id) {
		TextView textView = (TextView) findViewById(id);
		textView.setText(text);
	}

	protected void setAdaptorImage(final int resource, final int id) {
		ImageView imageView = (ImageView) findViewById(id);
		if (imageView != null) {
			imageView.setImageResource(resource);
		}
	}

	protected void setupBtn(final Rating rating) {

		Button btn = (Button) findViewById(R.id.button);
		final String ssid = rating.getSsid();
		final NotificationStorageManager storageManager = new NotificationStorageManager(getApplicationContext());
		setBtnText(ssid, storageManager);
		btn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(final View v) {
				boolean hasNotification = storageManager.contains(ssid);
				if (hasNotification) {
					storageManager.rmString(ssid);
				} else {
					storageManager.addString(ssid);
				}
				setBtnText(ssid, storageManager);
			}
		});
	}

	private void setBtnText(final String ssid, final NotificationStorageManager storageManager) {
		Button btn = (Button) findViewById(R.id.button);
		boolean hasNotification = storageManager.contains(ssid);
		if (hasNotification) {
			btn.setText(R.string.rm_notification_btn);
		} else {
			btn.setText(R.string.set_notification_btn);
		}
	}

}
