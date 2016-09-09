package com.kresshy.weatherstation.connection;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class BluetoothDiscoveryReceiver extends BroadcastReceiver {

    private static BluetoothDiscoveryReceiver instance = null;
    private ArrayAdapter<String> bluetoothDevices;
    private ActionBarActivity activity;

    protected BluetoothDiscoveryReceiver(ArrayAdapter<String> bluetoothDevices, ActionBarActivity activity) {
        this.bluetoothDevices = bluetoothDevices;
        this.activity = activity;
    }

    public static BluetoothDiscoveryReceiver getInstance(ArrayAdapter<String> bluetoothDevices, ActionBarActivity activity) {
        if (instance == null) {
            return new BluetoothDiscoveryReceiver(bluetoothDevices, activity);
        } else {
            return instance;
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // If it's already paired, skip it, because it's been listed
            // already
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                bluetoothDevices.add(device.getName() + "\n" + device.getAddress());
            }
            // When discovery is finished, change the Activity fragmentTitle
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            activity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
            activity.setTitle("Select Device");
            if (bluetoothDevices.getCount() == 0) {
                String noDevices = "None found";
                bluetoothDevices.add(noDevices);
                Toast.makeText(context, "No devices found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
