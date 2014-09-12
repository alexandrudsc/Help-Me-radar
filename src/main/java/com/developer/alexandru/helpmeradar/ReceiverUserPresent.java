package com.developer.alexandru.helpmeradar;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Alexandru on 9/1/2014.
 * Unused.Designed to start the service containing SMS receiver when screen off and stop it when screen on.
 * No need.Initially on KitKat SMS broadcast was not fired.
 */
public class ReceiverUserPresent extends BroadcastReceiver {

    Intent serviceIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(serviceIntent == null)
            serviceIntent = new Intent(context, SmsListenerService.class);
        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            context.stopService(serviceIntent);
            Log.d("USER PRESENT", "user present");
        }
        else
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                context.startService(serviceIntent);
                Log.d("USER PRESENT", "screen off");
            }

    }
}
