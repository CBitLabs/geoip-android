package com.cbitlabs.geoip;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.HashMap;
import java.util.Map;

public class HistoryActivity extends Activity {

    private ArrayAdapter<HistoryItem> historyAdaptor = null;
    private int pageNum;

    Future<JsonArray> loading;

    public HistoryActivity() {
        pageNum = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create a history adapter for our list view
        if (historyAdaptor == null) {
            historyAdaptor = new ArrayAdapter<HistoryItem>(this, 0) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null)
                        convertView = getLayoutInflater().inflate(R.layout.history_item, null);

                    HistoryItem historyItem = getItem(position);
                    ImageView imageView = (ImageView) convertView.findViewById(R.id.rating_icon);
                    imageView.setImageResource(historyItem.getRating().getIcon());
                    convertView = setHistoryAdaptorText(convertView, historyItem.getSsid(), R.id.item_ssid);
                    convertView = setHistoryAdaptorText(convertView, historyItem.getLoc(), R.id.item_loc);
                    convertView = setHistoryAdaptorText(convertView, historyItem.getCreated_at_human(), R.id.item_created_at_human);
                    return convertView;
                }
            };
        }

        // basic setup of the ListView and adapter
        setContentView(R.layout.activity_main);
        setListView();
        loadHistory(false);
    }

    private View setHistoryAdaptorText(View convertView, String text, int id) {
        TextView tv = (TextView) convertView.findViewById(id);
        tv.setText(text);
        return convertView;
    }

    private void setListView() {
        ListView lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(historyAdaptor);
        lv.setEmptyView(findViewById(R.id.empty_element));
        lv.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadHistory(false);
            }

        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                HistoryItem historyItem= (HistoryItem) parent.getItemAtPosition(position);
                Intent intent = new Intent(view.getContext(), HistoryDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(historyItem.SER_KEY, historyItem);
                intent.putExtras(bundle);
                startActivity(intent);
            }

        });

    }


    private void loadHistory(final boolean clear) {
        // don't attempt to load more if a load is already in progress
        if (loading != null && !loading.isDone() && !loading.isCancelled())
            return;
        if (clear) {
            pageNum = 0;
        }


        String url = Util.getHistoryUrl(Util.getUUID(this), pageNum);
        Log.i(Util.LOG_TAG, "Requesting history with url: " + url);

        pageNum++;

        loading = Ion.with(this, url)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        // this is called back onto the ui thread, no Activity.runOnUiThread or Handler.post necessary.
                        if (e != null) {
                            Log.i(Util.LOG_TAG, e.toString());
                            Toast.makeText(HistoryActivity.this, "Error loading history", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.i(Util.LOG_TAG, "Found history: " + result.toString());
                        if (clear) { //clear after request returns
                            historyAdaptor.clear();
                        }

                        for (int i = 0; i < result.size(); i++) {
                            JsonObject o = result.get(i).getAsJsonObject();
                            o.addProperty("ssid", Util.cleanSSID(o.get("ssid")));
                            historyAdaptor.add(new HistoryItem(o));
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.history, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.refreshHistory:
                loadHistory(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
