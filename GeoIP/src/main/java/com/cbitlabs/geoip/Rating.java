package com.cbitlabs.geoip;

import java.io.Serializable;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.Util;

/**
 * Created by jblum on 2/21/14.
 */

public class Rating implements Serializable {
	private static final long serialVersionUID = 7526472295622776147L;
	public static final String SER_KEY = "com.cbitlabs.geoip.Rating";
	private static final int infectedIcon = R.drawable.status_bad;
	private static final int notInfectedIcon = R.drawable.status_good;
	private static final int noRatingIcon = R.drawable.status_unknown;
	private final int spam_count;
	private final int spam_freq;
	private final int bot_count;
	private final int bot_freq;
	private final int unexp_count;
	private final int unexp_freq;
	private final int raw_score;
	private final boolean infected;
	private boolean validRating;
	private final int statusicon;

	private final String ssid;
	private final String raw_ssid;

	public Rating(final JsonObject rating, final String ssid) {
		raw_ssid = ssid;
		this.ssid = GenUtil.fmtSSID(ssid);
		spam_count = rating.get("spam_count").getAsInt();
		spam_freq = rating.get("spam_freq").getAsInt();
		bot_count = rating.get("bot_count").getAsInt();
		bot_freq = rating.get("bot_freq").getAsInt();
		unexp_count = rating.get("unexp_count").getAsInt();
		unexp_freq = rating.get("unexp_freq").getAsInt();
		raw_score = rating.get("raw_score").getAsInt();
		infected = rating.get("is_infected").getAsBoolean();
		try {
			validRating = rating.get("valid_rating").getAsBoolean();
		} catch (NullPointerException e) {
			validRating = false;
		}

		if (validRating) {
			statusicon = infected ? infectedIcon : notInfectedIcon;
		} else {
			statusicon = noRatingIcon;
		}

	}

	// Null object if server cannot be contacted
	public Rating(final String ssid) {
		raw_ssid = ssid;
		this.ssid = GenUtil.fmtSSID(ssid);
		spam_count = 0;
		spam_freq = 0;
		bot_count = 0;
		bot_freq = 0;
		unexp_count = 0;
		unexp_freq = 0;
		raw_score = 0;
		infected = false;
		statusicon = noRatingIcon;
		validRating = false;

	}

	public int getSpam_count() {
		return spam_count;
	}

	public int getSpam_freq() {
		return spam_freq;
	}

	public int getBot_count() {
		return bot_count;
	}

	public int getBot_freq() {
		return bot_freq;
	}

	public int getUnexp_count() {
		return unexp_count;
	}

	public int getUnexp_freq() {
		return unexp_freq;
	}

	public int getRaw_score() {
		return raw_score;
	}

	public boolean isInfected() {
		return infected;
	}

	@Deprecated
	public int getIcon() {
		return 0;
	}

	public String getSsid() {
		return ssid;
	}

	public String getRawSsid() {
		return raw_ssid;
	}

	public int notificationIcon(final Context c) {
		NotificationStorageManager storageManager = new NotificationStorageManager(c);
		boolean hasNotification = storageManager.contains(ssid);
		return hasNotification ? R.drawable.watched : android.R.color.transparent;
	}

	public int statusIcon() {
		return statusicon;
	}

	@Override
	public String toString() {
		return "Rating{" + "spam_count=" + spam_count + ", spam_freq=" + spam_freq + ", bot_count=" + bot_count
				+ ", bot_freq=" + bot_freq + ", unexp_count=" + unexp_count + ", unexp_freq=" + unexp_freq
				+ ", raw_score=" + raw_score + ", is_infected=" + infected + ", statusicon=" + statusicon + ", ssid='"
				+ ssid + '\'' + '}';
	}
}
