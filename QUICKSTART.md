# 快速开始指南

## 5分钟快速上手

### 第一步：获取飞书凭证（2分钟）

1. 打开 https://open.feishu.cn/app
2. 点击"创建企业自建应用"
3. 填写应用名称："快速录音"
4. 复制 **App ID** 和 **App Secret**
5. 添加权限：
   - `im:message`
   - `im:message.group_at_msg`
   - `im:file`
6. 点击"发布"

### 第二步：获取对话 ID（1分钟）

1. 打开飞书网页版
2. 进入与 Jason 的对话
3. 查看浏览器地址栏
4. 复制 `oc_xxxxxx` 部分

### 第三步：修改配置（1分钟）

编辑文件：`app/src/main/java/com/openclaw/voicewidget/FeishuClient.kt`

找到这几行：
```kotlin
private const val APP_ID = "YOUR_APP_ID"
private const val APP_SECRET = "YOUR_APP_SECRET"
private const val CHAT_ID = "oc_074f724cab241350a3afa017c3356707"
```

替换为你的配置：
```kotlin
private const val APP_ID = "cli_a1b2c3d4e5f6g7h8"  // 你的 App ID
private const val APP_SECRET = "abcdefghijklmnopqrstuvwxyz123456"  // 你的 App Secret
private const val CHAT_ID = "oc_074f724cab241350a3afa017c3356707"  // 你的对话 ID
```

### 第四步：编译安装（1分钟）

#### 使用 Android Studio（推荐）
1. 打开 Android Studio
2. File → Open → 选择 `VoiceWidget` 文件夹
3. 等待 Gradle 同步
4. 点击绿色运行按钮 ▶️
5. 选择你的手机

#### 使用命令行
```bash
cd VoiceWidget
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 第五步：使用（30秒）

1. 打开应用，点击"检查权限"，授予录音权限
2. 长按桌面 → 小部件 → 找到"快速录音" → 拖到桌面
3. 点击 Widget 开始录音 🎤
4. 再次点击停止并发送 🔴
5. 完成！✓

---

## 常见问题速查

### Q: 编译失败？
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Q: 录音没声音？
设置 → 应用 → 快速录音 → 权限 → 开启"麦克风"

### Q: 发送失败？
1. 检查网络连接
2. 确认 App ID/Secret 正确
3. 确认对话 ID 正确
4. 确认飞书应用已发布

### Q: Widget 点击没反应？
1. 重新添加 Widget
2. 设置 → 电池 → 快速录音 → 关闭电池优化

---

## 完整文档

- 详细使用说明：`README.md`
- 配置指南：`CONFIG.md`
- 开发报告：`REPORT.md`

---

**就这么简单！现在你可以一键录音发送到飞书了。** 🎉
