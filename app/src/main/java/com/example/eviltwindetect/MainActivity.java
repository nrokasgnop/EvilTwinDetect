package com.example.eviltwindetect;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.support.v7.app.AppCompatActivity;
import android.content.BroadcastReceiver;

import android.content.Context;

import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    ListView list;
    WifiManager wifi;
    String wifis[];
    WifiScanReceiver wifiReciever;


    String networkSSID = "";
    String networkPass = "";
    static long itemSelect = 0;


    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (ListView) findViewById(R.id.listView);

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        wifi.startScan();

    }

    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class WifiScanReceiver extends BroadcastReceiver{
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifi.getScanResults();
            wifis = new String[wifiScanList.size()];

            for(int i = 0; i < wifiScanList.size(); i++){
                //wifis[i] = ((wifiScanList.get(i)).toString());
                wifis[i] = "SSID: " + ((wifiScanList.get(i).SSID).toString()) + "\n" +
                        "BSSID: "+ ((wifiScanList.get(i).BSSID).toString()) + "\n" +
                        "Capabilities: "+ ((wifiScanList.get(i).capabilities).toString()) + "\n" +
                        "Level: " + ((wifiScanList.get(i).level)) + "\n" +
                        "Frequency: " + ((wifiScanList.get(i).frequency));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    getApplicationContext(),android.R.layout.simple_list_item_1,wifis);
            list.setAdapter(adapter);
            showDialog();
        }
    }

    private void showDialog() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                itemSelect = id;
                builder.setTitle("Wi-Fi status");
                builder.setMessage("Do you want to connect?");
                builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String item = Long.toString(itemSelect);
                        connecting(itemSelect);
                        //Toast.makeText(getApplicationContext(), item + "Connected", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Disconnect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Not Connect", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });
    }

    private void connecting(long itemSelect) {
        List<ScanResult> wifiScanList = wifi.getScanResults();
        WifiConfiguration conf = new WifiConfiguration();
        networkSSID = ((wifiScanList.get((int) itemSelect).SSID).toString());
        conf.SSID = "\"" + networkSSID + "\"";

        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        //conf.preSharedKey = "\""+ networkPass +"\"";
        wifi.addNetwork(conf);

        List<WifiConfiguration> list1 = wifi.getConfiguredNetworks();

        for (WifiConfiguration i : list1) {
            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                Toast.makeText(getApplicationContext(), networkSSID + " Connected", Toast.LENGTH_SHORT).show();
                wifi.disconnect();
                wifi.enableNetwork(i.networkId, true);
                wifi.reconnect();
                break;
            }
        }
    }

}
