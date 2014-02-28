package com.cbitlabs.geoip;

import android.net.wifi.ScanResult;

import com.google.gson.JsonObject;

/**
 * Created by jblum on 2/21/14.
 */
public class ScanRating {
    private final ScanResult scanResult;
    private final Rating rating;

    public ScanRating(ScanResult result, JsonObject rating) {
        this.scanResult = result;
        this.rating = new Rating(rating, result.SSID);
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public Rating getRating() {
        return rating;
    }
}
