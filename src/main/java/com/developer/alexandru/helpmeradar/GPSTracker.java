package com.developer.alexandru.helpmeradar;

/**
 * Created by Alexandru on 8/16/2014.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Set;

public class GPSTracker extends Service implements LocationListener {

    private final static String TAG = "GPSTracker";

    private  MainActivity mainActivity;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    //Dialog mechanism
    private AlertDialog.Builder alertDialog;
    private Dialog dialog;

    private TextView textView;

    public GPSTracker(MainActivity mainActivity) {
        //Recreated
        this.stopUsingGPS();

        this.mainActivity = mainActivity;
        registerListeners();
    }

    public Location getLocation() {

        try {
            if(locationManager == null)
                locationManager = (LocationManager) mainActivity.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            updateUI(location);
                        }

                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                updateUI(location);
                            }

                        }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    private void registerListeners() {
        //register the listeners to request the location as soon as possible
        if (locationManager == null)
            locationManager = (LocationManager) mainActivity
                    .getSystemService(LOCATION_SERVICE);

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this);

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this);
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        if(alertDialog == null)
            alertDialog = new AlertDialog.Builder(mainActivity);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.enable_bluetooth_title);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.enable_bluetooth_text);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                PreferenceManager.getDefaultSharedPreferences(mainActivity).edit().putBoolean(
                        SettingsActivity.PREF_NAME_ALLOW_GPS, true).commit();
                mainActivity.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                PreferenceManager.getDefaultSharedPreferences(mainActivity).edit().putBoolean(
                        SettingsActivity.PREF_NAME_ALLOW_GPS, false).commit();
            }
        });

        // Showing Alert Message.If it was already shown do NOT do it again
        if(dialog == null)
            dialog = alertDialog.create();
        if(!dialog.isShowing())
            dialog.show();
    }

    private void updateUI(final Location location){
        try {
            this.location = location;
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);

            prefs.edit().putString(SettingsActivity.PREF_NAME_LAST_GPS_COORDS, location.getLatitude() +
                    " " + location.getLongitude()).apply();
            if(textView == null)
                textView = (TextView) mainActivity.findViewById(R.id.label_gps_coords);
            if (prefs.getBoolean(SettingsActivity.PREF_NAME_ALLOW_GPS, true)) {
                if(textView == null)
                    textView = (TextView) mainActivity.findViewById(R.id.label_gps_coords);
                textView.setText("Lat " + getLatitude() + " " +
                                 "Lng " + getLongitude() );

            }else
                textView.setText(mainActivity.getResources().getString(R.string.no_coords));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null)
            updateUI(location);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.getLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

