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
import android.widget.TextView
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

    private lateinit var textViewMaxHR: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val testModeSwitch: SwitchCompat = findViewById(R.id.switchTestMode)
        testModeSwitch.isChecked = Settings.testMode // 初始化开关的状态
        testModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeTestModeSwitch(isChecked)
        }

        textViewMaxHR = findViewById(R.id.textViewMaxHeartRate)
        textViewMaxHR.text = "MAX HEART RATE: ${Settings.maxHeartRate}"

        val buttonChangeMaxHR: Button = findViewById(R.id.buttonChangeMaxHeartRate)
        buttonChangeMaxHR.setOnClickListener {
            onClickChangeMaxHRButton(it)
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

    private fun onClickChangeMaxHRButton(view: View){
        showChangeMaxHRDialog(view)
    }

    private fun showChangeMaxHRDialog(view: View){
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new max heart rate")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_device_id)
        input.setText(Settings.maxHeartRate.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newMaxHR = input.text.toString().toIntOrNull()
            if (newMaxHR != null) {
                Settings.maxHeartRate = newMaxHR
                textViewMaxHR.text = "MAX HEART RATE: ${Settings.maxHeartRate}"
                showToast("Changed max heart rate to $newMaxHR")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 底部提示信息条
    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
    }

}