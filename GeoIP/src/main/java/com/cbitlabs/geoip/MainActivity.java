package com.cbitlabs.geoip;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private ScanAdaptor scanAdaptor = null;
    private Timer autoUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //start reporting geoIP in the background.
        startService(new Intent(this, ReportIntentService.class));

        setScanAdaptor();
        setListView();

        loadNetworks();
    }

    private void setScanAdaptor() {
        if (scanAdaptor == null) {
            scanAdaptor = new ScanAdaptor(this, 0);
        }
    }

    private void setListView() {
        // basic setup of the ListView and adapter
        setContentView(R.layout.activity_main);
        final ListView lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(scanAdaptor);
        lv.setEmptyView(findViewById(R.id.empty_element));
        lv.setClickable(true);
        lv.setLongClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                TextView textView = (TextView) view.findViewById(R.id.scan_connected);

                if (textView.getText().equals("Connected")) {
                    return;
                }

                final ScanResult result = (ScanResult) parent.getItemAtPosition(position);

                if (!Util.connectToNetwork(getApplicationContext(), result.SSID)) {
                    startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                }

                TextView first = (TextView) lv.getChildAt(0).findViewById(R.id.scan_connected);
                first.setText("");
                textView.setText("Connecting...");
            }

        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view,
                                           int position, long id) {
                Toast.makeText(MainActivity.this, "Long clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void loadNetworks() {
        List<ScanResult> results = Util.getAvailableWifiScan(this);
        scanAdaptor.clear();
        scanAdaptor.addAll(results);
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
                loadNetworks();
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

    @Override
    public void onResume() {
        super.onResume();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        loadNetworks();
                    }
                });
            }
        }, 0, 15 * 1000); // updates each 40 secs
    }

    @Override
    public void onPause() {
        autoUpdate.cancel();
        super.onPause();
    }
}
