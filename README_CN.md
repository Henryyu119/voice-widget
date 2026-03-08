# 🎤 Android 快速录音 Widget

一键录音，自动发送到飞书。告别繁琐操作，3秒完成记录。

---

## ✨ 特性

- 🎯 **一键录音** - 桌面 Widget，点击即录
- ⏱️ **实时反馈** - 显示录音时长和状态
- 🚀 **自动发送** - 录音完成自动上传飞书
- 🔒 **稳定可靠** - 前台服务保证不被杀
- 🎨 **简洁美观** - 极简设计，符合直觉

---

## 📸 效果预览

```
┌─────────────────┐
│                 │
│   🎤 录音       │
│                 │
│  点击开始录音    │
│                 │
└─────────────────┘

        ↓ 点击

┌─────────────────┐
│                 │
│   🔴 录音中     │
│                 │
│    00:05        │
│  点击停止并发送  │
└─────────────────┘

        ↓ 再次点击

┌─────────────────┐
│                 │
│   ✓ 发送成功    │
│                 │
└─────────────────┘
```

---

## 🚀 快速开始

### 1️⃣ 配置飞书（2分钟）

1. 访问 https://open.feishu.cn/app
2. 创建企业自建应用
3. 复制 App ID 和 App Secret
4. 添加权限：`im:message`、`im:file`
5. 发布应用

### 2️⃣ 修改配置（1分钟）

编辑 `app/src/main/java/com/openclaw/voicewidget/FeishuClient.kt`：

```kotlin
private const val APP_ID = "cli_xxxxx"      // 你的 App ID
private const val APP_SECRET = "xxxxx"      // 你的 App Secret
private const val CHAT_ID = "oc_xxxxx"      // 对话 ID
```

### 3️⃣ 编译安装（1分钟）

```bash
./INSTALL.sh
```

或手动编译：

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4️⃣ 使用（30秒）

1. 打开应用，授予录音权限
2. 长按桌面 → 小部件 → 快速录音
3. 点击 Widget 开始录音
4. 再次点击停止并发送

---

## 📱 使用场景

- 💰 **记账** - "买菜50元"
- ⏰ **时间记录** - "刚写了2小时文案"
- 📝 **日记** - "今天很累，但完成了视频"
- 🔔 **提醒** - "明天记得买牛奶"

---

## 🛠️ 技术栈

- **语言**: Kotlin
- **最低版本**: Android 8.0 (API 26)
- **核心库**: OkHttp, MediaRecorder, Coroutines
- **架构**: Widget + Service + API Client

---

## 📂 项目结构

```
VoiceWidget/
├── app/src/main/java/com/openclaw/voicewidget/
│   ├── VoiceWidget.kt          # Widget 主类
│   ├── RecordingService.kt     # 录音服务
│   ├── FeishuClient.kt         # 飞书客户端
│   └── MainActivity.kt         # 配置界面
├── README.md                   # 使用说明
├── QUICKSTART.md               # 快速上手
├── CONFIG.md                   # 配置指南
└── INSTALL.sh                  # 安装脚本
```

---

## ❓ 常见问题

### Q: 录音没声音？
**A**: 设置 → 应用 → 快速录音 → 权限 → 开启"麦克风"

### Q: 发送失败？
**A**: 
1. 检查网络连接
2. 确认 App ID/Secret 正确
3. 确认飞书应用已发布

### Q: Widget 点击没反应？
**A**: 
1. 重新添加 Widget
2. 关闭电池优化

### Q: 如何获取对话 ID？
**A**: 
1. 飞书网页版打开对话
2. 查看 URL 中的 `oc_xxxxx`

---

## 📚 完整文档

- **QUICKSTART.md** - 5分钟快速上手
- **CONFIG.md** - 详细配置和故障排查
- **REPORT.md** - 技术实现说明

---

## 🔐 权限说明

- `RECORD_AUDIO` - 录音
- `INTERNET` - 网络请求
- `FOREGROUND_SERVICE` - 前台服务
- `POST_NOTIFICATIONS` - 通知（Android 13+）

---

## 🎯 路线图

- [ ] 长按录音模式（类似微信）
- [ ] 本地语音识别
- [ ] 多对话选择
- [ ] 录音历史记录
- [ ] 自定义 Widget 样式

---

## 📄 许可证

MIT License

---

## 💬 联系方式

如有问题或建议，欢迎反馈。

---

**⚡ 现在就开始，3秒完成记录！**
