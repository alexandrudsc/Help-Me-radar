package com.developer.alexandru.helpmeradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;


/**
 * Created by Alexandru on 8/18/2014.
 * Broadcast receiver for ACTION_MEDIA_BUTTON
 */
public class HeadphonesActionReceiver extends BroadcastReceiver {

    private final String TAG ="HeadphonesActionReceiver" ;
    private SharedPreferences prefs;

    //If the activity was not destroyed yet try to post on Facebook and Twitter
    private static MainActivity activity;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON))
            return;
        if (prefs == null)
            prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //If media button is not required, reject the incoming broadcast
        if(!prefs.getBoolean(SettingsActivity.HEADSET_LISTENER_PREF, false))
            return;

        Bundle extras = intent.getExtras();
        KeyEvent event = extras.getParcelable(Intent.EXTRA_KEY_EVENT) ;
        if (event != null ) {
            final int action = event.getAction();
            Log.d(TAG, event.toString());
            //action = 1 means ACTION_UP
            final int keyCode = event.getKeyCode();
            /*if ((keyCode == KeyEvent.KEYCODE_HEADSETHOOK && action == 1) ||            //For pressy button (used to hang up
                    (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE && action == 1) ||    //calls and play media)
                    (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT && action == 1) ||
                    (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD && action == 1)) {*/
            if (action == KeyEvent.ACTION_UP && !isInitialStickyBroadcast()){
                Actions.sendGeneralAlerts(context);

                if(activity != null) {

                    try {
                        String messageTemplate = prefs.getString(SettingsActivity.PREF_NAME_DEFAULT_TEMPLATE,
                                context.getResources().getString(R.string.pref_default_message));
                        //Form a google maps link
                        String link = Actions.formGoogleMapsLink(
                                prefs.getString(SettingsActivity.PREF_NAME_LAST_GPS_COORDS, "Lat 0, Lng 0"));

                        //Post a link on facebook
                        if (prefs.getBoolean(SettingsActivity.PREF_NAME_POST_FACEBOOK, false))
                            activity.publishStoryFacebook(context.getResources().getString(R.string.app_name),
                                    link,
                                    messageTemplate);
                        //Post on Twitter
                        if (prefs.getBoolean(SettingsActivity.PREF_NAME_POST_TWITTER, false))
                            activity.postTweet(messageTemplate + " " + link);
                    }catch (Exception e){
                        //Problems with the activity.This is stupid.Should not have been done here
                    }
                }

                abortBroadcast();
            }
        }
        else Log.d(TAG, "media extras null");
    }

    /**
     * There is a feature (stupid) to post on Facebook and Twitter using the headset buttons
     * In order to use this, the login sessions for these must be alive.
     * There are related to the lifecycle of the main activity
     */
    public static void setActivity(MainActivity act){
        activity = act;
    }
}
