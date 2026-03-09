package com.openclaw.voicewidget

import java.io.File

class VoiceRepository {
    private val feishuClient = FeishuClient()

    fun sendRecognizedText(text: String): Result<Unit> {
        return feishuClient.sendText(text)
    }

    fun uploadVoiceBackup(audioFile: File): Boolean {
        return ServerClient().uploadVoice(audioFile)
    }
}
