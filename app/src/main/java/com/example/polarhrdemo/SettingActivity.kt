package com.example.polarhrdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl.defaultImplementation
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHrData
import java.util.UUID

class SettingActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "SettingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val testModeSwitch: SwitchCompat = findViewById(R.id.switchTestMode)
        testModeSwitch.isChecked = Settings.testMode // 初始化开关的状态
        testModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeTestModeSwitch(isChecked)
        }
    }

    // 处理test mode开关事务逻辑
    private fun onCheckedChangeTestModeSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.testMode = true
            showToast("Test Mode is on")
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.testMode = false
            showToast("Test Mode is off")
        }
    }

    // 底部提示信息条
    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
    }

}