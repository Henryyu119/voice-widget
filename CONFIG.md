# 配置指南

## 飞书应用配置详细步骤

### 1. 创建飞书应用

1. 访问 [飞书开放平台](https://open.feishu.cn/app)
2. 点击"创建企业自建应用"
3. 填写应用信息：
   - 应用名称：快速录音 Widget
   - 应用描述：桌面快速录音工具
   - 应用图标：上传一个图标

### 2. 获取凭证

在应用详情页面：

1. 点击"凭证与基础信息"
2. 复制 `App ID`
3. 复制 `App Secret`

### 3. 配置权限

在"权限管理"页面，添加以下权限：

#### 必需权限

- `im:message` - 获取与发送单聊、群组消息
- `im:message.group_at_msg` - 获取群组中所有消息
- `im:file` - 上传文件

#### 权限申请理由

```
应用需要发送语音消息到指定对话，用于快速记录信息。
```

### 4. 发布应用

1. 点击"版本管理与发布"
2. 创建版本
3. 提交审核（企业内部应用通常自动通过）
4. 发布到企业

### 5. 获取对话 ID

#### 方法 1：通过网页版

1. 打开飞书网页版
2. 进入目标对话
3. 查看浏览器地址栏
4. URL 格式：`https://xxx.feishu.cn/messenger/oc_xxxxx`
5. `oc_xxxxx` 就是对话 ID

#### 方法 2：通过 API

```bash
curl -X GET \
  'https://open.feishu.cn/open-apis/im/v1/chats?page_size=20' \
  -H 'Authorization: Bearer YOUR_TENANT_ACCESS_TOKEN'
```

### 6. 修改代码配置

编辑 `app/src/main/java/com/openclaw/voicewidget/FeishuClient.kt`：

```kotlin
companion object {
    // 替换为你的配置
    private const val APP_ID = "cli_xxxxxxxxxxxxx"
    private const val APP_SECRET = "xxxxxxxxxxxxxxxxxxxxx"
    private const val CHAT_ID = "oc_074f724cab241350a3afa017c3356707"
}
```

## 编译配置

### 方法 1：使用 Android Studio（推荐）

1. 打开 Android Studio
2. File → Open → 选择 VoiceWidget 文件夹
3. 等待 Gradle 同步完成
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
5. 等待编译完成
6. 点击通知中的 "locate" 查看 APK

### 方法 2：使用命令行

```bash
cd VoiceWidget

# 首次编译需要下载依赖
./gradlew build

# 编译 Debug 版本（用于测试）
./gradlew assembleDebug

# 编译 Release 版本（用于发布）
./gradlew assembleRelease
```

### 签名配置（Release 版本）

创建 `app/keystore.properties`：

```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=your_key_alias
storeFile=your_keystore_file.jks
```

修改 `app/build.gradle`：

```gradle
android {
    signingConfigs {
        release {
            def keystorePropertiesFile = rootProject.file("keystore.properties")
            def keystoreProperties = new Properties()
            keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## 安装配置

### 通过 USB 安装

```bash
# 启用开发者选项和 USB 调试
# 连接手机到电脑

# 安装 APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 如果已安装，覆盖安装
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 通过文件传输安装

1. 将 APK 文件传输到手机
2. 在手机上打开文件管理器
3. 找到 APK 文件并点击安装
4. 允许"安装未知应用"权限

## 测试配置

### 1. 权限测试

```bash
# 检查应用权限
adb shell dumpsys package com.openclaw.voicewidget | grep permission

# 授予录音权限
adb shell pm grant com.openclaw.voicewidget android.permission.RECORD_AUDIO
```

### 2. 录音测试

1. 打开应用
2. 点击"检查权限"
3. 授予所有权限
4. 添加 Widget 到桌面
5. 点击 Widget 测试录音

### 3. 网络测试

```bash
# 查看应用日志
adb logcat | grep VoiceWidget

# 查看网络请求
adb logcat | grep OkHttp
```

### 4. 飞书 API 测试

使用 curl 测试 API：

```bash
# 获取 token
curl -X POST \
  'https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal' \
  -H 'Content-Type: application/json' \
  -d '{
    "app_id": "YOUR_APP_ID",
    "app_secret": "YOUR_APP_SECRET"
  }'

# 发送消息
curl -X POST \
  'https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=chat_id' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "receive_id": "oc_xxxxx",
    "msg_type": "text",
    "content": "{\"text\":\"测试消息\"}"
  }'
```

## 故障排查

### 编译错误

**问题**: Gradle 同步失败

**解决**:
```bash
# 清理缓存
./gradlew clean

# 重新下载依赖
./gradlew build --refresh-dependencies
```

**问题**: SDK 版本不匹配

**解决**: 在 Android Studio 中：
1. Tools → SDK Manager
2. 安装 Android 14.0 (API 34)

### 运行时错误

**问题**: 录音权限被拒绝

**解决**:
```bash
# 手动授予权限
adb shell pm grant com.openclaw.voicewidget android.permission.RECORD_AUDIO
adb shell pm grant com.openclaw.voicewidget android.permission.POST_NOTIFICATIONS
```

**问题**: 飞书 API 返回 401

**解决**:
1. 检查 App ID 和 App Secret 是否正确
2. 检查应用是否已发布
3. 检查权限是否已添加

**问题**: Widget 不显示

**解决**:
1. 重启手机
2. 重新安装应用
3. 检查 AndroidManifest.xml 中的 Widget 配置

## 性能优化配置

### ProGuard 配置

创建 `app/proguard-rules.pro`：

```proguard
# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保留 FeishuClient
-keep class com.openclaw.voicewidget.FeishuClient { *; }
```

### 电池优化

在应用中添加提示，引导用户关闭电池优化：

```kotlin
val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
    data = Uri.parse("package:$packageName")
}
startActivity(intent)
```

## 更新配置

### 版本号管理

在 `app/build.gradle` 中：

```gradle
android {
    defaultConfig {
        versionCode 2        // 每次更新 +1
        versionName "1.1"    // 显示给用户的版本号
    }
}
```

### 更新检查

可以添加简单的更新检查逻辑：

```kotlin
fun checkUpdate() {
    // 从服务器获取最新版本号
    // 比较本地版本号
    // 提示用户更新
}
```

---

配置完成后，就可以开始使用快速录音 Widget 了！
