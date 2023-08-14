package com.example.polarhrdemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHrData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import java.util.UUID
import android.content.SharedPreferences


interface TestUpdateCallback {
    fun updatePage()
}

class TestActivity : AppCompatActivity(), TestUpdateCallback {
    companion object {
        private const val TAG = "TestActivity"
    }

    private lateinit var textViewHR: TextView
    private lateinit var textViewDeviceId: TextView
    private lateinit var textViewBattery: TextView
    private lateinit var textViewFwVersion: TextView
    private lateinit var testPolarDevice: TestPolarDevice

    private var deviceId: String = "B7215226"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        textViewHR = findViewById(R.id.test_view_hr)
        textViewDeviceId = findViewById(R.id.test_view_deviceId)
        textViewBattery = findViewById(R.id.test_view_battery_level)
        textViewFwVersion = findViewById(R.id.test_view_fw_version)

        testPolarDevice = TestPolarDevice(deviceId, applicationContext)
        testPolarDevice.setTestUpdateCallback(this)
        testPolarDevice.connectToDevice()
    }

    public override fun onDestroy() {
        super.onDestroy()
        testPolarDevice.shutdownAPI()
    }

    override fun updatePage() {
        textViewDeviceId.text = deviceId
        textViewHR.text = testPolarDevice.latestHR
        textViewFwVersion.text = testPolarDevice.fwVersion
        textViewBattery.text = testPolarDevice.battery
    }
}

class TestPolarDevice (private val deviceId: String, private val context: Context) {
    private var api: PolarBleApi = PolarBleApiDefaultImpl.defaultImplementation(
        context,
        setOf(
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
            PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
        )
    )
    private var hrDisposable: Disposable? = null
    private var testUpdateCallback: TestUpdateCallback? = null

    var latestHR = "0"
    lateinit var fwVersion: String
    lateinit var battery: String

    init{
        api.setApiLogger { str: String -> Log.d("SDK", str) }
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun blePowerStateChanged(powered: Boolean) {
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Toast.makeText(context, R.string.connected, Toast.LENGTH_SHORT).show()
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
            }

            override fun bleSdkFeatureReady(identifier: String, feature: PolarBleApi.PolarBleSdkFeature) {

                when (feature) {
                    PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING -> {
                        streamHR()
                    }
                    else -> {}
                }
            }

            override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
                if (uuid == UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")) {
                    val msg = "Firmware: " + value.trim { it <= ' ' }
                    fwVersion = msg.trimIndent()
                }
            }

            override fun batteryLevelReceived(identifier: String, level: Int) {
                val batteryLevelText = "Battery level: $level%"
                battery = batteryLevelText
            }

            override fun hrNotificationReceived(identifier: String, data: PolarHrData.PolarHrSample) {
                //deprecated
            }

            override fun polarFtpFeatureReady(identifier: String) {
                //deprecated
            }

            override fun streamingFeaturesReady(identifier: String, features: Set<PolarBleApi.PolarDeviceDataType>) {
                //deprecated
            }

            override fun hrFeatureReady(identifier: String) {
                //deprecated
            }
        })
    }

    fun setTestUpdateCallback(callback: TestUpdateCallback) {
        testUpdateCallback = callback
    }

    fun connectToDevice() {
        try {
            api.connectToDevice(deviceId)
        } catch (a: PolarInvalidArgument) {
            a.printStackTrace()
        }
    }

    fun streamHR(){
        val isDisposed = hrDisposable?.isDisposed ?: true
        if (isDisposed) {
            hrDisposable = api.startHrStreaming(deviceId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { hrData: PolarHrData ->
                        for (sample in hrData.samples) {
                            latestHR = sample.hr.toString()
                            testUpdateCallback?.updatePage()
                        }
                    },
                    { error: Throwable ->
                        hrDisposable = null
                    }
                )
        } else {
            // NOTE stops streaming if it is "running"
            hrDisposable?.dispose()
            hrDisposable = null
        }
    }

    fun shutdownAPI(){
        api.shutDown()
    }
}