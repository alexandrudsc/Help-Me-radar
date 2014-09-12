package com.developer.alexandru.helpmeradar;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Alexandru on 8/22/2014.
 */
public class BluetoothListenerService extends Service {

    public static final boolean D = true;

    @Override
    public void onCreate() {
        super.onCreate();
        if(D) Log.d("SERVICE", "create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(D) Log.d("SERVICE", "destroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
