package com.kresshy.rubbertester.connection;

import android.app.Activity;
import android.os.Handler;

import com.kresshy.rubbertester.bluetooth.BluetoothConnection;
import com.kresshy.rubbertester.wifi.WifiConnection;

public class ConnectionFactory {

    public static Connection getConnection(Handler handler, Activity activity) {
        String connectionType = ConnectionManager.getConnectionType(activity);

        if (connectionType.equals("bluetooth")) {
            return BluetoothConnection.getInstance(handler, activity);
        } else {
            return WifiConnection.getInstance(handler, activity);
        }
    }
}
