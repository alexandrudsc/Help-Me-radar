package com.developer.alexandru.helpmeradar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import java.util.ArrayList;


/**
 * Created by Alexandru on 8/19/2014.
 * Actions to be performed, like sending SMS-es, bluetooth messages, add/remove contacts.
 */
public class Actions {

    public static final String SMS_SEND = "send_sms";
    private static SmsManager smsManager;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


    //Header for link google maps
    public static final String HEADER_LINK_GOOGLE_MAPS = "https://www.google.com/maps/@";


    public static void sendSMSToAll(Context context) {
        ArrayList<ContactsActivity.Contact> contacts = retrieveContacts(context);
        int n = contacts.size();
        SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREF_FILE, Context.MODE_PRIVATE);

        String name = prefs.getString(SettingsActivity.PREF_NAME_USER_NAME, context.getResources().getString(
                R.string.pref_default_name));

        String messageTemplate = prefs.getString(SettingsActivity.PREF_NAME_DEFAULT_TEMPLATE,context.getResources().getString(
                R.string.pref_default_message));

        String location = prefs.getString(SettingsActivity.PREF_NAME_LAST_GPS_COORDS, "Lat 0, Lng 0");

        String body = MainActivity.MESSAGE_FROM_PANIC_RADAR + ": " + name + " " + messageTemplate + " " +
                formGoogleMapsLink(location);

        for (int i = 0; i < n; i++)
            sendSMS(context, contacts.get(i).phoneNumber, body);

    }

    private static void sendSMS(Context context, String phoneNumber, String body) {
        if (smsManager == null)
            smsManager = SmsManager.getDefault();

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SEND), 0);

        if (phoneNumber.length() > 0 && body.length() > 0) {
            smsManager.sendTextMessage(phoneNumber, null, body, sentPendingIntent, null);
        }
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
                contact.phoneNumber = data[1];
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
        contactsPref.edit().remove("contact_" + position).commit();
        String currentValue;
        int i = position + 1;
        while (!((currentValue = contactsPref.getString("contact_" + i, "-1")).equals("-1"))) {
            contactsPref.edit().remove("contact_" + i).commit();
            contactsPref.edit().putString("contact_" + (i - 1), currentValue).commit();
            i++;
        }
    }

    public static String formGoogleMapsLink(String location){
        String separatedCoords = "0,0";
        if (!location.equals("Lat 0, Lng 0")) {
            String[] coords = location.split(" ");
            separatedCoords = coords[0] + "," + coords[1];
        }
        //Add layer (zoom) at the end of the link for better support
        return HEADER_LINK_GOOGLE_MAPS + separatedCoords + ",14z";
    }

}
