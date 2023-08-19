package com.example.polarhrdemo

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl.defaultImplementation
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHrData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.UUID
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import jxl.Workbook
import jxl.write.Label
import jxl.write.WritableSheet
import jxl.write.WritableWorkbook

class PolarDevice (val deviceId: String, private val context: Context, private val testMode:Boolean) {
    /**
     * 设备类，处理大部分关于设备的事务
     * 数据的获取储存与输出
     */
    companion object {
        private const val TAG = "PolarDevice"
    }

    var period = 1 // 区间初始，从1开始
    var isRecord = false // 控制目前是否处于录制内
    var isPeriod = false // 控制目前是否处于区间内
    var rrAvailable = false
    private var deviceDataList = mutableListOf<DeviceData>() // 列表储存数据
    private var latestHeartRate = "0" // 储存最新的心率，用于数据的展示
    private var latestHRPercentage = "0" // 储存最新的心率百分比，用于数据的展示
    private var latestHRQuantile = "0" // 储存最新的心率分位点，用于数据的展示
    private var latestHRV = "0"
    lateinit var fwVersion: String // 储存fw版本
    lateinit var battery: String // 储存电量

    private var rrList = mutableListOf<Double>() // 列表储存RR数据

    private var updateCallback: UpdateCallback? = null
    private var testUtils = TestUtils()

    // 定义时间格式
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    // 初始化polar api
    private var api: PolarBleApi = defaultImplementation(
        context,
        setOf(
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
            PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
        )
    )
    private var hrDisposable: Disposable? = null

    // 设备数据类，用以储存设备的数据
    data class DeviceData (
        val timestamp: String, // 时间戳
        val hr: Int, // 心率
        val hrPercentage: Float = 0.0F, // 储存心率离最大心率百分比
        val hrQuantile: Int = 0, // 心率与最大心率的分位点
        val HRV: Double?, // HRV
        val period: Int? // 区间
    )

    init {
        // 设置polar api的callback函数
        api.setApiLogger { str: String -> Log.d("SDK", str) }
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun blePowerStateChanged(powered: Boolean) {
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Toast.makeText(context, "Device:${deviceId} connected!", Toast.LENGTH_SHORT).show()
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

    fun setUpdateCallback(callback: UpdateCallback) {
        updateCallback = callback
    }

    // 添加数据，如果在区间内就把区间也纯存进来，否则为null
    private fun addValue(polarData: PolarHrData.PolarHrSample) {
        // 时间戳
        val currentDateTime = LocalDateTime.now() // 获取时间
        val formattedDateTime = currentDateTime.format(formatter) // 格式化时间

        val newHrPercentage: Float = (polarData.hr.toFloat() / Settings.maxHeartRate.toFloat()) * 100
        val newHrQuantile: Int = if (newHrPercentage > 100 ) { 6 } else { ((newHrPercentage / 20) + 1).toInt() }
        var newHRV: Double = -1.0
        if (polarData.rrAvailable) {
            // 先判断有没有
            rrAvailable = true
            for (rr in polarData.rrsMs) {
                rrList.add(rr.toDouble())
            }
        }
        if (rrList.size >= 2) {
            newHRV = calculateVariance(rrList)
            latestHRV = "%.2f".format(newHRV)
        }
        // 更新数据
        latestHeartRate = polarData.hr.toString()
        latestHRPercentage = "%.1f".format(newHrPercentage) + "%"
        latestHRQuantile = newHrQuantile.toString()
        if (isRecord) {
            val newHRVValue = if (rrAvailable) newHRV else null
            val newPeriodValue = if (isPeriod) period else null
            val tempData = DeviceData(
                formattedDateTime,
                polarData.hr,
                newHrPercentage,
                newHrQuantile,
                newHRVValue,
                newPeriodValue
            )
            deviceDataList.add(0, tempData)
        }
    }

    // 连接设备
    fun connectToDevice() {
        if (testMode) {
            // 测试模式下直接streamHR
            streamHR()
        } else {
            // 尝试连接
            try {
                api.connectToDevice(deviceId)
            } catch (a: PolarInvalidArgument) {
                a.printStackTrace()
                Toast.makeText(context, R.string.connection_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 开始录制
    fun startRecord() {
        isRecord = true
    }

    // 结束录制
    fun stopRecord() {
        isRecord = false
    }

    // 开始区间
    fun startPeriod() {
        isPeriod = true
    }

    // 结束区间
    fun endPeriod() {
        isPeriod = false
        period++ // 记得加一，使得下一个区间与前一个区间有所区分
    }

    // 提供给recycleView使用，获取最新的心率
    fun getLatestHeartRate(): String {
        return latestHeartRate
    }
    // 提供给recycleView使用，获取最新的心率
    fun getLatestHRPercentage(): String {
        return latestHRPercentage
    }
    // 提供给recycleView使用，获取最新的心率
    fun getLatestHRQuantile(): String {
        return latestHRQuantile
    }
    // 提供给recycleView使用，获取最新的HRV(SDNN方法)
    fun getLatestHRV(): String {
        return latestHRV
    }

    // 开始监听
    private fun streamHR() {
        val isDisposed = hrDisposable?.isDisposed ?: true
        if (isDisposed) {
            // 判断是否在测试模式
            if(testMode) {
                // 测试模式下不挂钩到api上，而是利用伪数据生成器
                val mockHRDataObservable = testUtils.createMockHRDataObservable()
                hrDisposable = mockHRDataObservable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { hrData: PolarHrData ->
                            for (sample in hrData.samples) {
                                addValue(sample)
                                updateCallback?.updateDeviceInfo()
                            }
                        },
                        { error: Throwable ->
                            hrDisposable = null
                        }
                    )
            } else {
                // 实际模式
                hrDisposable = api.startHrStreaming(deviceId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { hrData: PolarHrData ->
                            for (sample in hrData.samples) {
                                addValue(sample)
                                updateCallback?.updateDeviceInfo()
                            }
                        },
                        { error: Throwable ->
                            hrDisposable = null
                        }
                    )
            }
        } else {
            // NOTE stops streaming if it is "running"
            hrDisposable?.dispose()
            hrDisposable = null
        }
    }

    // 关闭设备连接
    fun disconnectDevice(){
        api.shutDown() // 关闭api
        hrDisposable?.dispose() // 关闭订阅
    }

    // 导出数据到外部储存目录，CSV格式
    // 两个参数，一个是applicationContext，另一个是文件名称
    fun exportDataToCSV(groupId:String, context: Context, fileName: String) {
        // CSV头
        val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val csvHeader = "ID:${deviceId},Group:${groupId},Date:${currentDateTime}\n" +
                "Timestamp,HeartRate,HeartRatePercentage,HeartRateQuantile,HeartRateVariability,Period\n"
        val csvContent = StringBuilder(csvHeader)
        // CSV内容
        for (data in deviceDataList.reversed()) {
            val periodValue = data.period?.toString() ?: "" // 处理空值
            val csvLine = "${data.timestamp},${data.hr},${"%.1f".format(data.hrPercentage)},${data.hrQuantile},${"%.2f".format(data.HRV)},$periodValue\n"
            csvContent.append(csvLine)
        }
        val csvData = csvContent.toString()
        // 检查外部存储是否可写入
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val file = File(context.getExternalFilesDir(null), fileName)

            try {
                BufferedWriter(FileWriter(file)).use { writer ->
                    writer.write(csvData)
                }
                // TODO: optional
                // 如果成功创建了文件，可以分享给其他应用程序或通知用户。
                // notifyFileCreated(context, file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // TODO
            // 处理外部存储不可用的情况。
            // 可以考虑其他选项，比如内部存储或云存储。
        }
    }

    // 导出数据到外部储存目录，CSV格式
    // 两个参数，一个是applicationContext，另一个是文件名称
    fun exportDataToExcel(groupId:String, context: Context, fileName: String) {
        val file = File(context.getExternalFilesDir(null), fileName)
        try {
            val writableWorkbook: WritableWorkbook = Workbook.createWorkbook(file)
            val sheet: WritableSheet = writableWorkbook.createSheet("DataSheet", 0)

            // Add head info
            val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            sheet.addCell(Label(0, 0, "DeviceID"))
            sheet.addCell(Label(1, 0, deviceId))
            sheet.addCell(Label(2, 0, "GroupID"))
            sheet.addCell(Label(3, 0, groupId))
            sheet.addCell(Label(4, 0, "Date"))
            sheet.addCell(Label(5, 0, currentDateTime))

            // Add headers
            sheet.addCell(Label(0, 1, "Timestamp"))
            sheet.addCell(Label(1, 1, "HeartRate"))
            sheet.addCell(Label(2, 1, "HeartRatePercentage"))
            sheet.addCell(Label(3, 1, "HeartRateQuantile"))
            sheet.addCell(Label(4, 1, "HeartRateVariability"))
            sheet.addCell(Label(5, 1, "Period"))

            // Add data rows
            for ((index, data) in deviceDataList.reversed().withIndex()) {
                sheet.addCell(Label(0, index + 2, data.timestamp.toString()))
                sheet.addCell(Label(1, index + 2, data.hr.toString()))
                sheet.addCell(Label(2, index + 2, "%.1f".format(data.hrPercentage)))
                sheet.addCell(Label(3, index + 2, data.hrQuantile.toString()))
                sheet.addCell(Label(4, index + 2, "%.2f".format(data.HRV)))
                sheet.addCell(Label(5, index + 2, data.period?.toString()))
            }

            // Write and close the workbook
            writableWorkbook.write()
            writableWorkbook.close()

            // TODO: optional
            // You can notify the user or share the file with other apps here.
            // notifyFileCreated(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 计算方差
    private fun calculateVariance(numbers: List<Double>): Double {
        val mean = numbers.average()
        val squaredDifferences = numbers.map { (it - mean) * (it - mean) }
        return squaredDifferences.average()
    }

}