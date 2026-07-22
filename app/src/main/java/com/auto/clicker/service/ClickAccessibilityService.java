package com.auto.clicker.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.auto.clicker.model.ActionRecord;

import java.util.ArrayList;
import java.util.List;

public class ClickAccessibilityService extends AccessibilityService {

    private static final String TAG = "ClickAccessibility";
    private static List<ActionRecord> recordedActions = new ArrayList<>();
    private static boolean isRecording = false;

    private static ClickAccessibilityService instance;
    private Handler mainHandler;

    public static ClickAccessibilityService getInstance() {
        return instance;
    }

    public static void setRecording(boolean recording) {
        isRecording = recording;
    }

    public static List<ActionRecord> getRecordedActions() {
        return new ArrayList<>(recordedActions);
    }

    public static void clearRecordedActions() {
        recordedActions.clear();
    }

    public static void addAction(ActionRecord action) {
        if (isRecording) {
            recordedActions.add(action);
            Log.d(TAG, "Recorded action: " + action.type);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 可以在这里处理其他无障碍事件
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        mainHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "Accessibility service connected");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "Accessibility service destroyed");
    }

    /**
     * 执行点击操作
     */
    public void performClick(float x, float y, long delay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mainHandler.postDelayed(() -> {
                Path path = new Path();
                path.moveTo(x, y);
                path.lineTo(x, y);

                GestureDescription.Builder builder = new GestureDescription.Builder();
                builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));

                dispatchGesture(builder.build(), new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        Log.d(TAG, "Click completed: " + x + "," + y);
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        Log.d(TAG, "Click cancelled");
                    }
                }, null);
            }, delay);
        }
    }

    /**
     * 执行长按操作
     */
    public void performLongClick(float x, float y, long duration, long delay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mainHandler.postDelayed(() -> {
                Path path = new Path();
                path.moveTo(x, y);
                path.lineTo(x, y);

                GestureDescription.Builder builder = new GestureDescription.Builder();
                builder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));

                dispatchGesture(builder.build(), new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        Log.d(TAG, "Long click completed: " + x + "," + y);
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        Log.d(TAG, "Long click cancelled");
                    }
                }, null);
            }, delay);
        }
    }

    /**
     * 执行滑动操作
     */
    public void performSwipe(float startX, float startY, float endX, float endY, int duration, long delay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mainHandler.postDelayed(() -> {
                Path path = new Path();
                path.moveTo(startX, startY);
                path.lineTo(endX, endY);

                GestureDescription.Builder builder = new GestureDescription.Builder();
                builder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));

                dispatchGesture(builder.build(), new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        Log.d(TAG, "Swipe completed");
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        Log.d(TAG, "Swipe cancelled");
                    }
                }, null);
            }, delay);
        }
    }
}
