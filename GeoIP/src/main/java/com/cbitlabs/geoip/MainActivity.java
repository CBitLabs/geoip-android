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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private ScanAdapter scanAdaptor = null;
    private Timer autoUpdate;
    private final int TWENTY_SECONDS = 20 * 1000;
    private Menu menu;

    // This "Future" tracks loading operations.
    // A Future is an object that manages the state of an operation
    // in progress that will have a "Future" result.
    // You can attach callbacks (setCallback) for when the result is ready,
    // or cancel() it if you no longer need the result.
    Future<JsonObject> loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start reporting geoIP in the background.
        startService(new Intent(getApplicationContext(), ReportIntentService.class));

        setScanAdaptor();
        setListView();

        loadNetworks();
    }

    private void setScanAdaptor() {
        if (scanAdaptor == null) {
            scanAdaptor = new ScanAdapter(this, 0);
        }
    }

    private void setListView() {
        // basic setup of the ListView and adapter
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

                final ScanRating result = (ScanRating) parent.getItemAtPosition(position);

                if (!Util.connectToNetwork(getApplicationContext(), result.getScanResult().SSID)) {
                    startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                }
                for (int i = 0; i < lv.getCount(); i++) {
                    TextView clearText = (TextView) lv.getChildAt(i).findViewById(R.id.scan_connected);
                    clearText.setText("");
                }

                textView.setText("Connecting...");
            }

        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view,
                                           int position, long id) {
                ScanRating scanRating = (ScanRating) parent.getItemAtPosition(position);
                Intent intent = new Intent(view.getContext(), ScanDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(scanRating.getRating().SER_KEY, scanRating.getRating());
                intent.putExtras(bundle);
                startActivity(intent);

                return true;
            }
        });
    }

    public void onToggleClicked() {
        boolean enable = !Util.isWifiEnabled(this);
        if (enable) {
            setWifiOnIcon();
            Util.enableWifi(this);
            loadNetworks();
        } else {
            setWifiOffIcon();
            setnoWifiText();
            scanAdaptor.clear();
            Util.disableWifi(this);
        }
    }

    private void loadNetworks() {

        if (Util.isWifiEnabled(this)) {
            setWifiOnIcon();
        } else {
            setnoWifiText();
            setWifiOffIcon();
            scanAdaptor.clear();
            return;
        }

        final List<ScanResult> results = Util.getNewScanResults(this, scanAdaptor);

        if (results.size() == 0) {
            setEmptyText();
            return;
        }

        String url = Util.getScanRatingUrl(results);

        loading = Ion.with(this, url)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject jsonRepsonse) {
                        // this is called back onto the ui thread, no Activity.runOnUiThread or Handler.post necessary.
                        if (e != null) {
                            Log.i(Util.LOG_TAG, e.toString());
                            return;
                        }
                        Log.i(Util.LOG_TAG, "Found ratings: " + jsonRepsonse.toString());
                        List<ScanRating> ratings = new ArrayList<ScanRating>();
                        for (ScanResult result : results) {

                            JsonElement rating = jsonRepsonse.get(Util.fmtBSSID(result.BSSID));
                            if (rating != null) {
                                ratings.add(new ScanRating(result, rating.getAsJsonObject()));
                            }
                        }
                        scanAdaptor.clear();
                        scanAdaptor.addAll(ratings);
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        if (Util.isWifiEnabled(this)) {
            setWifiOnIcon();
        } else {
            setWifiOffIcon();
        }


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

            case R.id.toggleWifi:
                onToggleClicked();
                return true;

            case R.id.action_settings:
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

    private void setnoWifiText() {
        setText(R.string.no_wifi);
    }

    private void setEmptyText() {
        setText(R.string.empty_element);
    }

    private void setText(int resource) {
        TextView textView = (TextView) findViewById(R.id.empty_element
        );
        if (textView != null) {
            textView.setText(resource);
        }

    }

    private void setWifiOffIcon() {
        setWifiIcon(R.drawable.ic_action_network_cell);
    }

    private void setWifiOnIcon() {
        setWifiIcon(R.drawable.ic_action_network_wifi);
    }

    private void setWifiIcon(int resource) {
        if (menu == null) {
            return;
        }

        MenuItem item = menu.findItem(R.id.toggleWifi);
        item.setIcon(resource);

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
        }, 0, TWENTY_SECONDS);
    }

    @Override
    public void onPause() {
        autoUpdate.cancel();
        super.onPause();
    }
}
