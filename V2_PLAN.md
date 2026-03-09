# VoiceWidget V2 执行计划（2026-03-09）

## 最终方向

### Widget A：快速文字记录 📝
- 点击 Widget 打开输入页
- 支持手动打字 / 系统输入法语音
- 发送到飞书专用群 Webhook
- 依赖用户提供 Webhook URL

### Widget B：语音日记 🎤
- 点击 Widget 开始录音
- 再次点击停止
- 上传到 VPS：`http://43.163.97.77:3002/api/diary/voice`
- VPS 使用讯飞云 ASR 转写
- 文本存入日记数据库
- 音频保留 7 天后删除

## 当前状态
- 已有 Android 基础项目
- 已有录音上传原型
- 已有 Widget 基础资源
- 已有部分“本地识别/飞书 Bot”实验代码：不再作为主路线

## 开发优先级

### Phase 1：语音日记（主线）
1. 清理/收口实验性本地识别代码，不作为主流程
2. 完善录音 Widget（独立 Widget B）
3. 完善录音 Service：开始/停止/状态同步
4. 上传音频到 VPS
5. VPS 接口联调
6. 真机测试

### Phase 2：快速文字记录
1. 新增独立 Widget A
2. 新增极简输入页
3. 支持系统输入法语音输入
4. 接入飞书群 Webhook
5. 真机测试

### Phase 3：VPS 后端
1. 讯飞云 ASR 接入
2. 日记数据库入库
3. 音频保留与清理（7 天）
4. 日志与失败重试

## 待用户提供
- 飞书群 Webhook URL（用于 Widget A）
- 讯飞 AppID / API Key / API Secret（用于 VPS 语音转写）
- Android 设备型号 / 系统版本（用于兼容性验证）

## 备注
- 飞书 Bot API 直发私聊：暂停，不作为当前版本主路线
- 本地 SpeechRecognizer：暂停，不作为当前版本主路线
- 若后续需要，可保留为实验分支
