package com.developer.alexandru.helpmeradar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    public final static String MESSAGE_FROM_PANIC_RADAR = "HELP ME RADAR";
    private final String TAG = "MAIN ACTIVITY";

    private static final int DISCOVERABLE_TIME = 700;

    //Codes for startActivityForResult()
    private final static int REQUEST_CODE_CHANGE_SETTINGS = 1235;
    private final static int REQUEST_TURN_ON_BLUETOOTH = 1236;
    private final static int REQUEST_DISCOVERABILITY = 1237;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String UNABLE_TO_CONNECT = "Unable to connect device";
    public static final String CONNECTION_LOST= "Device connection was lost";

    // Name of the connected device
    private String mConnectedDeviceName = null;

    //GPS helper
    private GPSTracker gpsTracker;

    private BluetoothService bluetoothService;
    private BluetoothAdapter adapter;
    private BluetoothDevice bluetoothDevice;
    private final static boolean SECURE_CONNECTION = true;

    //Receiver for MEDIA_BUTTON
    private ComponentName eventComponent;
    private AudioManager audioManager;

    private  Context mContext;
    private SharedPreferences prefs;

    //Dialog builder for alerts
    private AlertDialog.Builder builder;

    private Intent serviceContainingBluetoothListenerIntent;
    private IntentFilter intentFilterBluetoothState;

    //Buttons that can be moved around
    private View bluetoothButton;
    private View settingsButton;
    private View peopleButton;
    private View destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        //checkAndOpenGPS();
        gpsTracker = new GPSTracker(this);

        //Click listener for the main button.Send SMS and bluetooth message (if possible)
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenForSMSSendingEvents();
                Actions.sendSMSToAll(getApplicationContext());
                if(bluetoothService != null && bluetoothService.getState() == BluetoothService.STATE_CONNECTED){
                    if(prefs == null)
                        prefs = getSharedPreferences(SettingsActivity.PREF_FILE, MODE_PRIVATE);

                    String name = prefs.getString(SettingsActivity.PREF_NAME_USER_NAME, getResources().getString(
                            R.string.pref_default_name));
                    if(name.equals(getResources().getString(R.string.pref_default_name)))
                        name = adapter.getName();
                    String messageTemplate = prefs.getString(SettingsActivity.PREF_NAME_DEFAULT_TEMPLATE,getResources().getString(
                            R.string.pref_default_message));

                    String body = name + ": " + messageTemplate + " " + Actions.formGoogleMapsLink(
                            prefs.getString(SettingsActivity.PREF_NAME_LAST_GPS_COORDS, "Lat 0, Lng 0"));
                    bluetoothService.write(body.getBytes());
                }
            }
        });


        //Check if service with receiver for MediaButton is needed
        registerHeadsetReceiver();

        //Receiver watching for bluetooth adapter status
        registerBluetoothReceiver();


        //Drag listener for destination view.At first is invisible
        destination = findViewById(R.id.destination);
        destination.setOnDragListener(new DragListener(this));

        //Touch listener for menu buttons.Start drag when touch
        View.OnTouchListener touchListener = new DragListener.TouchEvent();

        bluetoothButton = findViewById(R.id.bluetooth);
        bluetoothButton.setOnTouchListener(touchListener);
        settingsButton = findViewById(R.id.settings);
        settingsButton.setOnTouchListener(touchListener);
        peopleButton = findViewById(R.id.people);
        peopleButton.setOnTouchListener(touchListener);

        /*Bluetooth class to configurate this device as "server" (thread listening to connections)
        * and to connect if case.
        */
        bluetoothService = new BluetoothService(this, mHandler);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 )
            adapter = BluetoothAdapter.getDefaultAdapter();
        else
            adapter = ((android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        //If the bluetooth is on, start the listening mode ("server" mode)
        if(adapter!= null && adapter.isEnabled())
            bluetoothService.start();

    }

    @Override
    public void onResume() {
        super.onResume();
        //Keep location as fresh as possible
        updateLocation();

        //If a menu button was moved, restore natural position
        restoreMenuButtons();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_CHANGE_SETTINGS:
                //If a service for media button actions is requested, start one
                registerHeadsetReceiver();
                //checkAndOpenGPS();
                //ifLocationAccepted(getSharedPreferences(SettingsActivity.PREF_FILE, MODE_PRIVATE));
                break;
            case REQUEST_TURN_ON_BLUETOOTH:
                //Bluetooth was turned on
                if(resultCode == RESULT_OK) {
                    if(adapter == null)
                        Toast.makeText(this, R.string.no_bluetooth, Toast.LENGTH_SHORT).show();
                    else
                        //Try to make it discoverable
                        ensureDiscoverable();
                    if(prefs == null)
                        prefs = getSharedPreferences(SettingsActivity.PREF_FILE, MODE_PRIVATE);
                    prefs.edit().putBoolean(SettingsActivity.PREF_NAME_ALLOW_BT, true).apply();
                }else {
                // Bluetooth was not turned on
                    setStatus(R.string.not_requested_bluetooth);
                    if (prefs == null)
                        prefs = getSharedPreferences(SettingsActivity.PREF_FILE, MODE_PRIVATE);
                    prefs.edit().putBoolean(SettingsActivity.PREF_NAME_ALLOW_BT, false).apply();
                }
                break;

            case REQUEST_DISCOVERABILITY:
                //The request for discoverability accepted
                if(resultCode == DISCOVERABLE_TIME){
                    //register receiver for bluetooth status
                    registerBluetoothReceiver();
                    if(adapter == null)
                        adapter = BluetoothAdapter.getDefaultAdapter();
                    if(adapter != null) {
                        if (prefs == null)
                            prefs = getSharedPreferences(SettingsActivity.PREF_FILE, MODE_PRIVATE);
                        prefs.edit().putBoolean(SettingsActivity.PREF_NAME_ALLOW_BT, true).apply();

                        //adapter.startDiscovery();
                        // Launch the DeviceListActivity to see devices and do scan
                        Intent serverIntent = new Intent(this, DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                    }
                }
                break;

            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    //Stop service as soon as requested.
    @Override
    public void onBackPressed() {
        if(bluetoothService != null)
            bluetoothService.stop();
        //Stop discovering
        if(adapter != null)
            adapter.cancelDiscovery();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Stop listening/connection for/to bluetooth devices
        if(bluetoothService != null)
            bluetoothService.stop();

        //Stop discovering
        if(adapter != null)
            adapter.cancelDiscovery();

        //Stop GPS service for preserving battery life
        gpsTracker.stopUsingGPS();

        //No need for bluetooth status broadcast receiver
        try {
            unregisterReceiver(receiverBluetoothStatus);
        } catch (Exception e){
        }

        //No need for sending SMS status
        try {
            unregisterReceiver(smsResultReceiver);
        } catch (Exception e){
        }

    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = adapter.getRemoteDevice(address);
        // Attempt to connect to the device
        bluetoothService.connect(device, secure);
    }

    //Every button menu to it's place and the destination invisible
    private void restoreMenuButtons(){
        if(bluetoothButton == null)
            bluetoothButton = findViewById(R.id.bluetooth);
        if(settingsButton == null)
            settingsButton = findViewById(R.id.settings);
        if(peopleButton == null)
            peopleButton = findViewById(R.id.people);
        if (destination == null)
            destination = findViewById(R.id.destination);

        bluetoothButton.setBackgroundResource(R.drawable.bluet);
        settingsButton.setBackgroundResource(R.drawable.settings);
        peopleButton.setBackgroundResource(R.drawable.ic_action_group);

        destination.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    public void menuBluetoothPinned(){
        Intent intent;
        //Get bluetooth adapter
        if(adapter == null)
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 )
                adapter = BluetoothAdapter.getDefaultAdapter();
            else
                adapter = ((android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if(adapter != null) {
            if (!adapter.isEnabled()) {
                //If not turn on, request it
                intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_TURN_ON_BLUETOOTH);
            } else {
                if(adapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    if(builder == null)
                        builder = new AlertDialog.Builder(this);
                    Dialog dialog = builder.setTitle(R.string.title_dialog_scan_again)
                            .setCancelable(true)
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    restoreMenuButtons();
                                }
                            })
                            .setNegativeButton(R.string.reject_scan_dialog, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    restoreMenuButtons();

                                    if (bluetoothService != null)
                                        bluetoothService.stop();
                                    adapter.cancelDiscovery();
                                    if (adapter != null)
                                        adapter.disable();
                                    setStatus(R.string.not_requested_bluetooth);
                                }
                            })
                            .setPositiveButton(R.string.accept_scan_dialog, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    restoreMenuButtons();
                                    if (adapter == null)
                                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 )
                                            adapter = BluetoothAdapter.getDefaultAdapter();
                                        else
                                            adapter = ((android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

                                    // Launch the DeviceListActivity to see devices and do scan
                                    Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

                                }
                            }).create();
                    dialog.show();
                }else{
                    ensureDiscoverable();
                }
            }
        } else{
            Toast.makeText(this, getResources().getString(R.string.no_bluetooth), Toast.LENGTH_SHORT).show();
            restoreMenuButtons();
        }
    }

    public void menuPeoplePinned(){
        Intent intent;
        intent = new Intent(this, ContactsActivity.class);
        startActivity(intent);

    }

    public void menuSettingsPinned(){

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CHANGE_SETTINGS);
    }

    //Broadcast receiver for bluetooth status
    private void registerBluetoothReceiver(){

        //Receiver for bluetooth state: ON or OFF
        //Careful not to register twice
        try{
            unregisterReceiver(receiverBluetoothStatus);
        }catch (Exception e){
        }
        if(intentFilterBluetoothState == null)
            intentFilterBluetoothState = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(receiverBluetoothStatus, intentFilterBluetoothState);

    }

    //Make sure the bluetooth is discoverable
    private void ensureDiscoverable() {
        if (adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_TIME);
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABILITY);
        }else{
            //Discoverable already.Register receiver for status
            registerBluetoothReceiver();
            if(prefs == null)
                prefs = getSharedPreferences(SettingsActivity.PREF_FILE, MODE_PRIVATE);
            prefs.edit().putBoolean(
                    SettingsActivity.PREF_NAME_ALLOW_BT, true).apply();

            //adapter.cancelDiscovery();
            //adapter.startDiscovery();
        }
    }

    //Set subtitle in action bar - bluetooth connection status (if any)
    private final void setStatus(int resId) {
        final TextView textView = (TextView)findViewById(R.id.bluetooth_status);
        if(textView.getVisibility() != View.VISIBLE)
            textView.setVisibility(View.VISIBLE);
        textView.setText(resId);
    }

    private void updateLocation(){
        if(gpsTracker == null)
            gpsTracker = new GPSTracker(this);

        gpsTracker.getLocation();

        if(!gpsTracker.canGetLocation())
            gpsTracker.showSettingsAlert();
    }

    //Register receiver for SMS sending events
    private void listenForSMSSendingEvents(){
        try {
            unregisterReceiver(smsResultReceiver);
        }catch (Exception e){
        }

        IntentFilter intentFilter = new IntentFilter(Actions.SMS_SEND);
        registerReceiver(smsResultReceiver, intentFilter);
    }

    //Service containing media button broadcast receiver will run until user explicitly kills
    private void registerHeadsetReceiver() {

        //The broadcast receiver is in ServiceListenerHeadphones.ACTION_HEADSET_PLUG intent is sent only
        // to dynamically registered receivers
        if(prefs == null)
            prefs = getSharedPreferences(SettingsActivity.PREF_FILE, MODE_PRIVATE);
        serviceListenerMediaButton(prefs.getBoolean(SettingsActivity.HEADSET_LISTENER_PREF, false));
    }

    private void serviceListenerMediaButton(boolean isNeeded){

        Intent serviceIntent = new Intent(this, ServiceListenerHeadphones.class);
        stopService(serviceIntent);
        if(isNeeded)
            startService(serviceIntent);

    }
/*
    private class StartHeadphoneListenerService implements Runnable{

        private Context context;

        public StartHeadphoneListenerService(Context context){
            this.context = context;
        }

        @Override
        public void run() {
            Intent serviceIntent = new Intent(context, ServiceListenerHeadphones.class);
            stopService(serviceIntent);
            if(context.getSharedPreferences(SettingsActivity.PREF_FILE, MODE_PRIVATE).getBoolean(
                    SettingsActivity.HEADSET_LISTENER_PREF, false))
                startService(serviceIntent);
        }
    }
*/
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(R.string.title_connected_to);
                            //mConversationArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    //byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    //Broadcast bluetooth event to ReceiverInternalBroadcast
                    Intent intent = new Intent(ReceiverInternalBroadcast.ACTION_ALERT);
                    intent.putExtra("message", readMessage);
                    sendOrderedBroadcast(intent, null);
                    break;

                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.connected_to) + " "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case MESSAGE_TOAST:
                    String data = msg.getData().getString(TOAST);
                    try {
                        if (data.equals(UNABLE_TO_CONNECT) && mContext != null) {
                            builder = new AlertDialog.Builder(mContext);
                            Dialog dialog;
                            dialog = builder.setTitle(getResources().getString(R.string.title_dialog_unable_to_connect))
                                    .setCancelable(true)
                                    .setNegativeButton(R.string.reject_dialog, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .setPositiveButton(R.string.accept_dialog, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (adapter == null)
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                                                    adapter = ((android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
                                                else
                                                    adapter = BluetoothAdapter.getDefaultAdapter();
                                            if (bluetoothDevice == null) {
                                                //adapter.cancelDiscovery();
                                                //adapter.startDiscovery();
                                                Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                                                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                                            }else
                                                bluetoothService.connect(bluetoothDevice, SECURE_CONNECTION);
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            restoreMenuButtons();
                                        }
                                    })
                                    .create();
                            dialog.show();
                            break;
                        }
                    }catch(Exception e){
                    }
                    if(data.equals(CONNECTION_LOST)) {
                        try {
                            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.connection_lost),
                                    Toast.LENGTH_SHORT).show();
                            break;
                        }catch (Exception e){
                        }
                    }
                    try {
                        Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                                Toast.LENGTH_SHORT).show();
                        break;
                    }catch (Exception e){
                    }

                    break;
            }
        }
    };

    private final BroadcastReceiver receiverBluetoothStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if(bluetoothService != null)
                            bluetoothService.stop();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        if(bluetoothService != null)
                            bluetoothService.start();
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver smsResultReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()){
                case Activity.RESULT_OK:
                    Toast.makeText(context, context.getResources().getString(R.string.sms_sent),
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, context.getResources().getString(R.string.generic_failure),
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, context.getResources().getString(R.string.no_service),
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, context.getResources().getString(R.string.null_pdu),
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, context.getResources().getString(R.string.radio_off),
                            Toast.LENGTH_SHORT).show();
                    break;

            }
        }

    };
}
