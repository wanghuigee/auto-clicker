package com.auto.clicker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.auto.clicker.MainActivity;
import com.auto.clicker.R;
import com.auto.clicker.model.ActionRecord;

public class FloatWindowService extends Service {

    private static final String CHANNEL_ID = "float_window_channel";
    private WindowManager windowManager;
    private View floatView;
    private TextView tvStatus;
    private PointF lastTouch;
    private long touchStartTime;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "START_RECORD":
                    startForeground(1, createNotification("录制中..."));
                    showFloatWindow();
                    ClickAccessibilityService.setRecording(true);
                    break;
                case "STOP":
                    ClickAccessibilityService.setRecording(false);
                    hideFloatWindow();
                    stopForeground(true);
                    stopSelf();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    private void showFloatWindow() {
        if (floatView != null) return;

        // 创建悬浮窗View
        tvStatus = new TextView(this);
        tvStatus.setText("● 录制中");
        tvStatus.setTextColor(Color.RED);
        tvStatus.setTextSize(14);
        tvStatus.setBackgroundColor(0xCC000000);
        tvStatus.setPadding(20, 10, 20, 10);

        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.x = 0;
        params.y = 100;

        // 添加触摸事件，实现拖动
        tvStatus.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouch = new PointF(event.getRawX(), event.getRawY());
                    touchStartTime = System.currentTimeMillis();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (lastTouch != null) {
                        params.x += (int) (event.getRawX() - lastTouch.x);
                        params.y += (int) (event.getRawY() - lastTouch.y);
                        windowManager.updateViewLayout(tvStatus, params);
                        lastTouch = new PointF(event.getRawX(), event.getRawY());
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    // 如果是点击（不是拖动），则记录操作
                    long duration = System.currentTimeMillis() - touchStartTime;
                    if (duration < 200) {
                        // 这里只是示意，实际需要结合无障碍服务获取坐标
                    }
                    lastTouch = null;
                    return true;
            }
            return false;
        });

        try {
            windowManager.addView(tvStatus, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideFloatWindow() {
        if (tvStatus != null && windowManager != null) {
            try {
                windowManager.removeView(tvStatus);
            } catch (Exception e) {
                e.printStackTrace();
            }
            tvStatus = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "悬浮窗服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String text) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("按键模拟器")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideFloatWindow();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
