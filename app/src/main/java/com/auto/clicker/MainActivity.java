package com.auto.clicker;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.auto.clicker.databinding.ActivityMainBinding;
import com.auto.clicker.model.ActionRecord;
import com.auto.clicker.service.ClickAccessibilityService;
import com.auto.clicker.service.FloatWindowService;
import com.auto.clicker.service.ReplayService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<ActionRecord> recording = new ArrayList<>();
    private boolean isRecording = false;
    private boolean isReplaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    private void setupListeners() {
        // 开启无障碍服务
        binding.btnOpenAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        // 开始录制
        binding.btnStartRecord.setOnClickListener(v -> startRecording());

        // 停止录制
        binding.btnStopRecord.setOnClickListener(v -> stopRecording());

        // 单次回放
        binding.btnReplayOnce.setOnClickListener(v -> replay(1));

        // 循环回放
        binding.btnReplayLoop.setOnClickListener(v -> {
            int count = parseIntSafe(binding.etLoopCount.getText().toString(), 3);
            replay(count);
        });

        // 停止回放
        binding.btnStopReplay.setOnClickListener(v -> stopReplay());

        // 保存文件
        binding.btnSaveFile.setOnClickListener(v -> saveRecording());

        // 加载文件
        binding.btnLoadFile.setOnClickListener(v -> loadRecording());
    }

    private void checkPermissions() {
        // 检查无障碍服务
        boolean accessibilityEnabled = isAccessibilityServiceEnabled();
        binding.tvAccessibilityStatus.setText("无障碍服务：" + (accessibilityEnabled ? "已开启 ✓" : "未开启 ✗"));
        binding.tvAccessibilityStatus.setTextColor(accessibilityEnabled ? 0xFF00FF88 : 0xFFFF6B6B);

        // 检查悬浮窗权限
        boolean floatWindowEnabled = Settings.canDrawOverlays(this);
        binding.tvFloatWindowStatus.setText("悬浮窗权限：" + (floatWindowEnabled ? "已开启 ✓" : "未开启 ✗"));
        binding.tvFloatWindowStatus.setTextColor(floatWindowEnabled ? 0xFF00FF88 : 0xFFFF6B6B);
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : enabledServices) {
            if (info.getId().contains(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void startRecording() {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
            return;
        }

        recording.clear();
        isRecording = true;

        // 启动悬浮窗显示录制状态
        Intent serviceIntent = new Intent(this, FloatWindowService.class);
        serviceIntent.setAction("START_RECORD");
        startForegroundService(serviceIntent);

        binding.btnStartRecord.setEnabled(false);
        binding.btnStopRecord.setEnabled(true);
        binding.tvStatus.setText("录制中... 请操作屏幕");
    }

    private void stopRecording() {
        isRecording = false;

        // 停止悬浮窗
        Intent serviceIntent = new Intent(this, FloatWindowService.class);
        serviceIntent.setAction("STOP");
        startService(serviceIntent);

        // 获取录制的操作
        recording = ClickAccessibilityService.getRecordedActions();
        ClickAccessibilityService.clearRecordedActions();

        binding.btnStartRecord.setEnabled(true);
        binding.btnStopRecord.setEnabled(false);
        binding.tvStatus.setText("录制完成，共 " + recording.size() + " 个操作");
    }

    private void replay(int repeatCount) {
        if (recording.isEmpty()) {
            Toast.makeText(this, "请先录制操作", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
            return;
        }

        isReplaying = true;
        binding.btnStopReplay.setEnabled(true);

        // 启动回放服务
        Intent serviceIntent = new Intent(this, ReplayService.class);
        serviceIntent.putExtra("actions", new Gson().toJson(recording));
        serviceIntent.putExtra("repeatCount", repeatCount);
        serviceIntent.putExtra("clickDelay", parseIntSafe(binding.etClickDelay.getText().toString(), 200));
        serviceIntent.putExtra("swipeDelay", parseIntSafe(binding.etSwipeDelay.getText().toString(), 500));
        serviceIntent.putExtra("longDelay", parseIntSafe(binding.etLongDelay.getText().toString(), 1000));
        startForegroundService(serviceIntent);

        binding.tvStatus.setText("回放中... 第 1/" + repeatCount + " 次");
    }

    private void stopReplay() {
        isReplaying = false;
        binding.btnStopReplay.setEnabled(false);

        Intent serviceIntent = new Intent(this, ReplayService.class);
        serviceIntent.setAction("STOP");
        startService(serviceIntent);

        binding.tvStatus.setText("回放已停止");
    }

    private void saveRecording() {
        if (recording.isEmpty()) {
            Toast.makeText(this, "没有录制内容可保存", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File dir = new File(getExternalFilesDir(null), "records");
            if (!dir.exists()) dir.mkdirs();

            String fileName = "record_" + System.currentTimeMillis() + ".json";
            File file = new File(dir, fileName);

            FileWriter writer = new FileWriter(file);
            writer.write(new Gson().toJson(recording));
            writer.close();

            binding.tvFileInfo.setText("已保存: " + fileName);
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRecording() {
        try {
            File dir = new File(getExternalFilesDir(null), "records");
            if (!dir.exists()) {
                Toast.makeText(this, "没有找到录制文件", Toast.LENGTH_SHORT).show();
                return;
            }

            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files == null || files.length == 0) {
                Toast.makeText(this, "没有找到录制文件", Toast.LENGTH_SHORT).show();
                return;
            }

            // 简单处理：加载最新的文件
            File latestFile = files[files.length - 1];
            FileReader reader = new FileReader(latestFile);
            Type listType = new TypeToken<List<ActionRecord>>() {}.getType();
            recording = new Gson().fromJson(reader, listType);
            reader.close();

            binding.tvFileInfo.setText("已加载: " + latestFile.getName() + " (" + recording.size() + "个操作)");
            Toast.makeText(this, "加载成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int parseIntSafe(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
