# 快速文字记录 Widget - 使用说明

## 项目简介

这是一个 Android 桌面 Widget 应用，可以一键录音并发送到飞书对话。

## 功能特性

- ✅ 桌面 Widget 一键录音
- ✅ 点击式交互（点击开始，再点击停止并发送）
- ✅ 实时显示录音时长
- ✅ 自动发送到飞书指定对话
- ✅ 状态反馈（录音中、发送中、成功）
- ✅ 前台服务保证录音稳定

## 技术栈

- **语言**: Kotlin
- **最低 SDK**: Android 8.0 (API 26)
- **目标 SDK**: Android 14 (API 34)
- **核心组件**:
  - AppWidgetProvider (Widget)
  - MediaRecorder (录音)
  - OkHttp (网络请求)
  - Foreground Service (前台服务)

## 安装步骤

### 1. 配置飞书应用

在使用前，需要先配置飞书应用：

1. 访问 [飞书开放平台](https://open.feishu.cn/)
2. 创建企业自建应用
3. 获取 `App ID` 和 `App Secret`
4. 添加以下权限：
   - `im:message` (发送消息)
   - `im:message.group_at_msg` (发送群消息)
   - `im:file` (上传文件)

### 2. 修改配置

编辑 `FeishuClient.kt` 文件，填入你的配置：

```kotlin
private const val APP_ID = "YOUR_APP_ID"          // 替换为你的 App ID
private const val APP_SECRET = "YOUR_APP_SECRET"  // 替换为你的 App Secret
private const val CHAT_ID = "oc_074f724cab241350a3afa017c3356707"  // 对话 ID
```

### 3. 编译 APK

使用 Android Studio：

```bash
# 打开项目
cd VoiceWidget

# 编译 Debug 版本
./gradlew assembleDebug

# 编译 Release 版本
./gradlew assembleRelease
```

APK 文件位置：
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### 4. 安装到手机

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 使用方法

### 首次使用

1. 打开应用
2. 点击"检查权限"，授予录音权限
3. 点击"配置飞书"，输入 App ID 和 App Secret（可选，如果已在代码中配置）

### 添加 Widget

1. 长按桌面空白处
2. 选择"小部件"或"Widget"
3. 找到"快速录音"
4. 拖动到桌面

### 录音操作

1. **点击 Widget** → 开始录音（按钮变红🔴）
2. **再次点击** → 停止录音并发送到飞书
3. 等待发送完成（显示✓）

## 项目结构

```
VoiceWidget/
├── app/
│   ├── src/main/
│   │   ├── java/com/openclaw/voicewidget/
│   │   │   ├── VoiceWidget.kt          # Widget 主类
│   │   │   ├── RecordingService.kt     # 录音服务
│   │   │   ├── FeishuClient.kt         # 飞书 API 客户端
│   │   │   └── MainActivity.kt         # 配置界面
│   │   ├── res/
│   │   │   ├── layout/                 # 布局文件
│   │   │   ├── drawable/               # 图形资源
│   │   │   ├── values/                 # 字符串资源
│   │   │   └── xml/                    # Widget 配置
│   │   └── AndroidManifest.xml         # 应用清单
│   └── build.gradle                    # 应用构建配置
├── build.gradle                        # 项目构建配置
├── settings.gradle                     # 项目设置
└── gradle.properties                   # Gradle 属性
```

## 核心功能说明

### 1. Widget (VoiceWidget.kt)

- 管理 Widget 状态（录音中/空闲）
- 处理点击事件
- 更新 UI 显示

### 2. 录音服务 (RecordingService.kt)

- 使用 MediaRecorder 录制音频
- 前台服务保证录音不被杀死
- 实时更新录音时长
- 录音完成后自动发送

### 3. 飞书客户端 (FeishuClient.kt)

- 获取 tenant_access_token
- 上传音频文件
- 发送语音消息到指定对话

### 4. 主界面 (MainActivity.kt)

- 权限管理
- 飞书配置
- 使用说明

## 权限说明

应用需要以下权限：

- `RECORD_AUDIO`: 录音
- `INTERNET`: 网络请求
- `FOREGROUND_SERVICE`: 前台服务
- `POST_NOTIFICATIONS`: 通知（Android 13+）

## 常见问题

### Q: 录音没有声音？
A: 检查是否授予了录音权限，在设置 → 应用 → 快速录音 → 权限中确认。

### Q: 发送失败？
A: 
1. 检查网络连接
2. 确认飞书 App ID 和 App Secret 正确
3. 确认对话 ID 正确
4. 检查飞书应用权限是否配置完整

### Q: Widget 点击没反应？
A: 
1. 重新添加 Widget
2. 检查应用是否被系统杀死（在设置中关闭电池优化）

### Q: 如何获取对话 ID？
A: 
1. 在飞书网页版打开对话
2. 查看 URL，格式为 `https://xxx.feishu.cn/messenger/oc_xxxxx`
3. `oc_xxxxx` 就是对话 ID

## 优化建议

### 性能优化

1. 音频格式使用 AAC，压缩率高
2. 采样率 44.1kHz，平衡质量和文件大小
3. 录音完成后自动删除临时文件

### 用户体验优化

1. 前台服务显示录音状态
2. 通知显示发送进度
3. 成功后自动关闭通知

### 未来改进

- [ ] 支持长按录音（类似微信）
- [ ] 支持本地语音识别
- [ ] 支持多个对话选择
- [ ] 支持录音历史记录
- [ ] 支持自定义 Widget 样式

## 开发环境

- Android Studio: Hedgehog | 2023.1.1+
- Gradle: 8.1.0
- Kotlin: 1.9.0
- Min SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)

## 许可证

MIT License

## 联系方式

如有问题，请联系开发者。

---

**注意**: 本应用仅供个人使用，请遵守飞书开放平台使用规范。
