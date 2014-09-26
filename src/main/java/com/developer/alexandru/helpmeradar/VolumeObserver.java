package com.developer.alexandru.helpmeradar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Alexandru on 9/24/2014.
 */
public class VolumeObserver extends ContentObserver {

    public static final String ACTION_VOLUME_CHANGED = "com.developer.alexandru.VOLUME_CHANGED";

    //debug
    private final String TAG = "VolumeObserver";
    private final boolean D = true;

    //Reference to the main activity for posting on Facebook and Twitter
    private static MainActivity activity;
    private Context mContext;
    private SharedPreferences prefs;

    //Last registered volume
    private int previousVolume;
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public VolumeObserver(Context context, Handler handler) {
        super(handler);
        this.mContext = context;
        final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        this.previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        final AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        final int currentVol = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
        final boolean wasChanged = (previousVolume - currentVol) != 0;

        if(wasChanged){

            if (D) Log.d(TAG, "volume changed with " + (previousVolume - currentVol));
            previousVolume = currentVol;
            Intent broadcastVolumeChanged = new Intent(ACTION_VOLUME_CHANGED);
            mContext.sendBroadcast(broadcastVolumeChanged);

            Actions.sendGeneralAlerts(mContext);

            if(activity != null){
                if (prefs == null)
                    prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                try {
                    String messageTemplate = prefs.getString(SettingsActivity.PREF_NAME_DEFAULT_TEMPLATE,
                            mContext.getResources().getString(R.string.pref_default_message));
                    //Form a google maps link
                    String link = Actions.formGoogleMapsLink(
                            prefs.getString(SettingsActivity.PREF_NAME_LAST_GPS_COORDS, "Lat 0, Lng 0"));

                    //Post a link on facebook
                    if (prefs.getBoolean(SettingsActivity.PREF_NAME_POST_FACEBOOK, false))
                        activity.publishStoryFacebook(mContext.getResources().getString(R.string.app_name),
                                link,
                                messageTemplate);
                    //Post on Twitter
                    if (prefs.getBoolean(SettingsActivity.PREF_NAME_POST_TWITTER, false))
                        activity.postTweet(messageTemplate + " " + link);
                }catch (Exception e){
                    //Problems with the activity.This is stupid.Should not have been done here
                }
            }

        }
    }

    /**
     * There is a feature (stupid) to post on Facebook and Twitter using the headset buttons
     * In order to use this, the login sessions for these must be alive.
     * There are related to the lifecycle of the main activity
     *
     * @param act the main activity reference
     */
    public static void setActivity(MainActivity act){
        activity = act;
    }

}
