package com.cbitlabs.geoip;

import com.google.gson.JsonObject;

import java.io.Serializable;

/**
 * Created by jblum on 2/28/14.
 * Serialization for History object from the server
 */
public class HistoryItem implements Serializable {
    private static final long serialVersionUID = 7526472295622776147L;
    public static final String SER_KEY = "com.cbitlabs.geoip.HistoryItem";
    private final int count;
    private final String loc;
    private final String security;
    private final Rating rating;
    private final String ssid;
    private final String bssid;
    private final String remote_addr;
    private final boolean isEnterprise;
    private final String created_at;
    private final String created_at_human;
    private final String datasrc;
    private final String ip;
    private final Double lat;
    private final Double lng;
    private final String uuid;

    public HistoryItem(JsonObject historyItem) {
        count = historyItem.get("count").getAsInt();
        loc = historyItem.get("loc").getAsString();
        security = historyItem.get("security").getAsString();
        ssid = historyItem.get("ssid").getAsString();
        rating = new Rating(historyItem.get("rating").getAsJsonObject(), ssid);
        bssid = historyItem.get("bssid").getAsString();
        remote_addr = historyItem.get("remote_addr").getAsString();
        isEnterprise = historyItem.get("isEnterprise").getAsBoolean();
        created_at = historyItem.get("created_at").getAsString();
        created_at_human = historyItem.get("created_at_human").getAsString();
        datasrc = historyItem.get("datasrc").getAsString();
        ip = historyItem.get("ip").getAsString();
        lat = historyItem.get("lat").getAsDouble();
        lng = historyItem.get("lng").getAsDouble();
        uuid = historyItem.get("uuid").getAsString();
    }

    public String getCreated_at_human() {
        return created_at_human;
    }

    public static String getSerKey() {
        return SER_KEY;
    }

    public int getCount() {
        return count;
    }

    public String getLoc() {
        return loc;
    }

    public String getSecurity() {
        return security;
    }

    public Rating getRating() {
        return rating;
    }

    public String getSsid() {
        return ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public String getRemote_addr() {
        return remote_addr;
    }

    public boolean isEnterprise() {
        return isEnterprise;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getDatasrc() {
        return datasrc;
    }

    public String getIp() {
        return ip;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getUuid() {
        return uuid;
    }

    public static long getSerialVersionUID() {

        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "HistoryItem{" +
                "count=" + count +
                ", loc='" + loc + '\'' +
                ", security='" + security + '\'' +
                ", rating=" + rating +
                ", ssid='" + ssid + '\'' +
                ", bssid='" + bssid + '\'' +
                ", remote_addr='" + remote_addr + '\'' +
                ", isEnterprise=" + isEnterprise +
                ", created_at='" + created_at + '\'' +
                ", created_at_human='" + created_at_human + '\'' +
                ", datasrc='" + datasrc + '\'' +
                ", ip='" + ip + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
