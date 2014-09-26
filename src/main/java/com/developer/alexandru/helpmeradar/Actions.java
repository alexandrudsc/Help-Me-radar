package com.developer.alexandru.helpmeradar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Patterns;

import java.util.ArrayList;


/**
 * Created by Alexandru on 8/19/2014.
 * Actions to be performed, like sending SMS-es, add/remove contacts.
 */
public class Actions {

    public static final String SMS_SEND = "send_sms";
    private static SmsManager smsManager;
    private static Mail m;
    //Header for link google maps
    public static final String HEADER_LINK_GOOGLE_MAPS = "http://www.google.com/m/maps?q=";
    public static final String HEADER_LINK_GOOGLE_SEARCH = "https://www.google.com/search?q=";

    /**
     * Send alerts on all enabled ways (SMS, email)
     * @param context application context
     */
    public static void sendGeneralAlerts(Context context){
        sendSMSToAll(context);
        sendEmailsToAll(context);
    }

    /**
     * Send SMS to all chosen contacts, if sending SMS is enabled
     * Do all work off the main thread
     * @param context application context
     */
    public static void sendSMSToAll(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //Sending SMS is enabled
        Log.d("ACTIONS", "sms sent to all");
        if(prefs.getBoolean(SettingsActivity.PREF_NAME_SEND_SMS, true)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<ContactsActivity.Contact> contacts = retrieveContacts(context);
                    int n = contacts.size();


                    String name = prefs.getString(SettingsActivity.PREF_NAME_USER_NAME, context.getResources().getString(
                            R.string.pref_default_name));
                    String address;
                    String messageTemplate = prefs.getString(SettingsActivity.PREF_NAME_DEFAULT_TEMPLATE, context.getResources().getString(
                            R.string.pref_default_message));

                    String location = prefs.getString(SettingsActivity.PREF_NAME_LAST_GPS_COORDS, "Lat 0, Lng 0");

                    String body = MainActivity.MESSAGE_FROM_PANIC_RADAR + ": " + name + ": " + messageTemplate + " " +
                            formGoogleMapsLink(location);

                    for (int i = 0; i < n; i++) {
                        address = contacts.get(i).address;
                        if (!isEmailAddress(address))
                            sendSMS(context, address, body);
                    }
                }
            }).start();
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

    /**
     * Send emails to all chosen contacts, if sending emails is enabled
     * Do all work off the main thread
     * @param context application context
     */
    public static void sendEmailsToAll(final Context context){
        final  SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //Sending emails is enabled
        if(prefs.getBoolean(SettingsActivity.PREF_NAME_SEND_EMAILS, false)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<ContactsActivity.Contact> contacts = retrieveContacts(context);
                    int n = contacts.size();

                    String name = prefs.getString(SettingsActivity.PREF_NAME_USER_NAME, context.getResources().getString(
                            R.string.pref_default_name));
                    String address;
                    String messageTemplate = prefs.getString(SettingsActivity.PREF_NAME_DEFAULT_TEMPLATE, context.getResources().getString(
                            R.string.pref_default_message));

                    String location = prefs.getString(SettingsActivity.PREF_NAME_LAST_GPS_COORDS, "Lat 0, Lng 0");

                    String body = MainActivity.MESSAGE_FROM_PANIC_RADAR + ": " + name + ": " + messageTemplate + " " +
                            formGoogleMapsLink(location);

                    for (int i = 0; i < n; i++) {
                        address = contacts.get(i).address;
                        if (isEmailAddress(address))
                            sendEmail(context, address, body);

                    }
                }
            }).start();
        }
    }

    /**
     * Send email to individual contact
     * @param context application context
     * @param address valid email address
     * @param body    body of the email
     */
    private static void sendEmail(Context context, String address, String body){
        String[] toArr = {address};
        //new MailThread( toArr, body).start();
        if(m == null)
            m = new Mail("helpmeradar@gmail.com", "helpmeradar1");
        m.setTo(toArr);
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

    /**
     * Retrieve all contacts from the preference file
     * @param context application context
     * @return array with Contact objects
     */
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

    /**
     * Create a google maps link with a marker on the location of the user
     * @param location location in lat - lng format (as in preference file)
     * @return link in string format
     */
    public static String formGoogleMapsLink(String location){
        String separatedCoords = "0,0";
        if (!location.equals("Lat 0, Lng 0")) {
            String[] coords = location.split(" ");
            separatedCoords = coords[0] + "%2c" + coords[1];
        }

        return HEADER_LINK_GOOGLE_MAPS + separatedCoords;
    }

    public static Double[] decodePartiolGoogleMapsLink(String link){
        Double[] latLng = new Double[]{0.0, 0.0};
        try {
            String[] coords = link.split( "%2c" );
            latLng[0] = Double.valueOf(coords[0]);
            latLng[1] = Double.valueOf(coords[1]);
        }catch (Exception e){
            return latLng;
        }
        //Add layer (zoom) at the end of the link for better support
        return latLng;
    }

    private static boolean isEmailAddress(String address){
        return Patterns.EMAIL_ADDRESS.matcher(address).matches();
    }
}
