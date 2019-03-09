package com.kresshy.rubbertester.activity;

import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kresshy.rubbertester.R;
import com.kresshy.rubbertester.application.RTConstants;
import com.kresshy.rubbertester.bluetooth.BluetoothConnection;
import com.kresshy.rubbertester.bluetooth.BluetoothDeviceItemAdapter;
import com.kresshy.rubbertester.bluetooth.BluetoothDiscoveryReceiver;
import com.kresshy.rubbertester.bluetooth.BluetoothStateReceiver;
import com.kresshy.rubbertester.connection.ConnectionManager;
import com.kresshy.rubbertester.force.ForceListener;
import com.kresshy.rubbertester.force.ForceMeasurement;
import com.kresshy.rubbertester.fragment.BluetoothDeviceListFragment;
import com.kresshy.rubbertester.fragment.ForceFragment;
import com.kresshy.rubbertester.fragment.NavigationDrawerFragment;
import com.kresshy.rubbertester.fragment.SettingsFragment;
import com.kresshy.rubbertester.fragment.WifiFragment;
import com.kresshy.rubbertester.serialization.LoadDialog;
import com.kresshy.rubbertester.serialization.SaveDialog;
import com.kresshy.rubbertester.serialization.SerializableMeasurementStorage;
import com.kresshy.rubbertester.utils.ConnectionState;
import com.kresshy.rubbertester.wifi.WifiDevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Set;

import timber.log.Timber;


public class RTActivity extends ActionBarActivity implements
        ForceFragment.OnFragmentInteractionListener,
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        BluetoothDeviceListFragment.OnFragmentInteractionListener,
        WifiFragment.OnFragmentInteractionListener {

    private static BluetoothDeviceItemAdapter bluetoothDevicesArrayAdapter;
    private static ArrayAdapter<String> wifiDevicesArrayAdapter;
    private static ArrayList<BluetoothDevice> bluetoothDevices;
    // required by bluetoothDeviceListFragment
    private static Set<BluetoothDevice> pairedDevices;

    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothDevice bluetoothDevice;

    private static String connectedDeviceName;
    private static SharedPreferences sharedPreferences;
    private CharSequence fragmentTitle;
    private static boolean requestedEnableBluetooth = false;

    private ForceListener forceListener;
    private NavigationDrawerFragment navigationDrawerFragment;
    private ConnectionManager connectionManager;

    private SerializableMeasurementStorage measurementStorage;

    private final int REQUEST_SAVE = 45;
    private final int REQUEST_LOAD = 46;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate()");

        // setting up view
        setContentView(R.layout.activity_main);

        // setting up navigation drawer fragment
        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        fragmentTitle = getTitle();

        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout)
        );

        // setting up bluetooth adapter, service and broadcast receivers
        bluetoothDevices = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothDevicesArrayAdapter = new BluetoothDeviceItemAdapter(this, bluetoothDevices);

        connectionManager = ConnectionManager
                .getInstance(this, bluetoothDevicesArrayAdapter, messageHandler);

        // setting up sharedpreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(connectionManager.sharedPreferenceChangeListener);

        registerReceiver(
                BluetoothStateReceiver.getInstance(
                        connectionManager.connection,
                        bluetoothDevicesArrayAdapter,
                        this,
                        sharedPreferences
                ),
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        );
        registerReceiver(
                BluetoothDiscoveryReceiver.getInstance(
                        bluetoothDevicesArrayAdapter,
                        this
                ),
                new IntentFilter(BluetoothDevice.ACTION_FOUND)
        );

        if (!requestedEnableBluetooth) {
            connectionManager.enableConnection();
            requestedEnableBluetooth = true;
        }

        measurementStorage = new SerializableMeasurementStorage();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("onStart()");

        // checking bluetoothadapter and bluetoothservice and make sure it is started
        if (!requestedEnableBluetooth) {
            connectionManager.enableConnection();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume()");

        // checking bluetoothadapter and bluetoothservice and make sure it is started
        if (!bluetoothAdapter.isEnabled() && !requestedEnableBluetooth) {
            requestedEnableBluetooth = true;
            connectionManager.enableConnection();
        } else if (bluetoothAdapter.isEnabled()) {

            switch (connectionManager.getConnectionState()) {
                case connected:
                    break;
                case connecting:
                    break;
                case disconnected:
                    connectionManager.startConnection();
                    break;
                case stopped:
                    connectionManager.startConnection();
                    break;
                default:
                    connectionManager.startConnection();
                    break;
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(connectionManager.sharedPreferenceChangeListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.d("onSaveInstanceState()");

        if (connectionManager.getConnectionState() == ConnectionState.connected) {
            Timber.d("Connected to a device");
            outState.putBoolean(getString(R.string.PREFERENCE_CONNECTED), true);
        } else {
            Timber.d("Not connected to a device");
            outState.putBoolean(getString(R.string.PREFERENCE_CONNECTED), false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("onPause()");
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(connectionManager.sharedPreferenceChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy()");

        connectionManager.stopConnection();
        BluetoothConnection.destroyInstance();

        try {
            unregisterReceiver(
                    BluetoothStateReceiver.getInstance(
                            connectionManager.connection,
                            bluetoothDevicesArrayAdapter,
                            this,
                            sharedPreferences
                    )
            );
        } catch (IllegalArgumentException ie) {
            Timber.d("BluetoothReceiver was not registered");
        }

        try {
            unregisterReceiver(
                    BluetoothDiscoveryReceiver.getInstance(
                            bluetoothDevicesArrayAdapter,
                            this
                    )
            );
        } catch (IllegalArgumentException ie) {
            Timber.d("bluetoothDiscoveryRegister was not registered");
        }

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(connectionManager.sharedPreferenceChangeListener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();

        switch (position) {
            case 0:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new ForceFragment())
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new BluetoothDeviceListFragment())
                        .commit();
                break;
            case 2:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new SettingsFragment())
                        .commit();
                break;
            case 3:
                if (bluetoothAdapter.isEnabled()) {
                    Timber.d("Disabling bluetooth adapter");
                    bluetoothAdapter.disable();
                }

                finish();
                break;
            case 4:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new WifiFragment())
                        .commit();
                break;
            default:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new ForceFragment())
                        .commit();
        }

        onSectionAttached(position);
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                fragmentTitle = getString(R.string.dashboard_view);
                break;
            case 1:
                fragmentTitle = getString(R.string.bluetooth_station_connect_view);
                break;
            case 2:
                fragmentTitle = getString(R.string.settings_view);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(fragmentTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                Intent intent = new Intent(getApplicationContext(),
                        SaveDialog.class);
                intent.putExtra("FileToSave", measurementStorage);
                startActivityForResult(intent, REQUEST_SAVE);

                return true;
            case R.id.action_load:
                Intent intent1 = new Intent(getApplicationContext(),
                        LoadDialog.class);
                startActivityForResult(intent1, REQUEST_LOAD);

                return true;
            case R.id.action_settings:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new SettingsFragment())
                        .commit();

                return true;
            case R.id.action_quit:
                if (bluetoothAdapter.isEnabled()) {
                    Timber.d("Disabling bluetooth adapter");
                    bluetoothAdapter.disable();
                }

                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED)
            return;

        switch (requestCode) {
            case REQUEST_SAVE:
                Toast.makeText(
                        getApplicationContext(),
                        "File saved! Name: "
                                + data.getStringExtra(getPackageName()) + ".rtm",
                        Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_LOAD:
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        + "/RubberTesterFiles";
                File dir = new File(path);
                dir.mkdir();

                try {

                    FileInputStream fis = new FileInputStream(path + "/"
                            + data.getStringExtra(getPackageName()));
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    SerializableMeasurementStorage loadedMeasurement = (SerializableMeasurementStorage) ois.readObject();
                    measurementStorage = loadedMeasurement;

                    loadMeasurementToGraph(measurementStorage);

                    Toast.makeText(
                            getApplicationContext(),
                            "Program: " + data.getStringExtra(getPackageName())
                                    + " opened.", Toast.LENGTH_SHORT).show();

                    ois.close();
                    fis.close();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
        }
    }

    private final Handler messageHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            String message = "";

            switch (msg.what) {

                case RTConstants.MESSAGE_TOAST:
                    message = (String) msg.obj;
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                    break;

                case RTConstants.MESSAGE_READ:
                    message = (String) msg.obj;

                    // [start_pdu_end]
                    String pdu = message.split("_")[1];
                    Timber.d("PDU of the message");
                    Timber.d(pdu);

                    ForceMeasurement forceMeasurement;

                    try {
                        Gson gson = new Gson();
                        forceMeasurement = gson.fromJson(pdu, ForceMeasurement.class);
                        Timber.d(forceMeasurement.toString());
                        measurementStorage.addMeasurement(forceMeasurement);
                        forceListener.measurementReceived(forceMeasurement);
                    } catch (JsonSyntaxException jse) {
                        Timber.d(jse.getMessage());
                        Timber.d("Cannot parse measurement: " + pdu);
                    }

                    break;

                case RTConstants.MESSAGE_STATE:
                    ConnectionState state = (ConnectionState) msg.obj;

                    switch (state) {
                        case connecting:
                            Toast.makeText(getApplicationContext(), "Connecting to station", Toast.LENGTH_SHORT).show();
                            break;
                        case disconnected:
                            Toast.makeText(getApplicationContext(), "Disconnected from station", Toast.LENGTH_LONG).show();
                            break;
                    }

                    break;

                case RTConstants.MESSAGE_CONNECTED:
                    Toast.makeText(getApplicationContext(), "Connected to station", Toast.LENGTH_SHORT).show();
                    onNavigationDrawerItemSelected(0);
                    break;
            }
        }
    };

    public void loadMeasurementToGraph(SerializableMeasurementStorage measurementStorage) {
        forceListener.enableMeasurementForLoad();

        for (ForceMeasurement measurement : measurementStorage.getMeasurementList()) {
            forceListener.measurementReceived(measurement);
        }

        forceListener.disableMeasurementForLoad();
    }

    public ArrayAdapter getPairedDevicesArrayAdapter() {
        return bluetoothDevicesArrayAdapter;
    }

    public static ArrayList<BluetoothDevice> getBluetoothDevices() {
        return bluetoothDevices;
    }

    public static Set<BluetoothDevice> getPairedDevices() {
        return pairedDevices;
    }

    @Override
    public void onDeviceSelectedToConnect(String address) {
        sharedPreferences.edit().putString(getString(R.string.PREFERENCE_DEVICE_ADDRESS), address).apply();

        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        Timber.d(bluetoothDevice.getName() + bluetoothDevice.getAddress());

        connectionManager.connectToDevice(bluetoothDevice);
    }

    @Override
    public void startBluetoothDiscovery() {
        Timber.d("Starting bluetooth discovery");
        bluetoothAdapter.startDiscovery();
    }

    @Override
    public void stopBluetoothDiscovery() {
        Timber.d("Stopping bluetooth discovery");
        bluetoothAdapter.cancelDiscovery();
    }


    @Override
    public void onDeviceSelectedToConnect(WifiDevice device) {
        connectionManager.connectToDevice(device);
    }

    @Override
    public void registerForceDataReceiver(ForceListener forceListener) {
        this.forceListener = forceListener;
    }
}
