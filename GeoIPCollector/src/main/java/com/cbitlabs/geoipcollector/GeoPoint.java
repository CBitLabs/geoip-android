package com.cbitlabs.geoipcollector;

/**
 * Created by stuart on 11/25/13.
 */
public class GeoPoint {
    private double lat, lng;
    protected boolean isValid;

    GeoPoint(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.isValid = true;
    }

    public static GeoPoint getNullPoint() {
        GeoPoint g = new GeoPoint(0, 0);
        g.isValid = false;
        return g;
    }

    public static boolean isValidPoint(GeoPoint g) {
        return g.isValid;
    }

    public String getLat() {
        return Double.toString(this.lat);
    }

    public String getLng() {
        return Double.toString(this.lng);
    }

    public String toString() {
        return String.format("%3.3f.%3.3f", this.lat, this.lng);
    }
}
