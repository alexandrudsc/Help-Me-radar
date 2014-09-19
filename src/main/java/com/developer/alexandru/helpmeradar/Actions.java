package com.developer.alexandru.helpmeradar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Patterns;

import java.util.ArrayList;


/**
 * Created by Alexandru on 8/19/2014.
 * Actions to be performed, like sending SMS-es, bluetooth messages, add/remove contacts.
 */
public class Actions {

    public static final String SMS_SEND = "send_sms";
    private static SmsManager smsManager;

    //Header for link google maps
    public static final String HEADER_LINK_GOOGLE_MAPS = " http://www.google.com/m/maps?q=";


    public static void sendSMSToAll(Context context) {
        ArrayList<ContactsActivity.Contact> contacts = retrieveContacts(context);
        int n = contacts.size();
        SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREF_FILE, Context.MODE_PRIVATE);

        String name = prefs.getString(SettingsActivity.PREF_NAME_USER_NAME, context.getResources().getString(
                R.string.pref_default_name));
        String address;
        String messageTemplate = prefs.getString(SettingsActivity.PREF_NAME_DEFAULT_TEMPLATE, context.getResources().getString(
                R.string.pref_default_message));

        String location = prefs.getString(SettingsActivity.PREF_NAME_LAST_GPS_COORDS, "Lat 0, Lng 0");

        String body = MainActivity.MESSAGE_FROM_PANIC_RADAR + ": " + name + " " + messageTemplate + " " +
                formGoogleMapsLink(location);

        for (int i = 0; i < n; i++) {
            address = contacts.get(i).address;
            if(isEmailAdress(address))
                sendEmail(context, address, body);
            else
                sendSMS(context, address, body);
        }
    }

    private static void sendSMS(Context context, String phoneNumber, String body) {
        if (smsManager == null)
            smsManager = SmsManager.getDefault();

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SEND), 0);

        if (phoneNumber.length() > 0 && body.length() > 0) {
            smsManager.sendTextMessage(phoneNumber, null, body, sentPendingIntent, null);
        }
    }

    private static void sendEmail(Context context, String address, String body){
        Intent sendEmail = new Intent(Intent.ACTION_SEND);
        sendEmail.setType("plain/text");
        sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        sendEmail.putExtra(Intent.EXTRA_TEXT, body);
        sendEmail.putExtra(Intent.EXTRA_SUBJECT, MainActivity.MESSAGE_FROM_PANIC_RADAR);

        sendEmail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //context.startActivity(sendEmail);

        Mail m = new Mail("helpmeradar@gmail.com", "helpmeradar1");

        String[] toArr = {address};

        new MailThread(m, toArr, body).start();

    }

    public static ArrayList<ContactsActivity.Contact> retrieveContacts(Context context) {
        SharedPreferences contactsPref = context.getSharedPreferences(ContactsActivity.CONTACTS_PREF_FILE, Context.MODE_PRIVATE);
        ArrayList<ContactsActivity.Contact> contacts = new ArrayList<ContactsActivity.Contact>();

        ContactsActivity.Contact contact;
        String currentValue;
        int i = 0;
        while (!((currentValue = contactsPref.getString("contact_" + i, "-1")).equals("-1"))) {
            contact = new ContactsActivity.Contact();
            String[] data = currentValue.split(ContactsActivity.SPLITTER);
            try {
                contact.name = data[0];
                contact.address = data[1];
                contacts.add(contact);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            i++;
        }

        return contacts;

    }

    public static void removeContact(Context context, int position) {
        SharedPreferences contactsPref = context.getSharedPreferences(ContactsActivity.CONTACTS_PREF_FILE, Context.MODE_PRIVATE);
        contactsPref.edit().remove("contact_" + position).apply();
        String currentValue;
        int i = position + 1;
        while (!((currentValue = contactsPref.getString("contact_" + i, "-1")).equals("-1"))) {
            contactsPref.edit().remove("contact_" + i).apply();
            contactsPref.edit().putString("contact_" + (i - 1), currentValue).apply();
            i++;
        }
    }

    public static String formGoogleMapsLink(String location){
        String separatedCoords = "0,0";
        if (!location.equals("Lat 0, Lng 0")) {
            String[] coords = location.split(" ");
            separatedCoords = coords[0] + "%2c" + coords[1];
        }
        //Add layer (zoom) at the end of the link for better support
        return HEADER_LINK_GOOGLE_MAPS + separatedCoords;
    }

    private static boolean isEmailAdress(String address){
        return Patterns.EMAIL_ADDRESS.matcher(address).matches();
    }

    private static class MailThread extends Thread{

        private Mail m;
        private String[] to;
        private String body;

        public MailThread(Mail m, String[] to, String body) {
            super();
            this.m = m;
            this.to = to;
            this.body = body;
        }

        @Override
        public void run() {
            m.setTo(to);
            m.setFrom("helpmeradar@gmail.com");
            m.setSubject(MainActivity.MESSAGE_FROM_PANIC_RADAR);
            m.setBody(body);
            try {
                if (m.send()) {
                    Log.d("ACTIONS", "Email was sent successfully.");
                } else {
                    Log.d("ACTIONS", "Email was NOT SENT.");
                }
            }catch (Exception e){
                Log.e("ACTIONS", "Email was NOT SENT.Problem: " + e);
            }
        }
    }

}
