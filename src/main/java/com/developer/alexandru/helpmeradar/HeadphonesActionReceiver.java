package com.developer.alexandru.helpmeradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.KeyEvent;

/**
 * Created by Alexandru on 8/18/2014.
 */
public class HeadphonesActionReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getCanonicalName().toUpperCase();
    private Camera camera;

    @Override
    public void onReceive(Context context, Intent intent) {

        //if(intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)){
            Bundle extras = intent.getExtras();
            KeyEvent event = null;
            if(extras != null) {
                event = extras.getParcelable(Intent.EXTRA_KEY_EVENT);
                //if(event != null && event.getAction() == KeyEvent.ACTION_DOWN) {

                //-1 no state, 1 - plugged, 0 - unplugged
                //int state = intent.getIntExtra("state", -1);

                //Headset type
                //String name = intent.getStringExtra("name");
                //Log.d("HEADPHONES", "state: " + state);
                //if(state == 1) {
                if(event.getAction() == KeyEvent.ACTION_DOWN)
                    Actions.sendSMSToAll(context);
                    /*if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
                        new CameraThread().run();
                    */
            }
                //}
            //}

        //}

    }

   /* private class CameraThread extends Thread{

        @Override
        public void run() {
            camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            camera.setParameters(parameters);
            camera.startPreview();
            try {
                Thread.sleep(2000);
            }catch (InterruptedException e){
                camera.stopPreview();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.release();
            }

            camera.stopPreview();
            camera.release();
        }
    }
*/

}
