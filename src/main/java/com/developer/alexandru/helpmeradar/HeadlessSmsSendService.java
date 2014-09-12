package com.developer.alexandru.helpmeradar;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Alexandru on 9/1/2014.
 * Dummy class. Integrated in order to be eligible for default messaging app 4.4+
 */
public class HeadlessSmsSendService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
