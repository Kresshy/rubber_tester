package com.kresshy.rubbertester.connection;

import android.os.Handler;
import android.os.Parcelable;

import com.kresshy.rubbertester.utils.ConnectionState;


public interface Connection {
    void setHandler(Handler handler);

    void start();

    void connect(Parcelable device);

    void stop();

    void write(byte[] out);

    ConnectionState getState();
}
