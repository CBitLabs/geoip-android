package com.cbitlabs.geoipcollector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class MainActivity extends Activity {

    ArrayAdapter<JsonObject> historyAdaptor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //start reporting geoIP in the background.
        Intent intent = new Intent(this, ReportIntentService.class);
        startService(intent);

        // create a history adapter for our list view
        if (historyAdaptor == null) {
            historyAdaptor = new ArrayAdapter<JsonObject>(this, 0) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null)
                        convertView = getLayoutInflater().inflate(R.layout.history_item, null);

                    JsonObject item = getItem(position);

                    TextView text = (TextView) convertView.findViewById(R.id.item);
                    text.setText(item.toString());
                    return convertView;
                }
            };
        }

        // basic setup of the ListView and adapter
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(historyAdaptor);

        // authenticate and do the first load
        load();
    }

    // This "Future" tracks loading operations.
    // A Future is an object that manages the state of an operation
    // in progress that will have a "Future" result.
    // You can attach callbacks (setCallback) for when the result is ready,
    // or cancel() it if you no longer need the result.
    Future<JsonArray> loading;

    private void load() {
        // don't attempt to load more if a load is already in progress
        if (loading != null && !loading.isDone() && !loading.isCancelled())
            return;

        String url = Util.getHistoryUrl(Util.getUUID(this));

        // This request loads a URL as JsonArray and invokes
        // a callback on completion.
        loading = Ion.with(this, url)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        // this is called back onto the ui thread, no Activity.runOnUiThread or Handler.post necessary.
                        if (e != null) {
                            Toast.makeText(MainActivity.this, "Error loading history", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.i(Util.TAG, "Found history: " + result.toString());
                        Toast.makeText(MainActivity.this, "Successfully loaded " + Integer.toString(result.size()) + " history items.", Toast.LENGTH_SHORT).show();

                        historyAdaptor.clear();

                        for (int i = 0; i < result.size(); i++) {
                            historyAdaptor.add(result.get(i).getAsJsonObject());
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Log.i(Util.TAG, "Settings!");
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendReport(View view) {
        Util.createReportingTask(this);
    }

    public void refreshHistory(View view) {
        load();
    }

}
