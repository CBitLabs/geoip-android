package com.cbitlabs.geoip;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class SettingsActivity extends Activity {
    private ArrayAdapter<String> notifcationAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_settings);
        setAdapter();
        setListView();
        loadAdapter();
    }

    private void setListView() {
        // basic setup of the ListView and adapter
        final ListView lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(notifcationAdaptor);
        lv.setEmptyView(findViewById(R.id.empty_element));
        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                TextView textView = (TextView) view.findViewById(R.id.notification_ssid);
                final String ssid = textView.getText().toString();
                AlertDialog dialog = createDialog(ssid);
                dialog.show();

            }

        });
    }

    private AlertDialog createDialog(final String ssid) {


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.rm_notification_title)
                .setPositiveButton(R.string.rm_notification, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NotificationManager.rmNetworkNotification(builder.getContext(), ssid);
                        notifcationAdaptor.remove(ssid);
                        notifcationAdaptor.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    private void setAdapter() {
        if (notifcationAdaptor == null) {
            notifcationAdaptor = new ArrayAdapter<String>(this, R.layout.notification_item, R.id.notification_ssid);
        }

    }

    private void loadAdapter() {
        Set<String> networks = NotificationManager.getNetworks(getApplicationContext());
        notifcationAdaptor.addAll(networks);
    }


}
