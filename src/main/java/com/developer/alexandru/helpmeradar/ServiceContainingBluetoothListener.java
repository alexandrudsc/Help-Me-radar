package com.developer.alexandru.helpmeradar;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Alexandru on 8/24/2014.
 * Service waiting for broadcasts launched when bluetooth message is received
 */
public class ServiceContainingBluetoothListener extends Service{
    public static final String TAG = "ServiceContainingBluetoothListener";

    ReceiverInternalBroadcast receiverInternalBroadcast ;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SERVICE CREATED");
        receiverInternalBroadcast = new ReceiverInternalBroadcast();
        IntentFilter intentFilter = new IntentFilter(ReceiverInternalBroadcast.ACTION_ALERT);
        registerReceiver(receiverInternalBroadcast, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SERVICE STOPPED");
        try {
            unregisterReceiver(receiverInternalBroadcast);
        }catch (Exception e){
        }
    }
}
