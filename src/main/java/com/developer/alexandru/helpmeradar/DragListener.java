package com.developer.alexandru.helpmeradar;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Alexandru on 9/6/2014.
 */

public class DragListener implements View.OnDragListener {

    private static final String TAG = "DRAG AND DROP";

    private Activity activity;
    public static int draggedViewId;

    public DragListener(Activity activity){
        this.activity = activity;
    }

    @Override
    public boolean onDrag(View view, DragEvent event) {
        final View draggedView = activity.findViewById(draggedViewId);
        switch (event.getAction()){

            case DragEvent.ACTION_DRAG_STARTED:
                view.setBackgroundResource(R.drawable.empty_dest);
                view.setVisibility(View.VISIBLE);
                draggedViewId = Integer.valueOf(event.getClipDescription().getLabel().toString());

                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                Log.d(TAG, "enter");
                switch (draggedViewId){
                    case R.id.bluetooth:
                        view.setBackgroundResource(R.drawable.dest_bluetooth);
                        break;
                    case R.id.people:
                        view.setBackgroundResource(R.drawable.dest_people);
                        break;
                    case R.id.settings:
                        view.setBackgroundResource(R.drawable.dest_settings);
                        break;
                }
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                Log.d(TAG, "exit");
                view.setBackgroundResource(R.drawable.empty_dest);
                break;
            case DragEvent.ACTION_DROP:
                Log.d(TAG, "drop");
                switch (draggedViewId){
                    case R.id.bluetooth:
                        view.setBackgroundResource(R.drawable.pinned_bluetooth);
                        ((MainActivity)activity).menuBluetoothPinned();
                        break;
                    case R.id.people:
                        view.setBackgroundResource(R.drawable.pinned_people);
                        ((MainActivity)activity).menuPeoplePinned();
                        break;
                    case R.id.settings:
                        view.setBackgroundResource(R.drawable.pinned_settings);
                        ((MainActivity)activity).menuSettingsPinned();
                        break;
                }
                //draggedView.setVisibility(View.VISIBLE);
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                Log.d(TAG, "end");

                if(!event.getResult()) {
                    view.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
                    //Avoid setting the visibility to visible direct.
                    //Not modify the hash map with the views receiving the dragging events
                    activity.findViewById(draggedViewId).post(new Runnable() {
                        @Override
                        public void run() {
                            switch (draggedViewId){
                                case R.id.settings:
                                    draggedView.setBackgroundResource(R.drawable.settings);
                                    break;
                                case R.id.people:
                                    draggedView.setBackgroundResource(R.drawable.ic_action_group);
                                    break;
                                case R.id.bluetooth:
                                    draggedView.setBackgroundResource(R.drawable.bluet);
                                    break;
                            }

                        }
                    });
                }
                break;
        }
        return true;
    }

    public static class LongClickListener implements View.OnLongClickListener{
        @Override
        public boolean onLongClick(View view) {
            ClipData data = new ClipData(new ClipDescription(String.valueOf(view.getId()), new String[1]),
                    new ClipData.Item("item"));
            //DragListener.draggedViewId = view.getId();
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, view, 0);
            view.setBackgroundResource(R.drawable.empty_dest);
            return true;
        }
    }

    public static class TouchEvent implements View.OnTouchListener{
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    ClipData data = new ClipData(new ClipDescription(String.valueOf(view.getId()), new String[1]),
                            new ClipData.Item("item"));
                    //DragListener.draggedViewId = view.getId();
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                    view.startDrag(data, shadowBuilder, view, 0);
                    view.setBackgroundResource(R.drawable.empty_dest);
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "UP");
                    return true;
            }
            return false;
        }
    }

}
