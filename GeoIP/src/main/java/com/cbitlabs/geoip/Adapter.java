package com.cbitlabs.geoip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by jblum on 2/28/14.
 * Wrapper for ArrayAdaptor class. Used to set text and images.
 */
abstract class Adapter extends ArrayAdapter {

	public Adapter(Context c, int resource) {
		super(c, resource);
	}

	protected View setAdaptorText(View convertView, String text, int id) {
		TextView textView = (TextView) convertView.findViewById(id);
		textView.setText(text);
		return convertView;
	}

	protected View setAdaptorImage(View convertView, int resource, int id) {
		ImageView imageView = (ImageView) convertView.findViewById(id);
		imageView.setImageResource(resource);
		if (resource == 0) {
			imageView.setVisibility(View.GONE);
		}
		return convertView;
	}

	protected View getConvertView(View convertView, int resource) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resource, null);
		}
		return convertView;
	}
}
