package com.developer.alexandru.helpmeradar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;


import java.util.ArrayList;

public class ContactsActivity extends ListActivity {
    public static final String CONTACTS_PREF_FILE = "contacts";
    private static final int PICK_CONTACT = 12345;
    public static final String SPLITTER = "splitterNameNumber";

    public static final String TAG = "CONTACTS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        this.setListAdapter(new MyListViewAdapter(this, Actions.retrieveContacts(this)));

        findViewById(R.id.btn_type_new_contact).setOnClickListener(new ClickListenerNewContact(this));
        findViewById(R.id.btn_type_new_email).setOnClickListener(new ClickListenerNewEmail(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.add_contact:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            //Uri to the selected contact
            Uri contact = data.getData();
            ContentResolver contentResolver = getContentResolver();
            Cursor c = managedQuery(contact, null, null, null, null);

            while (c.moveToNext()) {
                // Id and name for the selected contact
                String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    // Get phone number
                    Cursor pCur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);

                    while (pCur.moveToNext()) {
                        String phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        getSharedPreferences(CONTACTS_PREF_FILE, MODE_PRIVATE).edit().putString("contact_" + getListAdapter().getCount(),
                                name + SPLITTER + phoneNumber).commit();

                        ((MyListViewAdapter) getListAdapter()).notifyDataSetChanged();
                        break;
                    }
                }

                // get email and type
                Cursor emailCur = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id},
                        null);

                // Add emails to the contacts list
                while (emailCur.moveToNext()) {
                    // This would allow you get several email addresses
                    // if the email addresses were stored in an array
                    String email = emailCur.getString(
                            emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    String emailType = emailCur.getString(
                            emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

                    getSharedPreferences(CONTACTS_PREF_FILE, MODE_PRIVATE).edit().putString("contact_" + getListAdapter().getCount(),
                            name + SPLITTER + email).commit();

                    ((MyListViewAdapter) getListAdapter()).notifyDataSetChanged();
                    break;
                }
                emailCur.close();
            }
        }
    }

    /**
     * Click listener for the add new contact button
     */
    private class ClickListenerNewContact implements View.OnClickListener{

        private Dialog dialog;
        private AlertDialog.Builder builder;
        View dialogView;

        public ClickListenerNewContact(Context context){
            builder = new AlertDialog.Builder(context);

            dialogView = View.inflate(context, R.layout.type_new_contact, null);
            builder.setCancelable(true)
                    .setView(dialogView)
                    .setNegativeButton(getResources().getString(R.string.negative_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton(getResources().getString(R.string.positive_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = ((EditText)dialogView.findViewById(R.id.new_name)).getText().toString();
                            String phoneNumber = ((EditText)dialogView.findViewById(R.id.new_phone_number)).getText().toString();
                            //Input data validation
                            if(name != null && phoneNumber != null){
                                getSharedPreferences(CONTACTS_PREF_FILE, MODE_PRIVATE).edit().putString("contact_" + getListAdapter().getCount(),
                                        name + SPLITTER + phoneNumber).commit();
                                ((MyListViewAdapter)getListAdapter()).notifyDataSetChanged();
                                dialog.cancel();
                            }
                        }
                    });
            dialog = builder.create();
        }

        @Override
        public void onClick(View v) {
            dialog.show();
        }
    }

    private class ClickListenerNewEmail implements View.OnClickListener{

        private Dialog dialog;
        private AlertDialog.Builder builder;
        View dialogView;

        public ClickListenerNewEmail(Context context){
            builder = new AlertDialog.Builder(context);

            dialogView = View.inflate(context, R.layout.type_new_email, null);
            builder.setCancelable(true)
                    .setView(dialogView)
                    .setNegativeButton(getResources().getString(R.string.negative_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton(getResources().getString(R.string.positive_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = ((EditText)dialogView.findViewById(R.id.new_name)).getText().toString();
                            String email = ((EditText)dialogView.findViewById(R.id.new_email)).getText().toString();

                            //Input data validation
                            if(name != null && email != null){
                                getSharedPreferences(CONTACTS_PREF_FILE, MODE_PRIVATE).edit().putString("contact_" + getListAdapter().getCount(),
                                        name + SPLITTER + email).commit();
                                ((MyListViewAdapter)getListAdapter()).notifyDataSetChanged();
                                dialog.cancel();
                            }
                        }
                    });
            dialog = builder.create();
        }

        @Override
        public void onClick(View v) {
            dialog.show();
        }
    }

    private class MyListViewAdapter extends BaseAdapter{

        private ArrayList<Contact> contacts;
        private Context context;

        public MyListViewAdapter(Context context, ArrayList<Contact> values) {
            this.contacts = values;
            this.context = context;

        }

        @Override
        public int getCount() {
            return contacts.size();
        }

        @Override
        public Object getItem(int position) {
            return contacts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void notifyDataSetChanged() {
            contacts.clear();
            contacts.addAll(Actions.retrieveContacts(context));
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.contact, parent, false);
            }

            Contact contact = contacts.get(position);

            TextView name = (TextView)convertView.findViewById(R.id.name);
            TextView address = (TextView)convertView.findViewById(R.id.address);

            name.setText(contact.name);
            address.setText(contact.address);

            final int positionInAdapter = position;

            convertView.findViewById(R.id.contact_layout).setOnLongClickListener(new View.OnLongClickListener() {


                @Override
                public boolean onLongClick(View v) {
                    Actions.removeContact(getApplicationContext(), positionInAdapter);
                    ((MyListViewAdapter)getListAdapter()).notifyDataSetChanged();
                    return true;
                }
            });

            return convertView;
        }
    }

    public static class Contact{
        public String name;
        public String address;
    }

}
