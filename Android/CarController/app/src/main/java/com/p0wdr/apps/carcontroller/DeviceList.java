package com.p0wdr.apps.carcontroller;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.Set;
import java.util.ArrayList;

import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


public class DeviceList extends ActionBarActivity {

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    Button listDevicesButton;
    ListView deviceList;

    private AdapterView.OnItemClickListener deviceListItemClickHandler = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            BluetoothDevice selectedDevice = (BluetoothDevice)deviceList.getItemAtPosition(position);

            Intent intent = new Intent(DeviceList.this, ContollerActivity.class);
            intent.putExtra(BluetoothDevice.EXTRA_DEVICE, selectedDevice);
            startActivity(intent);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        listDevicesButton = (Button) findViewById(R.id.button);
        deviceList = (ListView) findViewById(R.id.listView);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (!myBluetooth.isEnabled()) {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);
            }
        }

        listDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });
    }

    private void pairedDevicesList() {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt);
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }
        final ArrayAdapter adapter = new BluetoothDeviceAdapter(this, list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(deviceListItemClickHandler);
    }

    public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {
        public BluetoothDeviceAdapter(Context context, ArrayList<BluetoothDevice> devices) {
            super(context, 0, devices);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            BluetoothDevice bluetoothDevice = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.bluetooth_device_item, parent, false);
            }
            TextView deviceName = (TextView) convertView.findViewById(R.id.device_name);

            deviceName.setText(bluetoothDevice.getName());
            return convertView;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
