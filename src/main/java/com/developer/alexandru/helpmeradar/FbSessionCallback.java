package com.developer.alexandru.helpmeradar;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.Session;
import com.facebook.SessionState;

/**
 * Created by Alexandru on 9/20/2014.
 */
public class FbSessionCallback implements Session.StatusCallback {

    public static final String TAG = "FbSessionCallback";
    private MainActivity activity;
    private SharedPreferences prefs;

    public FbSessionCallback(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void call(Session session, SessionState state, Exception exception) {
        try {

        if(state.isOpened())
            activity.findViewById(R.id.facebook_login).setBackgroundResource(R.drawable.facebook_login_yes);
        else
            activity.findViewById(R.id.facebook_login).setBackgroundResource(R.drawable.facebook_login);
        if (activity.pendingPublishReauthorization && state == SessionState.OPENED_TOKEN_UPDATED) {
            Log.d(TAG, "permissions: " + session.getPermissions().toString());
            activity.pendingPublishReauthorization = false;

            if (prefs == null)
                prefs = PreferenceManager.getDefaultSharedPreferences(activity);

            //Name of the user
            String name = prefs.getString(SettingsActivity.PREF_NAME_USER_NAME, activity.getResources().getString(
                    R.string.pref_default_name));
            //Message template from settings
            String messageTemplate = prefs.getString(SettingsActivity.PREF_NAME_DEFAULT_TEMPLATE, activity.getResources().getString(
                    R.string.pref_default_message));
            //Form a google maps link
            String link = Actions.formGoogleMapsLink(
                    prefs.getString(SettingsActivity.PREF_NAME_LAST_GPS_COORDS, "Lat 0, Lng 0"));

            activity.publishStoryFacebook(activity.getResources().getString(R.string.app_name),
                    link,
                    messageTemplate);
            }
        }catch (Exception e){

        }
    }
}
