package com.cbitlabs.geoip;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by jblum on 2/28/14.
 */
public class HistoryAdapter extends Adapter {

    public HistoryAdapter(Context c, int resource) {
        super(c, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = getConvertView(convertView, R.layout.history_item);

        HistoryItem historyItem = (HistoryItem) getItem(position);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.rating_icon);
        imageView.setImageResource(historyItem.getRating().getIcon());
        convertView = setAdaptorText(convertView,
                historyItem.getSsid(), R.id.item_ssid);
        convertView = setAdaptorText(convertView,
                historyItem.getLoc(), R.id.item_loc);
        convertView = setAdaptorText(convertView,
                historyItem.getCreated_at_human(), R.id.item_created_at_human);
        convertView = setAdaptorImage(convertView, historyItem.getRating().notificationIcon(getContext()), R.id.hasNotification);
        return convertView;
    }

}
