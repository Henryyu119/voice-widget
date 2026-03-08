#!/bin/bash

echo "=========================================="
echo "  Android 快速录音 Widget - 安装脚本"
echo "=========================================="
echo ""

# 检查 Android Studio 或 Java
if ! command -v java &> /dev/null; then
    echo "❌ 未找到 Java，请先安装 JDK 17+"
    echo "   下载地址: https://adoptium.net/"
    exit 1
fi

echo "✅ Java 已安装"
echo ""

# 检查配置
echo "📝 检查配置..."
if grep -q "YOUR_APP_ID" app/src/main/java/com/openclaw/voicewidget/FeishuClient.kt; then
    echo "⚠️  警告: 检测到默认配置，请先修改 FeishuClient.kt"
    echo ""
    echo "需要修改的内容:"
    echo "  - APP_ID: 你的飞书 App ID"
    echo "  - APP_SECRET: 你的飞书 App Secret"
    echo "  - CHAT_ID: 目标对话 ID"
    echo ""
    read -p "是否已完成配置？(y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "请先完成配置，然后重新运行此脚本"
        exit 1
    fi
fi

echo "✅ 配置检查完成"
echo ""

# 编译
echo "🔨 开始编译..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

echo "✅ 编译成功"
echo ""

# 检查设备
echo "📱 检查 Android 设备..."
if ! command -v adb &> /dev/null; then
    echo "⚠️  未找到 adb，请手动安装 APK"
    echo "   APK 位置: app/build/outputs/apk/debug/app-debug.apk"
    exit 0
fi

DEVICES=$(adb devices | grep -v "List" | grep "device$" | wc -l)
if [ $DEVICES -eq 0 ]; then
    echo "⚠️  未检测到 Android 设备"
    echo "   APK 位置: app/build/outputs/apk/debug/app-debug.apk"
    echo "   请手动传输到手机并安装"
    exit 0
fi

echo "✅ 检测到 Android 设备"
echo ""

# 安装
echo "📲 安装到设备..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "  ✅ 安装成功！"
    echo "=========================================="
    echo ""
    echo "下一步:"
    echo "  1. 打开应用，授予录音权限"
    echo "  2. 长按桌面 → 小部件 → 快速录音"
    echo "  3. 拖动到桌面"
    echo "  4. 点击开始录音！"
    echo ""
else
    echo "❌ 安装失败"
    exit 1
fi
