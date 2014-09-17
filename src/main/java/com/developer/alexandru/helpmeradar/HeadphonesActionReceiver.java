package com.developer.alexandru.helpmeradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import java.security.Key;

/**
 * Created by Alexandru on 8/18/2014.
 */
public class HeadphonesActionReceiver extends BroadcastReceiver {

    private final String TAG ="HeadphonesActionReceiver" ;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON))
            return;
        Bundle extras = intent.getExtras();
        KeyEvent event = extras.getParcelable(Intent.EXTRA_KEY_EVENT) ;
        if (event != null) {

                //if(event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                //-1 no state, 1 - plugged, 0 - unplugged
                //int state = intent.getIntExtra("state", -1);

                //Headset type
                //String name = intent.getStringExtra("name");
                //Log.d("HEADPHONES", "state: " + state);
                //if(state == 1) {

                if (event.getAction() == KeyEvent.ACTION_UP && !isInitialStickyBroadcast()) {
                    Actions.sendSMSToAll(context);
                    Log.d(TAG, "pressed");
                }
            abortBroadcast();
            }
        else Log.d(TAG, "media extras null");

    }
}
