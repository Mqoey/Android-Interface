/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TwoLineListItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceActivity extends AppCompatActivity {

    public static final String EXTRA_ADDRESS = "bt_address",EXTRA_NAME="bt_name";

    private ListView mListView;
    private List<Pair<String, String>> mList;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle("Printer Setup");
        androidx.appcompat.app.ActionBar ab = getSupportActionBar();

        // Enable the Up button
//        ab.setDisplayHomeAsUpEnabled(true);

        setResult(RESULT_CANCELED);

        mList = new ArrayList<Pair<String,String>>();
        mListView = (ListView)findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                final Pair<String, String> data = mList.get(position);
                final String bthAddress = data.second;
                final String bthName = data.first;

                Intent resultData = new Intent();
                resultData.putExtra(EXTRA_ADDRESS, bthAddress);
                resultData.putExtra(EXTRA_NAME,bthName);
                setResult(RESULT_OK, resultData);
                finish();
            }
        });



        loadDevices();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void loadDevices() {
        final BluetoothAdapter bthAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bthAdapter == null) {
            return;
        }

        final ArrayAdapter<Pair<String, String>> arrayAdapter = new ArrayAdapter<Pair<String,String>>(this, android.R.layout.simple_list_item_2, mList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TwoLineListItem row;
                if(convertView == null){
                    LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row = (TwoLineListItem)inflater.inflate(android.R.layout.simple_list_item_2, null);
                } else{
                    row = (TwoLineListItem)convertView;
                }

                final Pair<String, String> data = mList.get(position);
                row.getText1().setText(data.first);
                row.getText2().setText(data.second);
                return row;
            }
        };
        mListView.setAdapter(arrayAdapter);

        final Set<BluetoothDevice> paired = bthAdapter.getBondedDevices();
        for (BluetoothDevice device: paired) {
            final Pair<String, String> data = new Pair<String, String>(device.getName(), device.getAddress());
            mList.add(data);
        }

        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {

            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
