package com.cbitlabs.geoip;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private ArrayAdapter<ScanResult> scanAdaptor = null;
    private HashMap<Integer, String> itemMap;

    public MainActivity() {
        itemMap = new HashMap<Integer, String>() {
            {
                put(R.id.item_ssid, "ssid");
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //start reporting geoIP in the background.
        Intent intent = new Intent(this, ReportIntentService.class);
        startService(intent);

        // create a scan adapter for our list view
        if (scanAdaptor == null) {
            scanAdaptor = new ArrayAdapter<ScanResult>(this, 0) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null)
                        convertView = getLayoutInflater().inflate(R.layout.history_item, null);

                    ScanResult item = getItem(position);

                    for (Map.Entry<Integer, String> el : itemMap.entrySet()) {
                        convertView = setAdaptorText(convertView, item, el.getKey(), el.getValue());
                    }
                    return convertView;
                }
            };
        }

        // basic setup of the ListView and adapter
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(scanAdaptor);
        listView.setEmptyView(findViewById(R.id.empty_element));
        load();
    }

    private View setAdaptorText(View convertView, ScanResult item, int id, String key) {
        TextView text = (TextView) convertView.findViewById(id);
        text.setText(item.SSID); //todo fix
        return convertView;
    }

    private void load() {

        List<ScanResult> results = Util.getAvailableWifiScan(this);
        HashMap<String, ScanResult> cleanResults = cleanScanReport(results);
        for (Map.Entry<String, ScanResult> entry : cleanResults.entrySet()) {
            scanAdaptor.add(entry.getValue());
        }
    }

    private HashMap<String, ScanResult> cleanScanReport(List<ScanResult> results) {
        HashMap<String, ScanResult> cleanResults = new HashMap<String, ScanResult>();
        for (ScanResult result : results) {
            String ssid = result.SSID;
            if (ssid == "") {
                continue;
            }
            if (cleanResults.containsKey(ssid)) {
                ScanResult el = cleanResults.get(ssid);
                if (el.level > result.level) {
                    cleanResults.put(ssid, result);
                }

            } else {
                cleanResults.put(ssid, result);
            }
        }
        return cleanResults;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent i;
        switch (item.getItemId()) {

            case R.id.refreshScan:
                load();
                return true;

            case R.id.action_settings:
                Log.i(Util.LOG_TAG, "Settings!");
                i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;

            case R.id.viewHistory:
                i = new Intent(this, HistoryActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
