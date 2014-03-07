package com.cbitlabs.geoip;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.Date;

/**
 * Created by jblum on 3/7/14.
 */
public class GeoUtil {

    public static GeoPoint getLocation(final Context c) {

        Location netLoc = getNetworkLocation(c);
        Location gpsLoc = getGPSLocation(c);
        Location l;

        if (isBetterLocation(gpsLoc, netLoc))
            l = gpsLoc;
        else
            l = netLoc;

        GeoPoint p;
        if (l == null) {
            p = GeoPoint.getNullPoint();
        } else {
            if (isRecentLocation(l)) {
                p = GeoPoint.getNullPoint();
            } else
                p = new GeoPoint(l.getLatitude(), l.getLongitude());
        }
        return p;
    }

    protected static Location getNetworkLocation(final Context c) {
        LocationManager locationManager = (LocationManager) c.getSystemService(c.LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    protected static Location getGPSLocation(final Context c) {
        LocationManager locationManager = (LocationManager) c.getSystemService(c.LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    protected static boolean isRecentLocation(Location l) {
        long locAge = (new Date()).getTime() - l.getTime();
        return locAge > Util.TEN_MINUTES;
    }


    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
        if (location == null) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > Util.FIVE_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -Util.FIVE_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
