package com.developer.alexandru.helpmeradar;

import com.developer.alexandru.helpmeradar.util.SystemUiHider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Alexandru
 */
public class AlertActivity extends Activity {
    private final static String TAG ="ALERT ACTIVITY";

    //Handler reposting runnables until touch
    private Runnable sound, blink;
    private Handler soundHandler, blinkingHandler;

    private Vibrator vibrator;

    private Ringtone ringtone;
    String ringtoneName;

    LinearLayout alertLayout;
    TextView message;
    TextView link;
    String receivedText;
    //Blinking frequency
    private static final int DELAY = 200;

    AudioManager audioManager;
    int userVolume;
    int userRingerMode;
    Geocoder geocoder;
    Thread getLocationName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        Log.d(TAG, "create");

        //Flags/wake lock to keep screen on/turn screen on/unlock screen
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        //When message is clicked stop all animations
        message = (TextView)findViewById(R.id.fullscreen_content);
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ringtone != null)
                    ringtone.stop();
                soundHandler.removeCallbacks(sound);
                blinkingHandler.removeCallbacks(blink);
                alertLayout.setBackgroundColor(getResources().getColor(android.R.color.white));

                if(vibrator != null && vibrator.hasVibrator())
                    vibrator.cancel();

            }
        });
        //When link is clicked stop activity
        link = (TextView)findViewById(R.id.link);
        link.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    stopRingBlinkVibrate();
                    finish();
                    return true;
                }
                return false;
            }
        });

    }

    /*Do all tasks like retrieve message and maps link in here, because the intent starting this activity
    * might actually be a new intent with onNewIntent() (this activity is singleInstance), therefore
    * not always the onCreate() is called
    */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "resume");

        alertLayout = (LinearLayout)findViewById(R.id.alert_layout);

        if(message == null)
            message = (TextView)findViewById(R.id.fullscreen_content);
        if(link == null)
            link = (TextView)findViewById(R.id.link);

        //Received text.Remove header if needed.
        //Split the text into the actual message and the link
        receivedText = getIntent().getStringExtra("message");
        if(receivedText.startsWith(MainActivity.MESSAGE_FROM_PANIC_RADAR))
            receivedText = receivedText.substring(MainActivity.MESSAGE_FROM_PANIC_RADAR.length() + 1);

        //Set text to "message" text view and form a link for "link" text view
        String[] textAndLink = receivedText.split(Actions.HEADER_LINK_GOOGLE_MAPS);
        try{
            message.setText(textAndLink[0]);
            link.setText(String.format("%s%s", Actions.HEADER_LINK_GOOGLE_MAPS, textAndLink[1]));
            //Try to convert coords into location name.Could fail if
            // no network connection
            if(getLocationName == null)
                getLocationName = new Thread(new RunnableGetLocation(this, textAndLink[0], textAndLink[1]));
            //Do not start the thread twice and display same message twice.
            if(!getLocationName.isAlive())
                try {
                    getLocationName.start();
                }catch (IllegalThreadStateException e){
                    getLocationName = null;
                    getLocationName = new Thread(new RunnableGetLocation(this, textAndLink[0], textAndLink[1]));
                    getLocationName.start();
                }
        }catch (IndexOutOfBoundsException e){
        }

        //Adjust volume to max, but retain current user's volume
        if(audioManager == null)
            audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        userVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        userRingerMode = audioManager.getRingerMode();

        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                AudioManager.FLAG_VIBRATE);

        ringtoneName = getRingtoneName();

        //Runnables and handlers for ringing and changing colors
        if(sound == null)
            sound = new Sound(ringtoneName);
        if(soundHandler == null)
            soundHandler = new Handler();
        soundHandler.post(sound);
        if(blink == null)
            blink = new BlinkingRunnable();
        if(blinkingHandler == null)
            blinkingHandler = new Handler();
        blinkingHandler.post(blink);

        //Vibrator
        if(getSharedPreferences(SettingsActivity.PREF_FILE, MODE_PRIVATE).getBoolean(
                SettingsActivity.PREF_NAME_ALLOW_VIBRATE, true)){
            if(vibrator == null)
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if(vibrator.hasVibrator())
                vibrator.vibrate(new long[]{400, 1350}, 0);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "pause");
        stopRingBlinkVibrate();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "new intent");

        //A new alarm so stop the current one
        stopRingBlinkVibrate();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "stop");
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, AudioManager.FLAG_VIBRATE);
        audioManager.setRingerMode(userRingerMode);

        stopRingBlinkVibrate();

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "destroyed");

        stopRingBlinkVibrate();

        super.onDestroy();
    }

    private String getRingtoneName(){
        return  getSharedPreferences(SettingsActivity.PREF_FILE, MODE_PRIVATE).getString(
                SettingsActivity.PREF_NAME_DEFAULT_RINGTONE, "none");
    }

    private void stopRingBlinkVibrate(){
        try{
            vibrator.cancel();
        }catch (Exception e){
        }

        try {
            soundHandler.removeCallbacks(sound);
        }catch (Exception e){
        }
        try {
            blinkingHandler.removeCallbacks(blink);
        }catch (Exception e){
        }
        try {
            ringtone.stop();
        }catch (Exception e){
        }
    }

    //Runnable posted infinit on handler to play ringtone
    private class Sound implements Runnable{
        public Sound(String alertName) {
            super();
            final Uri uri;
            if(alertName.equals("none"))
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
            else
                uri = Uri.parse(alertName);

            ringtone = RingtoneManager.getRingtone(getApplicationContext(),uri);
            if(ringtone != null)
                ringtone.setStreamType(AudioManager.STREAM_ALARM);
        }

        @Override
        public void run() {
            try {
                ringtone.play();
            }catch (Exception e){

            }
            soundHandler.post(this);
        }


    }

    //Runnable posted infinit on handler to play change colors
    private class BlinkingRunnable implements Runnable{

        private int previousColor;

        public BlinkingRunnable(){
            //white
            previousColor = 0;
        }

        @Override
        public void run() {
            if(previousColor == 0) {
                alertLayout.setBackgroundColor(getResources().getColor(R.color.red));
                //red
                previousColor =1 ;

            }else{
                alertLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
                //white
                previousColor = 0 ;
            }

            blinkingHandler.postDelayed(this, DELAY);
        }
    }

    //Runnable to be runned in a background thread to retrieve human readable location
    private class RunnableGetLocation implements Runnable{

        private Context mContext;
        private double lat, lng;
        private String receivedText;
        public RunnableGetLocation(Context context,String receivedText, String receivedLink){
            this.mContext = context;
            this.receivedText = receivedText;
            try {
                String[] latLng = receivedLink.split(",");
                lat = Double.valueOf(latLng[0]);
                lng = Double.valueOf(latLng[1]);
            }catch (ArrayIndexOutOfBoundsException e){
            }
        }

        @Override
        public void run() {
            if(geocoder == null)
                geocoder = new Geocoder(mContext);
            try {
                final ArrayList<Address> addresses = (ArrayList<Address>) geocoder.getFromLocation(lat, lng, 1);
                final String address = addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getAddressLine(1);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        message.setText(receivedText + " " + address);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
