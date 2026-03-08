package com.openclaw.voicewidget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()
        checkPermissions()
    }

    private fun setupUI() {
        findViewById<TextView>(R.id.tv_title).text = "快速录音 Widget"
        findViewById<TextView>(R.id.tv_description).text = 
            "请将 Widget 添加到桌面，点击即可快速录音并发送到飞书。"

        findViewById<Button>(R.id.btn_add_widget).setOnClickListener {
            showAddWidgetInstructions()
        }

        findViewById<Button>(R.id.btn_configure).setOnClickListener {
            showConfigurationDialog()
        }

        findViewById<Button>(R.id.btn_permissions).setOnClickListener {
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            Toast.makeText(this, "所有权限已授予", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            
            if (allGranted) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("需要权限")
                    .setMessage("录音功能需要麦克风权限，请在设置中授予。")
                    .setPositiveButton("去设置") { _, _ ->
                        openAppSettings()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun showAddWidgetInstructions() {
        AlertDialog.Builder(this)
            .setTitle("添加 Widget")
            .setMessage("""
                1. 长按桌面空白处
                2. 选择"小部件"或"Widget"
                3. 找到"快速录音"
                4. 拖动到桌面
                
                添加后，点击 Widget 即可开始录音！
            """.trimIndent())
            .setPositiveButton("知道了", null)
            .show()
    }

    private fun showConfigurationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_config, null)
        
        AlertDialog.Builder(this)
            .setTitle("配置飞书")
            .setView(dialogView)
            .setPositiveButton("保存") { dialog, _ ->
                val appId = dialogView.findViewById<EditText>(R.id.et_app_id).text.toString()
                val appSecret = dialogView.findViewById<EditText>(R.id.et_app_secret).text.toString()
                
                if (appId.isNotEmpty() && appSecret.isNotEmpty()) {
                    saveConfiguration(appId, appSecret)
                    Toast.makeText(this, "配置已保存", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun saveConfiguration(appId: String, appSecret: String) {
        getSharedPreferences("FeishuConfig", MODE_PRIVATE)
            .edit()
            .putString("app_id", appId)
            .putString("app_secret", appSecret)
            .apply()
    }
}
