package com.auto.clicker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.auto.clicker.MainActivity;
import com.auto.clicker.R;
import com.auto.clicker.model.ActionRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ReplayService extends Service {

    private static final String TAG = "ReplayService";
    private static final String CHANNEL_ID = "replay_channel";

    private Handler mainHandler;
    private boolean isReplaying = false;
    private List<ActionRecord> actions;
    private int repeatCount;
    private int currentRepeat = 0;
    private int currentActionIndex = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mainHandler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if ("STOP".equals(intent.getAction())) {
            stopReplay();
            return START_NOT_STICKY;
        }

        // 解析参数
        String actionsJson = intent.getStringExtra("actions");
        repeatCount = intent.getIntExtra("repeatCount", 1);
        int clickDelay = intent.getIntExtra("clickDelay", 200);
        int swipeDelay = intent.getIntExtra("swipeDelay", 500);
        int longDelay = intent.getIntExtra("longDelay", 1000);

        Type listType = new TypeToken<List<ActionRecord>>() {}.getType();
        actions = new Gson().fromJson(actionsJson, listType);

        if (actions == null || actions.isEmpty()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // 启动前台服务
        startForeground(2, createNotification("回放中..."));

        // 开始回放
        isReplaying = true;
        currentRepeat = 0;
        currentActionIndex = 0;

        ClickAccessibilityService service = ClickAccessibilityService.getInstance();
        if (service == null) {
            Log.e(TAG, "Accessibility service not available");
            stopSelf();
            return START_NOT_STICKY;
        }

        // 开始执行操作序列
        executeNextAction(service, clickDelay, swipeDelay, longDelay);

        return START_NOT_STICKY;
    }

    private void executeNextAction(ClickAccessibilityService service, int clickDelay, int swipeDelay, int longDelay) {
        if (!isReplaying || currentRepeat >= repeatCount) {
            stopReplay();
            return;
        }

        if (currentActionIndex >= actions.size()) {
            // 当前轮次完成
            currentRepeat++;
            currentActionIndex = 0;

            if (currentRepeat >= repeatCount) {
                stopReplay();
                return;
            }
        }

        ActionRecord action = actions.get(currentActionIndex);
        long delay = 0;

        switch (action.type) {
            case "click":
                delay = clickDelay;
                service.performClick(action.x, action.y, delay);
                break;
            case "long_click":
                delay = longDelay;
                service.performLongClick(action.x, action.y, 1000, delay);
                break;
            case "swipe":
                delay = swipeDelay;
                service.performSwipe(action.startX, action.startY, action.endX, action.endY, action.swipeDuration > 0 ? action.swipeDuration : 300, delay);
                break;
        }

        currentActionIndex++;

        // 延迟执行下一个操作
        mainHandler.postDelayed(() -> {
            executeNextAction(service, clickDelay, swipeDelay, longDelay);
        }, delay + 50);
    }

    private void stopReplay() {
        isReplaying = false;
        mainHandler.removeCallbacksAndMessages(null);
        stopForeground(true);
        stopSelf();
        Log.d(TAG, "Replay stopped");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "回放服务",
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
        isReplaying = false;
        mainHandler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
