package com.developer.alexandru.helpmeradar;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Alexandru on 8/18/2014.
 * Service running in the background containing the MEDIA_BUTTON receiver
 * The receiver must be registered this way, because in the manifest is immediately disabled
 */
public class ServiceListenerHeadphones extends Service {

    //Receiver for MEDIA_BUTTON_ACTION
    private ComponentName eventReceiver;

    @Override
    public void onCreate() {
        //Register the receiver for MEDIA_BUTTON to be the sole receiver
        eventReceiver = new ComponentName(this, HeadphonesActionReceiver.class);
        ((AudioManager)getSystemService(AUDIO_SERVICE)).registerMediaButtonEventReceiver(eventReceiver);

        //Register SMS sending status receiver
        IntentFilter smsResultFilter = new IntentFilter(Actions.SMS_SEND);
        registerReceiver(smsResultReceiver, smsResultFilter);

        Log.i(getClass().getCanonicalName().toUpperCase(), "SERVICE CREATED");
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
            Log.i(getClass().getCanonicalName().toUpperCase(), "SERVICE STOPPED");
        }catch (NullPointerException e){
            //;
        }
        try {
            ((AudioManager) getSystemService(AUDIO_SERVICE)).unregisterMediaButtonEventReceiver(eventReceiver);
        }catch (Exception e){
            //
        }

        try {
            unregisterReceiver(smsResultReceiver);
        }catch (Exception e){
            //
        }

        super.onDestroy();
    }

    private final BroadcastReceiver smsResultReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()){
                case Activity.RESULT_OK:
                    Toast.makeText(context, context.getResources().getString(R.string.sms_sent),
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, context.getResources().getString(R.string.generic_failure),
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, context.getResources().getString(R.string.no_service),
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, context.getResources().getString(R.string.null_pdu),
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, context.getResources().getString(R.string.radio_off),
                            Toast.LENGTH_SHORT).show();
                    break;

            }
        }

    };

}
