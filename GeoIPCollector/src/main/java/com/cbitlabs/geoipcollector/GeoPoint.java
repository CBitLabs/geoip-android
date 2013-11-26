package com.cbitlabs.geoipcollector;

/**
 * Created by stuart on 11/25/13.
 */
public class GeoPoint {
    public double lat, lon;
    protected boolean isValid;
    GeoPoint(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
        this.isValid=true;
    }

    public static GeoPoint getNullPoint(){
        GeoPoint g = new GeoPoint(0,0);
        g.isValid=false;
        return g;
    }

    public static boolean isValidPoint(GeoPoint g){
        return g.isValid;
    }

    public String toString(){
        return String.format("%3.3f.%3.3f", this.lat, this.lon);
    }
}
