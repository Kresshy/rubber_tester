package com.kresshy.rubbertester.activity;

import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
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
import com.kresshy.rubbertester.fragment.CalibrationFragment;
import com.kresshy.rubbertester.fragment.GraphViewFragment;
import com.kresshy.rubbertester.fragment.NavigationDrawerFragment;
import com.kresshy.rubbertester.fragment.SettingsFragment;
import com.kresshy.rubbertester.fragment.WifiFragment;
import com.kresshy.rubbertester.utils.ConnectionState;
import com.kresshy.rubbertester.weather.WeatherListener;
import com.kresshy.rubbertester.wifi.WifiDevice;

import java.util.ArrayList;
import java.util.Set;

import timber.log.Timber;


public class RTActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        BluetoothDeviceListFragment.OnFragmentInteractionListener,
        GraphViewFragment.OnFragmentInteractionListener,
        WifiFragment.OnFragmentInteractionListener,
        CalibrationFragment.OnFragmentInteractionListener {

    private static BluetoothDeviceItemAdapter bluetoothDevicesArrayAdapter;
    private static ArrayAdapter<String> wifiDevicesArrayAdapter;
    private static ArrayList<BluetoothDevice> bluetoothDevices;
    // required by bluetoothDeviceListFragment
    private static Set<BluetoothDevice> pairedDevices;

    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothDevice bluetoothDevice;

    private static double weatherDataCount = 0;
    private static String connectedDeviceName;
    private static SharedPreferences sharedPreferences;
    private CharSequence fragmentTitle;
    private static boolean requestedEnableBluetooth = false;

    private WeatherListener weatherListener;
    private NavigationDrawerFragment navigationDrawerFragment;
    private ConnectionManager connectionManager;

    private ForceListener forceListener;

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
                        .replace(R.id.container, new GraphViewFragment())
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
                        .replace(R.id.container, new GraphViewFragment())
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
                fragmentTitle = getString(R.string.bluetooth_weather_station_connect_view);
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
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragment())
                    .commit();

            return true;
        } else if (id == R.id.action_quit) {
            if (bluetoothAdapter.isEnabled()) {
                Timber.d("Disabling bluetooth adapter");
                bluetoothAdapter.disable();
            }

            finish();
        }

        return super.onOptionsItemSelected(item);
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

//                    double windSpeed = 0;
//                    double temperature = 0;
//
//                    WeatherData weatherData;
//                    WeatherMeasurement weatherMeasurement;

                    ForceMeasurement forceMeasurement;

                    try {
                        Gson gson = new Gson();
                        forceMeasurement = gson.fromJson(pdu, ForceMeasurement.class);
                        Timber.d(forceMeasurement.toString());
                        forceListener.measurementReceived(forceMeasurement);
                    } catch (JsonSyntaxException jse) {
                        Timber.d("Cannot parse measurement: " + pdu);
                    }

//                    try {
//                        Gson gson = new Gson();
//                        weatherMeasurement = gson.fromJson(pdu, WeatherMeasurement.class);
//                        Timber.d(weatherMeasurement.toString());
//                        weatherData = weatherMeasurement.getWeatherDataForNode(0);
//                        Timber.d(weatherData.toString());
//                        Timber.d("Transferring new weatherMeasurement / weatherData");
//                        weatherListener.weatherDataReceived(weatherData);
//                        weatherListener.measurementReceived(weatherMeasurement);
//                        break;
//                    } catch (JsonSyntaxException jse) {
//                        try {
//                            Timber.d("JsonSyntaxException, parsing as version 1 pdu");
//                            String[] weather = pdu.split(" ");
//                            windSpeed = Double.parseDouble(weather[0]);
//                            temperature = Double.parseDouble(weather[1]);
//                            weatherData = new WeatherData(windSpeed, temperature);
//                            Timber.d(weatherData.toString());
//                            weatherMeasurement = new WeatherMeasurement();
//                            weatherMeasurement.setVersion(1);
//                            weatherMeasurement.setNumberOfNodes(1);
//                            weatherMeasurement.addWeatherDataToMeasurement(weatherData);
//                            Timber.d(weatherMeasurement.toString());
//                            Timber.d("Transferring new weatherMeasurement / weatherData");
//                            weatherListener.weatherDataReceived(weatherData);
//                            weatherListener.measurementReceived(weatherMeasurement);
//                            break;
//                        } catch (NumberFormatException nfe) {
//                            Timber.d("Cannot parse weather data: " + pdu);
//                        }
//                    } catch (NumberFormatException nfe) {
//                        Timber.d("Cannot parse weather data: " + pdu);
//                    }

                    break;

                case RTConstants.MESSAGE_STATE:
                    ConnectionState state = (ConnectionState) msg.obj;

                    switch (state) {
                        case connecting:
                            Toast.makeText(getApplicationContext(), "Connecting to weather station", Toast.LENGTH_SHORT).show();
                            break;
                        case disconnected:
                            Toast.makeText(getApplicationContext(), "Disconnected from weather station", Toast.LENGTH_LONG).show();
                            break;
                    }

                    break;

                case RTConstants.MESSAGE_CONNECTED:
                    Toast.makeText(getApplicationContext(), "Connected to weather station", Toast.LENGTH_SHORT).show();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, new CalibrationFragment())
                            .commit();
                    break;
            }
        }
    };

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
    public void registerWeatherDataReceiver(WeatherListener weatherListener) {
        this.weatherListener = weatherListener;
    }

    @Override
    public void startDashboardAfterCalibration() {
        navigationDrawerFragment.selectItem(0);
    }

    @Override
    public void onDeviceSelectedToConnect(WifiDevice device) {
        connectionManager.connectToDevice(device);
    }
}