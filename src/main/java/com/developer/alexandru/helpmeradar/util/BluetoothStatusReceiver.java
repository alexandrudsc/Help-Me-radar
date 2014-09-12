package com.developer.alexandru.helpmeradar.util;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;


/**
 * Created by Alexandru on 8/29/2014.
 */

public class BluetoothStatusReceiver extends BroadcastReceiver {

    private View bluetoothVisible;

    public BluetoothStatusReceiver(View bluetoothVisible){
        this.bluetoothVisible = bluetoothVisible;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
                    == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                bluetoothVisible.setVisibility(View.VISIBLE);
            }
            else
                bluetoothVisible.setVisibility(View.GONE);
        }
    }
}