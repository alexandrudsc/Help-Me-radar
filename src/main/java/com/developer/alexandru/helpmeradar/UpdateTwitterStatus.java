package com.developer.alexandru.helpmeradar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Alexandru on 9/23/2014.
 */
public class UpdateTwitterStatus extends AsyncTask<String, String, String> {

    private final Context mContext;
    private final SharedPreferences prefs;

    public UpdateTwitterStatus(Context context) {
        this.mContext = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Before starting background thread Show Progress Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    /**
     * getting Places JSON
     * */
    protected String doInBackground(String... args) {
        Log.d("Tweet Text", "> " + args[0]);
        //Add time to the post to avoid "Duplicate tweet" error 403 when
        String time = Calendar.getInstance().getTime().toString();
        String status = args[0] + " " + time;
        try {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(MainActivity.TWITTER_API_KEY);
            builder.setOAuthConsumerSecret(MainActivity.TWITTER_API_SECRET);

            // Access Token
            String access_token = prefs.getString(MainActivity.PREF_KEY_OAUTH_TOKEN, "");
            // Access Token Secret
            String access_token_secret = prefs.getString(MainActivity.PREF_KEY_OAUTH_SECRET, "");

            AccessToken accessToken = new AccessToken(access_token, access_token_secret);
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

            // Update status
            twitter4j.Status response = twitter.updateStatus(status);

            Log.d("Status", "> " + response.getText());
        } catch (TwitterException e) {
            // Error in updating status
            Log.d("Twitter Update Error", e.getMessage());
        }
        return null;
    }

    /**
     * After completing background task Dismiss the progress dialog and show
     * the data in UI Always use runOnUiThread(new Runnable()) to update UI
     * from background thread, otherwise you will get error
     * **/
    protected void onPostExecute(String file_url) {
        Toast.makeText(mContext,
                "Status tweeted successfully", Toast.LENGTH_SHORT)
                .show();
    }
}
