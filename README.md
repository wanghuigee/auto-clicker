# 手机按键模拟器

一款Android手机按键模拟工具，支持录制和回放点击、滑动、长按操作。

## 功能特点

- **录制操作**：自动记录屏幕上的点击、滑动、长按操作
- **回放功能**：支持单次回放和多次循环回放
- **操作延迟**：可自定义点击、滑动、长按的延迟时间
- **文件管理**：录制的操作可以保存为JSON文件，方便分享和重复使用
- **悬浮窗**：录制时显示悬浮窗状态指示

## 安装使用

### 方法一：直接下载APK

从 [Releases](../../releases) 页面下载最新的APK文件安装。

### 方法二：从源码编译

1. 克隆仓库
   ```bash
   git clone https://github.com/yourusername/auto-clicker.git
   ```

2. 用Android Studio打开项目

3. 连接手机或启动模拟器，点击Run运行

### 方法三：使用GitHub Actions

1. Fork本仓库
2. 在Actions页面触发Build workflow
3. 从Artifacts下载编译好的APK

## 使用步骤

1. **开启无障碍服务**：首次使用需要在设置中开启无障碍服务权限
2. **开始录制**：点击"开始录制"按钮，然后操作手机
3. **停止录制**：操作完成后点击"停止录制"
4. **回放**：选择单次回放或设置循环次数后点击回放

## 权限说明

- **无障碍服务**：用于模拟点击、滑动操作
- **悬浮窗权限**：用于录制时显示状态指示
- **存储权限**：用于保存录制文件

## 技术栈

- Java
- Android AccessibilityService（无障碍服务）
- Gson（JSON解析）
- Material Design Components

## 开发环境

- Android Studio Hedgehog+
- JDK 17
- Android SDK 34
- Min SDK 24 (Android 7.0)

## 注意事项

- 本工具仅供学习和个人自动化使用
- 请勿用于恶意刷单、作弊等违规行为
- 使用时请确保已获得相关应用的使用许可

## License

MIT License
