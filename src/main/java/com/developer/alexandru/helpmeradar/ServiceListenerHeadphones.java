package com.developer.alexandru.helpmeradar;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Alexandru on 8/18/2014.
 */
public class ServiceListenerHeadphones extends Service {

    private HeadphonesActionReceiver receiver;
    private         ComponentName eventReceiver;
    @Override
    public void onCreate() {
        receiver = new HeadphonesActionReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.setPriority(Integer.MAX_VALUE);

        registerReceiver(receiver, intentFilter);

        eventReceiver = new ComponentName(this, HeadphonesActionReceiver.class);
        ((AudioManager)getSystemService(AUDIO_SERVICE)).registerMediaButtonEventReceiver(eventReceiver);

        Log.i(getClass().getCanonicalName().toUpperCase(), "SERVICE CREATED");
    }

    public ServiceListenerHeadphones() {
        super();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(getClass().getCanonicalName().toUpperCase(), "SERVICE STARTED");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(receiver);
            Log.i(getClass().getCanonicalName().toUpperCase(), "SERVICE STOPPED");

        }catch (NullPointerException e){
            ;
        }
        try {
            ((AudioManager) getSystemService(AUDIO_SERVICE)).unregisterMediaButtonEventReceiver(eventReceiver);
        }catch (Exception e){

        }
        super.onDestroy();
    }
}
