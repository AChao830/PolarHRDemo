package com.example.polarhrdemo

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
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
import kotlin.math.sqrt
import kotlin.math.exp

class PolarDevice (val groupId:String, val deviceId: String, private val context: Context, private val testMode:Boolean) {
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
    private var latestHRZone = "0" // 储存最新的心率分位点，用于数据的展示
    private var latestSDRR = "0"
    private var latestpNN50 = "0"
    private var latestRMSSD = "0"
    private var latestBanistersTRIMP = "0"
    private var latestEdwardsTRIMP = "0"
    private var latestLuciasTRIMP = "0"
    private var latestStangosTRIMP = "0"
    lateinit var fwVersion: String // 储存fw版本
    private var battery: String = "0" // 储存电量

    private var rrList = mutableListOf<Double>() // 列表储存RR数据
    private var hrList = mutableListOf<Double>() // 列表储存HR数据
    private var hrPercentageList = mutableListOf<Double>() // 列表储存HR百分比数据

    private var updateCallback: UpdateCallback? = null
    private var testUtils = TestUtils()

    val plotter = HRPlotter()

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
        val hrZone: Int = 0, // 心率与最大心率的分位点
        val SDRR: Double?, // SDRR
        val pNN50: Double?, // pNN50
        val RMSSD: Double?, // RMSSD
        val BanistersTRIMP: Double,
        val EdwardsTRIMP: Double,
        val LuciasTRIMP: Double,
        val StangosTRIMP: Double,
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

    fun setPlotterListener(callback: UpdateCallback) {
        plotter.setListener(callback)
    }

    // 添加数据，如果在区间内就把区间也纯存进来，否则为null
    private fun addValue(polarData: PolarHrData.PolarHrSample) {
        // 时间戳
        val currentDateTime = LocalDateTime.now() // 获取时间
        val formattedDateTime = currentDateTime.format(formatter) // 格式化时间

        val newHrPercentage: Float = (polarData.hr.toFloat() / Settings.maxHeartRate.toFloat()) * 100
        var newHRZone = 0
        if (newHrPercentage <= Settings.zone0) {
            newHRZone = 0
        } else if (newHrPercentage <= Settings.zone1) {
            newHRZone = 0
        } else if (newHrPercentage <= Settings.zone2) {
            newHRZone = 1
        } else if (newHrPercentage <= Settings.zone3) {
            newHRZone = 2
        } else if (newHrPercentage <= Settings.zone4) {
            newHRZone = 3
        } else if (newHrPercentage <= Settings.zone5) {
            newHRZone = 4
        } else if (newHrPercentage <= Settings.zone6) {
            newHRZone = 5
        } else {
            newHRZone = 6
        }

        var newSDRR: Double = -1.0
        var newpNN50: Double = -1.0
        var newRMSSD: Double = -1.0
        if (polarData.rrAvailable) {
            // 先判断有没有
            rrAvailable = true
            for (rr in polarData.rrsMs) {
                rrList.add(rr.toDouble())
            }
        }
        if (rrList.size >= 2) {
            newSDRR = calculateSDRR(rrList)
            newpNN50 = calculatepNN50(rrList)
            newRMSSD = calculateRMSSD(rrList)
            latestSDRR = "%.2f".format(newSDRR)
            latestpNN50 = "%.2f".format(newpNN50) + "%"
            latestRMSSD = "%.2f".format(newRMSSD)
        }
        val newBanistersTRIMP: Double = calculateTRIMP("Banisters", Settings.sex)
        val newEdwardsTRIMP: Double = calculateTRIMP("Edwards", Settings.sex)
        val newLuciasTRIMP: Double = calculateTRIMP("Lucias", Settings.sex)
        val newStangosTRIMP: Double = calculateTRIMP("Stangos", Settings.sex)
        // 更新数据
        latestHeartRate = polarData.hr.toString()
        latestHRPercentage = "%.2f".format(newHrPercentage) + "%"
        latestHRZone = newHRZone.toString()
        latestBanistersTRIMP = "%.2f".format(newBanistersTRIMP)
        latestEdwardsTRIMP = "%.2f".format(newEdwardsTRIMP)
        latestLuciasTRIMP = "%.2f".format(newLuciasTRIMP)
        latestStangosTRIMP = "%.2f".format(newStangosTRIMP)
        if (isRecord) {
            val newSDRRValue = if (rrAvailable) newSDRR else null
            val newpNN50Value = if (rrAvailable) newpNN50 else null
            val newRMSSDValue = if (rrAvailable) newRMSSD else null
            val newPeriodValue = if (isPeriod) period else null
            val tempData = DeviceData(
                formattedDateTime,
                polarData.hr,
                newHrPercentage,
                newHRZone,
                newSDRRValue,
                newpNN50Value,
                newRMSSDValue,
                newBanistersTRIMP,
                newEdwardsTRIMP,
                newLuciasTRIMP,
                newStangosTRIMP,
                newPeriodValue
            )
            deviceDataList.add(0, tempData)
        }
        hrList.add(0, polarData.hr.toDouble())
        hrPercentageList.add(0, newHrPercentage.toDouble())
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
    fun getLatestHRZone(): String {
        return latestHRZone
    }
    // 提供给recycleView使用，获取最新的SDRR
    fun getLatestSDRR(): String {
        return latestSDRR
    }

    // 提供给recycleView使用，获取最新的pNN50
    fun getLatestpNN50(): String {
        return latestpNN50
    }

    // 提供给recycleView使用，获取最新的RMSSD
    fun getLatestRMSSD(): String {
        return latestRMSSD
    }

    // 提供给recycleView使用，获取最新的电池电量
    fun getLatestBattery(): String {
        return battery
    }

    // 提供给recycleView使用，获取最新的BanistersTRIMP
    fun getLatestBanistersTRIMP(): String {
        return latestBanistersTRIMP
    }

    // 提供给recycleView使用，获取最新的BanistersTRIMP
    fun getLatestEdwardsTRIMP(): String {
        return latestEdwardsTRIMP
    }

    // 提供给recycleView使用，获取最新的BanistersTRIMP
    fun getLatestLuciasTRIMP(): String {
        return latestLuciasTRIMP
    }

    // 提供给recycleView使用，获取最新的BanistersTRIMP
    fun getLatestStangosTRIMP(): String {
        return latestStangosTRIMP
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
                                plotter.addValues(sample)
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
                                plotter.addValues(sample)
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
                "Timestamp,HeartRate,HeartRatePercentage,HeartRateZone,SDRR,pNN50,RMSSD," +
                "BanistersTRIMP,EdwardsTRIMP,LuciasTRIMP,StangosTRIMP,Period\n"
        val csvContent = StringBuilder(csvHeader)
        // CSV内容
        for (data in deviceDataList.reversed()) {
            val periodValue = data.period?.toString() ?: "" // 处理空值
            val csvLine = "${data.timestamp},${data.hr},${"%.2f".format(data.hrPercentage)}," +
                    "${data.hrZone},${"%.2f".format(data.SDRR)},${"%.2f".format(data.pNN50)}," +
                    "${"%.2f".format(data.RMSSD)},${"%.2f".format(data.BanistersTRIMP)}," +
                    "${"%.2f".format(data.EdwardsTRIMP)},${"%.2f".format(data.LuciasTRIMP)}," +
                    "${"%.2f".format(data.StangosTRIMP)},$periodValue\n"
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
            sheet.addCell(Label(3, 1, "HeartRateZone"))
            sheet.addCell(Label(4, 1, "SDRR"))
            sheet.addCell(Label(5, 1, "pNN50"))
            sheet.addCell(Label(6, 1, "RMSSD"))
            sheet.addCell(Label(7, 1, "BanistersTRIMP"))
            sheet.addCell(Label(8, 1, "EdwardsTRIMP"))
            sheet.addCell(Label(9, 1, "LuciasTRIMP"))
            sheet.addCell(Label(10, 1, "StangosTRIMP"))
            sheet.addCell(Label(11, 1, "Period"))

            // Add data rows
            for ((index, data) in deviceDataList.reversed().withIndex()) {
                sheet.addCell(Label(0, index + 2, data.timestamp.toString()))
                sheet.addCell(Label(1, index + 2, data.hr.toString()))
                sheet.addCell(Label(2, index + 2, "%.2f".format(data.hrPercentage)))
                sheet.addCell(Label(3, index + 2, data.hrZone.toString()))
                sheet.addCell(Label(4, index + 2, "%.2f".format(data.SDRR)))
                sheet.addCell(Label(5, index + 2, "%.2f".format(data.pNN50)))
                sheet.addCell(Label(6, index + 2, "%.2f".format(data.RMSSD)))
                sheet.addCell(Label(7, index + 2, "%.2f".format(data.BanistersTRIMP)))
                sheet.addCell(Label(8, index + 2, "%.2f".format(data.EdwardsTRIMP)))
                sheet.addCell(Label(9, index + 2, "%.2f".format(data.LuciasTRIMP)))
                sheet.addCell(Label(10, index + 2, "%.2f".format(data.StangosTRIMP)))
                sheet.addCell(Label(11, index + 2, data.period?.toString()))
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

    // 计算SDRR
    private fun calculateSDRR(numbers: List<Double>): Double {
        val mean = numbers.average()
        val squaredDifferences = numbers.map { (it - mean) * (it - mean) }
        val squaredSum = squaredDifferences.sum()
        val variance = squaredSum / numbers.size
        return sqrt(variance)
    }

    // 计算pNN50
    private fun calculatepNN50(numbers: List<Double>): Double {
        val differences = numbers.zipWithNext { a, b -> b - a }
        val n = differences.count{ it > 50.0 }
        return (n.toDouble() / differences.size) * 100
    }

    // 计算RMSSD
    private fun calculateRMSSD(numbers: List<Double>): Double {
        val differences = numbers.zipWithNext { a, b -> b - a }
        val mean = differences.average()
        val squaredDifferences = differences.map { (it - mean) * (it - mean) }
        val squaredSum = squaredDifferences.sum()
        val variance = squaredSum / differences.size
        return sqrt(variance)
    }

    // 计算TRIMP
    private fun calculateTRIMP(method: String, sex: String): Double {
        val t:Double = 1.0/60.0
        if (method == "Banisters") {
            val deltaHr = (hrList.average() - Settings.restHeartRate) / (Settings.maxHeartRate - Settings.restHeartRate)
            return if (sex == "Male") {
                val y = 0.64 * exp(1.92) * deltaHr
                t*hrList.size*deltaHr*y
            } else {
                val y = 0.86 * exp(1.67) * deltaHr
                t*hrList.size*deltaHr*y
            }
        } else if (method == "Edwards") {
            return (1.0*t*hrPercentageList.count { it >= 50 && it < 60 }) +
                    (2.0*t*hrPercentageList.count { it >= 60 && it < 70 }) +
                    (3.0*t*hrPercentageList.count { it >= 70 && it < 80 }) +
                    (4.0*t*hrPercentageList.count { it >= 80 && it < 90 }) +
                    (5.0*t*hrPercentageList.count { it >= 90})

        } else if (method == "Lucias") {
            return (1.0*t*hrPercentageList.count { it < Settings.LT1}) +
                    (2.0*t*hrPercentageList.count { it >= Settings.LT1 && it <= Settings.LT2 }) +
                    (3.0*t*hrPercentageList.count { it >Settings.LT2 })
        } else if (method == "Stangos") {
            return (1.25*t*hrPercentageList.count { it > 65 && it <= 71 }) +
                    (1.71*t*hrPercentageList.count { it > 71 && it <= 78 }) +
                    (2.54*t*hrPercentageList.count { it > 78 && it <= 85 }) +
                    (3.61*t*hrPercentageList.count { it > 85 && it <= 92 }) +
                    (5.16*t*hrPercentageList.count { it > 92})
        }
        return 0.0
    }

    // 底部提示信息条
    private fun showToast(message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast.show()
    }

}