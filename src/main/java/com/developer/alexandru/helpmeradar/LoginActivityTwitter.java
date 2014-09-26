package com.developer.alexandru.helpmeradar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import twitter4j.auth.AccessToken;


/**
 * Created by Alexandru on 9/23/2014.
 */
public class LoginActivityTwitter extends Activity implements TwitterOAuthView.Listener {

    public static final String TAG = "LoginTwitterActivity";


    //Custom webView to handle Twitter API calls
    private TwitterOAuthView twitterOAuthView;
    private boolean oauthStarted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_twitter);
        twitterOAuthView = (TwitterOAuthView) findViewById(R.id.web_view);

        oauthStarted = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(oauthStarted)
            return;

        oauthStarted = true;
        // Start Twitter OAuth process. Its result will be notified via
        // TwitterOAuthView.Listener interface.
        twitterOAuthView.start(MainActivity.TWITTER_API_KEY,
                            MainActivity.TWITTER_API_SECRET,
                            MainActivity.TWITTER_CALLBACK_URL,
                            true,
                            this);
    }

    @Override
    public void onSuccess(TwitterOAuthView view, AccessToken accessToken) {
        showMessage("Success");
        new DecodeAccesToken(accessToken).start();
    }

    @Override
    public void onFailure(TwitterOAuthView view, TwitterOAuthView.Result result) {
        showMessage("Failure");
        setResult(RESULT_CANCELED);
        finish();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private class DecodeAccesToken extends Thread{

        private AccessToken accessToken;

        public DecodeAccesToken(AccessToken token) {
            this.accessToken = token;
        }

        @Override
        public void run() {
            Intent data = new Intent();
            String token = accessToken.getToken();
            String secretToken = accessToken.getTokenSecret();
            String name = accessToken.getScreenName();
            data.putExtra(MainActivity.PREF_KEY_OAUTH_TOKEN, token);
            data.putExtra(MainActivity.PREF_KEY_OAUTH_SECRET, secretToken);
            data.putExtra(MainActivity.PREF_KEY_TWITTER_NAME, name);
            setResult(RESULT_OK, data);
            finish();
        }
    }

}
