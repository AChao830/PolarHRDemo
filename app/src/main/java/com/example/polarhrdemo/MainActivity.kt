package com.example.polarhrdemo

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl.defaultImplementation
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHrData
import java.util.UUID


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUEST_CODE = 1
    }

    private val bluetoothOnActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "Bluetooth off")
        }
    }

    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取并读取设置
        sharedPreferenceHelper = SharedPreferenceHelper(this)
        Settings.testMode = sharedPreferenceHelper.loadTestMode()
        Settings.maxHeartRate = sharedPreferenceHelper.loadMaxHeartRate()
        Settings.restHeartRate = sharedPreferenceHelper.loadRestHeartRate()
        Settings.gender = sharedPreferenceHelper.loadGender()
        Settings.zone1 = sharedPreferenceHelper.loadZone1()
        Settings.zone2 = sharedPreferenceHelper.loadZone2()
        Settings.zone3 = sharedPreferenceHelper.loadZone3()
        Settings.zone4 = sharedPreferenceHelper.loadZone4()
        Settings.zone5 = sharedPreferenceHelper.loadZone5()
        Settings.showHR = sharedPreferenceHelper.loadShowHR()
        Settings.showHeartRate = sharedPreferenceHelper.loadShowHeartRate()
        Settings.showHRPercentage = sharedPreferenceHelper.loadShowHRPercentage()
        Settings.showHRZone = sharedPreferenceHelper.loadShowHRZone()
        Settings.showHRV = sharedPreferenceHelper.loadShowHRV()
        Settings.showSDRR = sharedPreferenceHelper.loadShowSDRR()
        Settings.showpNN50 = sharedPreferenceHelper.loadShowpNN50()
        Settings.showRMSSD = sharedPreferenceHelper.loadShowRMSSD()
        Settings.showTRIMP = sharedPreferenceHelper.loadShowTRIMP()
        Settings.showBanister = sharedPreferenceHelper.loadShowBanister()
        Settings.showEdward = sharedPreferenceHelper.loadShowEdward()
        Settings.showLucia = sharedPreferenceHelper.loadShowLucia()
        Settings.showStango = sharedPreferenceHelper.loadShowStango()
        Settings.showCustom = sharedPreferenceHelper.loadShowCustom()
        Settings.LT1 = sharedPreferenceHelper.loadLT1()
        Settings.LT2 = sharedPreferenceHelper.loadLT2()
        Settings.zone0Coefficient = sharedPreferenceHelper.loadZoneCoefficient("zone0Coefficient")
        Settings.zone1Coefficient = sharedPreferenceHelper.loadZoneCoefficient("zone1Coefficient")
        Settings.zone2Coefficient = sharedPreferenceHelper.loadZoneCoefficient("zone2Coefficient")
        Settings.zone3Coefficient = sharedPreferenceHelper.loadZoneCoefficient("zone3Coefficient")
        Settings.zone4Coefficient = sharedPreferenceHelper.loadZoneCoefficient("zone4Coefficient")
        Settings.zone5Coefficient = sharedPreferenceHelper.loadZoneCoefficient("zone5Coefficient")
        Settings.zone6Coefficient = sharedPreferenceHelper.loadZoneCoefficient("zone6Coefficient")

        // 获取Player
        Settings.Player1 = sharedPreferenceHelper.loadPlayer("Player1")?: ""
        Settings.Player2 = sharedPreferenceHelper.loadPlayer("Player2")?: ""
        Settings.Player3 = sharedPreferenceHelper.loadPlayer("Player3")?: ""
        Settings.Player4 = sharedPreferenceHelper.loadPlayer("Player4")?: ""
        Settings.Player5 = sharedPreferenceHelper.loadPlayer("Player5")?: ""
        Settings.Player6 = sharedPreferenceHelper.loadPlayer("Player6")?: ""
        Settings.Player7 = sharedPreferenceHelper.loadPlayer("Player7")?: ""
        Settings.Player8 = sharedPreferenceHelper.loadPlayer("Player8")?: ""


        // 检查蓝牙连接
        checkBT()

        val hrConnectButton: Button = findViewById(R.id.buttonHr)
        hrConnectButton.setOnClickListener { onClickConnectHr(it) }

        val settingsButton: Button = findViewById(R.id.buttonSettings)
        settingsButton.setOnClickListener { onClickButtonSettings(it) }

        val scanButton: Button = findViewById(R.id.buttonScan)
        scanButton.setOnClickListener { onClickButtonScan(it) }

        val playerButton: Button = findViewById(R.id.buttonPlayer)
        playerButton.setOnClickListener { onClickButtonPlayer(it) }
    }

    fun onClickButtonTest(view: View) {
        // showToast("This is a test button and does nothing")
        val intent = Intent(this, TestActivity::class.java)
        startActivity(intent)
    }

    // 跳转至HR页面
    private fun onClickConnectHr(view: View) {
        // 利用intent还可以传进去数据
        val intent = Intent(this, HRActivity::class.java)
        startActivity(intent)
    }

    // 跳转至Settings页面
    private fun onClickButtonSettings(view: View) {
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
    }

    // 跳转至扫描页面
    private fun onClickButtonScan(view: View) {
        val intent = Intent(this, ScanActivity::class.java)
        startActivity(intent)
    }

    // 跳转至Player页面
    private fun onClickButtonPlayer(view: View) {
        val intent = Intent(this, PlayerActivity::class.java)
        startActivity(intent)
    }

    // 检查蓝牙连接
    private fun checkBT() {
        val btManager = applicationContext.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = btManager.adapter
        if (bluetoothAdapter == null) {
            showToast("Device doesn't support Bluetooth")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothOnActivityResultLauncher.launch(enableBtIntent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), PERMISSION_REQUEST_CODE)
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
        }
    }

    // 处理权限请求结果
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (index in 0..grantResults.lastIndex) {
                if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                    Log.w(TAG, "Needed permissions are missing")
                    showToast("Needed permissions are missing")
                    return
                }
            }
            Log.d(TAG, "Needed permissions are granted")
        }
    }

    // 底部提示信息条
    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
    }
}