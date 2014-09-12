package com.developer.alexandru.helpmeradar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Alexandru on 8/18/2014.
 * Used at nothing.Initially created for KitKat and above.Service containing a SMS broadcast receiver
 */
public class SmsListenerService extends Service {

    private static final int PRIORITY_MAX = 999;
    BroadcastReceiver receiver;


    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter(com.developer.alexandru.helpmeradar.SMSListener.SMS_RECEIVED);
        intentFilter.setPriority(PRIORITY_MAX);
        Log.i(getClass().getCanonicalName().toUpperCase(), "SERVICE CREATED");
        receiver = new SMSListener();
        registerReceiver(receiver, intentFilter);
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
        Log.i(getClass().getCanonicalName().toUpperCase(), "SERVICE STOPPED");
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
