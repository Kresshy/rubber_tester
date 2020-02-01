package com.kresshy.rubbertester.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import com.kresshy.rubbertester.R;

public class AlarmReceiver extends BroadcastReceiver {
    MediaPlayer mp;

    @Override
    public void onReceive(Context context, Intent intent) {
        mp= MediaPlayer.create(context, R.raw.alert);
        mp.start();
    }
}
