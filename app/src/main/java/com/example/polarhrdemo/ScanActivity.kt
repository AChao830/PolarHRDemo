package com.example.polarhrdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidplot.util.PixelUtils
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHrData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import java.util.UUID

class ScanActivity: AppCompatActivity() {
    companion object {
        private const val TAG = "ScanActivity"
    }

    private val scanDeviceList = mutableListOf<String>()
    private var testUtils = TestUtils()

    private val api: PolarBleApi by lazy {
        // Notice all features are enabled
        PolarBleApiDefaultImpl.defaultImplementation(
            applicationContext,
            setOf(
                PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
                PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_H10_EXERCISE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP,
                PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
            )
        )
    }
    private var scanDisposable: Disposable? = null

    private lateinit var scanButton: Button
    private lateinit var recyclerViewScanDevice: RecyclerView
    private lateinit var scanDeviceInfoAdapter: ScanDeviceInfoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        scanButton = findViewById(R.id.buttonScanDevices)

        api.setPolarFilter(false)
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun blePowerStateChanged(powered: Boolean) {
                Log.d(TAG, "BLE power: $powered")
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTED: ${polarDeviceInfo.deviceId}")
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTING: ${polarDeviceInfo.deviceId}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "DISCONNECTED: ${polarDeviceInfo.deviceId}")
            }

            override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
                Log.d(TAG, "DIS INFO uuid: $uuid value: $value")
            }

            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d(TAG, "BATTERY LEVEL: $level")
            }

            override fun hrNotificationReceived(identifier: String, data: PolarHrData.PolarHrSample) {
                // deprecated
            }
        })

        scanButton.setOnClickListener {
            onClickScanButton(it)
        }

        recyclerViewScanDevice = findViewById(R.id.recyclerViewScanDevices)
        recyclerViewScanDevice.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        scanDeviceInfoAdapter = ScanDeviceInfoAdapter(scanDeviceList)
        recyclerViewScanDevice.adapter = scanDeviceInfoAdapter

        PixelUtils.init(this)
    }

    private fun onClickScanButton(view: View) {
        val isDisposed = scanDisposable?.isDisposed ?: true
        if (isDisposed) {
            scanDisposable = api.searchForDevice()
            /*val mockDeviceInfoObservable = testUtils.createMockDeviceINfoObservable()
            scanDisposable = mockDeviceInfoObservable*/
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { polarDeviceInfo: PolarDeviceInfo ->
                        scanDeviceList.add(polarDeviceInfo.deviceId)
                        showToast("polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable)
                        Log.d(TAG, "polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable)
                        scanDeviceInfoAdapter.updateData()
                    },
                    { error: Throwable ->
                        showToast("Device scan failed. Reason $error")
                        Log.e(TAG, "Device scan failed. Reason $error")
                    },
                    {
                        showToast("complete")
                        Log.d(TAG, "complete")
                    }
                )
        } else {
            scanDisposable?.dispose()
        }
    }

    // 底部提示信息条
    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
    }
}