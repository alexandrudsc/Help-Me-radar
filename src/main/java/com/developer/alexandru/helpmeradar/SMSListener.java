package com.developer.alexandru.helpmeradar;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Alexandru on 8/16/2014.
 */
public class SMSListener extends BroadcastReceiver {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public static final String SMS_DELIVRED = "android.provider.Telephony.SMS_DELIVER";
    private final String TAG = getClass().getCanonicalName().toUpperCase();

    public static final String SMS_URI = "content://sms";

    public static final String ADDRESS = "address";
    public static final String PERSON = "person";
    public static final String DATE = "date";
    public static final String READ = "read";
    public static final String STATUS = "status";
    public static final String TYPE = "type";
    public static final String BODY = "body";
    public static final String SEEN = "seen";

    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;

    public static final int MESSAGE_IS_NOT_READ = 0;
    public static final int MESSAGE_IS_READ = 1;

    public static final int MESSAGE_IS_NOT_SEEN = 0;
    public static final int MESSAGE_IS_SEEN = 1;

    private PowerManager.WakeLock wakeLock;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SMS_RECEIVED)){
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE, "ALERT RECEIVED");
            wakeLock.acquire();
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock keyguardLock =  keyguardManager.newKeyguardLock("TAG");
            keyguardLock.disableKeyguard();
            Log.i(TAG, "Intent received: " + intent.getAction());
            Bundle bundle = intent.getExtras();
            if (bundle != null)
                handleMessage(bundle, context);

        }
    }

    private void handleMessage(Bundle bundle, Context context) {
        //If user allows incoming messages then react if PANIC MESSAGE received
        if(context.getSharedPreferences(SettingsActivity.PREF_FILE, Context.MODE_PRIVATE).
                getBoolean(SettingsActivity.PREF_NAME_ALLOW_INCOMING_SMS, true)) {
            /*Intent forIntentService = new Intent(context, ServiceGetMessage.class);
            forIntentService.putExtras(intent.getExtras());
            IntentService intentService = new ServiceGetMessage(context);*/

            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                if (messages.length >= 0) {
                    ContentResolver contentResolver = context.getContentResolver();
                    String text = messages[0].getMessageBody();
                    if (text.startsWith(MainActivity.MESSAGE_FROM_PANIC_RADAR)) {
                        Log.d(TAG, "Message panic");
                        /*Intent intentStartApp = new Intent(context.getApplicationContext(), AlertActivity.class);
                        intentStartApp.putExtra("message", text);
                        intentStartApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentStartApp.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        */
                        Intent broadcastAlertIntent = new Intent(ReceiverInternalBroadcast.ACTION_ALERT);
                        broadcastAlertIntent.putExtra("message", text);

                        String body = String.copyValueOf(text.toCharArray(), MainActivity.MESSAGE_FROM_PANIC_RADAR.length() + 1,
                                text.length() - MainActivity.MESSAGE_FROM_PANIC_RADAR.length() - 1);
                        context.sendOrderedBroadcast(broadcastAlertIntent, null);
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            putSmsToDatabase(contentResolver, messages[0], body);
                            this.abortBroadcast();
                        }

                        //context.getApplicationContext().startActivity(intentStartApp);

                    } else {
                        //If the app is default messaging app
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
                            putSmsToDatabase(contentResolver, messages[0], text);
                    }
                }
            }
        }
        if (wakeLock.isHeld())
            wakeLock.release();
    }

    private void putSmsToDatabase( ContentResolver contentResolver, SmsMessage sms, String body ) {

        // Create SMS row
        ContentValues values = new ContentValues();
        values.put( ADDRESS, sms.getOriginatingAddress() );
        values.put( DATE, sms.getTimestampMillis() );
        values.put( READ, MESSAGE_IS_NOT_READ );
        values.put( STATUS, sms.getStatus() );
        values.put( TYPE, MESSAGE_TYPE_INBOX );
        values.put( SEEN, MESSAGE_IS_NOT_SEEN );
        values.put( BODY, body);

        // Push row into the SMS table
        contentResolver.insert( Uri.parse(SMS_URI), values );
    }

}
