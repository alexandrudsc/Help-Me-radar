package com.developer.alexandru.helpmeradar;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by Alexandru on 8/24/2014.
 */
public class ReceiverInternalBroadcast extends BroadcastReceiver {
    public static final String ACTION_ALERT = "com.developer.alexandru.helpmeradar.ACTION_START_ALERT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION_ALERT)){
            Log.d("BROADCAST", "received");
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                                                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                                        PowerManager.ON_AFTER_RELEASE, "ALERT RECEIVED");
            wakeLock.acquire();
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock keyguardLock =  keyguardManager.newKeyguardLock("TAG");
            keyguardLock.disableKeyguard();
            Intent alertIntent = new Intent(context, AlertActivity.class);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            alertIntent.putExtra("message", intent.getStringExtra("message"));

            context.startActivity(alertIntent);
            this.abortBroadcast();
            wakeLock.release();
        }
    }
}
