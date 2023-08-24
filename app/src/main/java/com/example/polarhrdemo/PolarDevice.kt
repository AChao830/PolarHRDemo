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
import kotlin.math.min

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
    private var latestCustomTRIMP = "0"
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
        val CustomTRIMP:Double,
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
        val newBanistersTRIMP: Double = calculateTRIMP("Banisters", Settings.gender)
        val newEdwardsTRIMP: Double = calculateTRIMP("Edwards", Settings.gender)
        val newLuciasTRIMP: Double = calculateTRIMP("Lucias", Settings.gender)
        val newStangosTRIMP: Double = calculateTRIMP("Stangos", Settings.gender)
        val newCustomTRIMP: Double = calculateTRIMP("Custom", Settings.gender)
        // 更新数据
        latestHeartRate = polarData.hr.toString()
        latestHRPercentage = "%.2f".format(newHrPercentage) + "%"
        latestHRZone = newHRZone.toString()
        latestBanistersTRIMP = "%.2f".format(newBanistersTRIMP)
        latestEdwardsTRIMP = "%.2f".format(newEdwardsTRIMP)
        latestLuciasTRIMP = "%.2f".format(newLuciasTRIMP)
        latestStangosTRIMP = "%.2f".format(newStangosTRIMP)
        latestCustomTRIMP = "%.2f".format(newCustomTRIMP)
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
                newCustomTRIMP,
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

    // 提供给recycleView使用，获取最新的EdwardsTRIMP
    fun getLatestEdwardsTRIMP(): String {
        return latestEdwardsTRIMP
    }

    // 提供给recycleView使用，获取最新的LuciasTRIMP
    fun getLatestLuciasTRIMP(): String {
        return latestLuciasTRIMP
    }

    // 提供给recycleView使用，获取最新的StangosTRIMP
    fun getLatestStangosTRIMP(): String {
        return latestStangosTRIMP
    }

    // 提供给recycleView使用，获取最新的CustomTRIMP
    fun getLatestCustomTRIMP(): String {
        return latestCustomTRIMP
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
        var csvHeader = "ID:${deviceId},Group:${groupId},Date:${currentDateTime}\n" + "Timestamp"
        if (Settings.showHR) {
            if (Settings.showHeartRate) {
                csvHeader += ",HeartRate"
            }
            if (Settings.showHRPercentage){
                csvHeader += ",HeartRatePercentage"
            }
            if (Settings.showHRZone) {
                csvHeader += ",HeartRateZone"
            }
        }
        if (Settings.showHRV) {
            if (Settings.showSDRR) {
                csvHeader += ",SDRR"
            }
            if (Settings.showpNN50) {
                csvHeader += ",pNN50"
            }
            if (Settings.showRMSSD) {
                csvHeader += ",RMSSD"
            }
        }
        if (Settings.showTRIMP) {
            if (Settings.showBanister) {
                csvHeader += ",BanistersTRIMP"
            }
            if (Settings.showEdward) {
                csvHeader += ",EdwardsTRIMP"
            }
            if (Settings.showLucia) {
                csvHeader += ",LuciasTRIMP"
            }
            if (Settings.showStango) {
                csvHeader += ",StangosTRIMP"
            }
            if (Settings.showCustom) {
                csvHeader += ",CustomTRIMP"
            }
        }
        csvHeader += ",Period\n"
        val csvContent = StringBuilder(csvHeader)
        // CSV内容
        for (data in deviceDataList.reversed()) {
            val periodValue = data.period?.toString() ?: "" // 处理空值
            var csvLine = data.timestamp
            if (Settings.showHR) {
                if (Settings.showHeartRate) {
                    csvLine += ",${data.hr}"
                }
                if (Settings.showHRPercentage){
                    csvLine += ",${"%.2f".format(data.hrPercentage)}"
                }
                if (Settings.showHRZone) {
                    csvLine += ",${data.hrZone}"
                }
            }
            if (Settings.showHRV) {
                if (Settings.showSDRR) {
                    csvLine += ",${"%.2f".format(data.SDRR)}"
                }
                if (Settings.showpNN50) {
                    csvLine += ",${"%.2f".format(data.pNN50)}"
                }
                if (Settings.showRMSSD) {
                    csvLine += ",${"%.2f".format(data.RMSSD)}"
                }
            }
            if (Settings.showTRIMP) {
                if (Settings.showBanister) {
                    csvLine += ",${"%.2f".format(data.BanistersTRIMP)}"
                }
                if (Settings.showEdward) {
                    csvLine += ",${"%.2f".format(data.EdwardsTRIMP)}"
                }
                if (Settings.showLucia) {
                    csvLine += ",${"%.2f".format(data.LuciasTRIMP)}"
                }
                if (Settings.showStango) {
                    csvLine += ",${"%.2f".format(data.StangosTRIMP)}"
                }
                if (Settings.showCustom) {
                    csvLine += ",${"%.2f".format(data.CustomTRIMP)}"
                }
            }
            csvLine += ",$periodValue\n"
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

            // Calculate Summary
            val summaryAllSession = summaryAllSession()
            val summaryAllPeriodWithGap = summaryAllPeriodWithGap()
            val summaryAllPeriodWithoutGap = summaryAllPeriodWithoutGap()


            // Add Summary
            sheet.addCell(Label(0, 2, "Summary"))
            sheet.addCell(Label(1, 2, "AllSession"))
            sheet.addCell(Label(2, 2, "AllPeriodWithGap"))
            sheet.addCell(Label(3, 2, "AllPeriodWithoutGap"))
            for (i in 1 until period) {
                sheet.addCell(Label(3+i, 2, "Period$i"))
            }
            var r = 3
            if (Settings.showHR) {
                if (Settings.showHeartRate) {
                    sheet.addCell(Label(0, r, "MaxHR"))
                    sheet.addCell(Label(1, r, summaryAllSession["maxHr"].toString()))
                    sheet.addCell(Label(2, r, summaryAllPeriodWithGap["maxHr"].toString()))
                    sheet.addCell(Label(3, r, summaryAllPeriodWithoutGap["maxHr"].toString()))
                    sheet.addCell(Label(0, r+1, "MinHR"))
                    sheet.addCell(Label(1, r+1, summaryAllSession["minHr"].toString()))
                    sheet.addCell(Label(2, r+1, summaryAllPeriodWithGap["minHr"].toString()))
                    sheet.addCell(Label(3, r+1, summaryAllPeriodWithoutGap["minHr"].toString()))
                    sheet.addCell(Label(0, r+2, "AverageHR"))
                    sheet.addCell(Label(1, r+2, summaryAllSession["averageHr"].toString()))
                    sheet.addCell(Label(2, r+2, summaryAllPeriodWithGap["averageHr"].toString()))
                    sheet.addCell(Label(3, r+2, summaryAllPeriodWithoutGap["averageHr"].toString()))
                    for (i in 1 until period) {
                        val summaryPeriod = summaryPeriod(i)
                        sheet.addCell(Label(3+i, r, summaryPeriod["maxHr"].toString()))
                        sheet.addCell(Label(3+i, r+1, summaryPeriod["minHr"].toString()))
                        sheet.addCell(Label(3+i, r+2, summaryPeriod["averageHr"].toString()))
                    }
                    r += 3
                }
            }
            if (Settings.showTRIMP) {
                if (Settings.showBanister) {
                    sheet.addCell(Label(0, r, "BanisterTRIMP increment"))
                    sheet.addCell(Label(1, r, summaryAllSession["banistersTRIMPGrowth"].toString()))
                    sheet.addCell(Label(2, r, summaryAllPeriodWithGap["banistersTRIMPGrowth"].toString()))
                    sheet.addCell(Label(3, r, summaryAllPeriodWithoutGap["banistersTRIMPGrowth"].toString()))
                    for (i in 1 until period) {
                        val summaryPeriod = summaryPeriod(i)
                        sheet.addCell(Label(3+i, r, summaryPeriod["banistersTRIMPGrowth"].toString()))
                    }
                    r += 1
                }
                if (Settings.showEdward) {
                    sheet.addCell(Label(0, r, "EdwardTRIMP increment"))
                    sheet.addCell(Label(1, r, summaryAllSession["edwardsTRIMPGrowth"].toString()))
                    sheet.addCell(Label(2, r, summaryAllPeriodWithGap["edwardsTRIMPGrowth"].toString()))
                    sheet.addCell(Label(3, r, summaryAllPeriodWithoutGap["edwardsTRIMPGrowth"].toString()))
                    for (i in 1 until period) {
                        val summaryPeriod = summaryPeriod(i)
                        sheet.addCell(Label(3+i, r, summaryPeriod["edwardsTRIMPGrowth"].toString()))
                    }
                    r += 1
                }
                if (Settings.showLucia) {
                    sheet.addCell(Label(0, r, "LuciaTRIMP increment"))
                    sheet.addCell(Label(1, r, summaryAllSession["luciasTRIMPGrowth"].toString()))
                    sheet.addCell(Label(2, r, summaryAllPeriodWithGap["luciasTRIMPGrowth"].toString()))
                    sheet.addCell(Label(3, r, summaryAllPeriodWithoutGap["luciasTRIMPGrowth"].toString()))
                    for (i in 1 until period) {
                        val summaryPeriod = summaryPeriod(i)
                        sheet.addCell(Label(3+i, r, summaryPeriod["luciasTRIMPGrowth"].toString()))
                    }
                    r += 1
                }
                if (Settings.showStango) {
                    sheet.addCell(Label(0, r, "StangoTRIMP increment"))
                    sheet.addCell(Label(1, r, summaryAllSession["stangosTRIMPGrowth"].toString()))
                    sheet.addCell(Label(2, r, summaryAllPeriodWithGap["stangosTRIMPGrowth"].toString()))
                    sheet.addCell(Label(3, r, summaryAllPeriodWithoutGap["stangosTRIMPGrowth"].toString()))
                    for (i in 1 until period) {
                        val summaryPeriod = summaryPeriod(i)
                        sheet.addCell(Label(3+i, r, summaryPeriod["stangosTRIMPGrowth"].toString()))
                    }
                    r += 1
                }
                if (Settings.showCustom) {
                    sheet.addCell(Label(0, r, "CustomTRIMP increment"))
                    sheet.addCell(Label(1, r, summaryAllSession["customTRIMPGrowth"].toString()))
                    sheet.addCell(Label(2, r, summaryAllPeriodWithGap["customTRIMPGrowth"].toString()))
                    sheet.addCell(Label(3, r, summaryAllPeriodWithoutGap["customTRIMPGrowth"].toString()))
                    for (i in 1 until period) {
                        val summaryPeriod = summaryPeriod(i)
                        sheet.addCell(Label(3+i, r, summaryPeriod["customTRIMPGrowth"].toString()))
                    }
                    r += 1
                }
            }

            // Add headers
            var c = 0
            sheet.addCell(Label(c, 12, "Timestamp"))
            c += 1
            if (Settings.showHR) {
                if (Settings.showHeartRate) {
                    sheet.addCell(Label(c, 12, "HeartRate"))
                    c += 1
                }
                if (Settings.showHRPercentage){
                    sheet.addCell(Label(c, 12, "HeartRatePercentage"))
                    c += 1
                }
                if (Settings.showHRZone) {
                    sheet.addCell(Label(c, 12, "HeartRateZone"))
                    c += 1
                }
            }
            if (Settings.showHRV) {
                if (Settings.showSDRR) {
                    sheet.addCell(Label(c, 12, "SDRR"))
                    c += 1
                }
                if (Settings.showpNN50) {
                    sheet.addCell(Label(c, 12, "pNN50"))
                    c += 1
                }
                if (Settings.showRMSSD) {
                    sheet.addCell(Label(c, 12, "RMSSD"))
                    c += 1
                }
            }
            if (Settings.showTRIMP) {
                if (Settings.showBanister) {
                    sheet.addCell(Label(c, 12, "BanistersTRIMP"))
                    c += 1
                }
                if (Settings.showEdward) {
                    sheet.addCell(Label(c, 12, "EdwardsTRIMP"))
                    c += 1
                }
                if (Settings.showLucia) {
                    sheet.addCell(Label(c, 12, "LuciasTRIMP"))
                    c += 1
                }
                if (Settings.showStango) {
                    sheet.addCell(Label(c, 12, "StangosTRIMP"))
                    c += 1
                }
                if (Settings.showCustom) {
                    sheet.addCell(Label(c, 12, "CustomTRIMP"))
                    c += 1
                }
            }
            sheet.addCell(Label(c, 12, "Period"))

            // Add data rows
            for ((index, data) in deviceDataList.reversed().withIndex()) {
                var c = 0
                sheet.addCell(Label(c, index + 13, data.timestamp.toString()))
                c += 1
                if (Settings.showHR) {
                    if (Settings.showHeartRate) {
                        sheet.addCell(Label(c, index + 13, data.hr.toString()))
                        c += 1
                    }
                    if (Settings.showHRPercentage){
                        sheet.addCell(Label(c, index + 13, "%.2f".format(data.hrPercentage)))
                        c += 1
                    }
                    if (Settings.showHRZone) {
                        sheet.addCell(Label(c, index + 13, data.hrZone.toString()))
                        c += 1
                    }
                }
                if (Settings.showHRV) {
                    if (Settings.showSDRR) {
                        sheet.addCell(Label(c, index + 13, "%.2f".format(data.SDRR)))
                        c += 1
                    }
                    if (Settings.showpNN50) {
                        sheet.addCell(Label(c, index + 13, "%.2f".format(data.pNN50)))
                        c += 1
                    }
                    if (Settings.showRMSSD) {
                        sheet.addCell(Label(c, index + 13, "%.2f".format(data.RMSSD)))
                        c += 1
                    }
                }
                if (Settings.showTRIMP) {
                    if (Settings.showBanister) {
                        sheet.addCell(Label(c, index + 13, "%.2f".format(data.BanistersTRIMP)))
                        c += 1
                    }
                    if (Settings.showEdward) {
                        sheet.addCell(Label(c, index + 13, "%.2f".format(data.EdwardsTRIMP)))
                        c += 1
                    }
                    if (Settings.showLucia) {
                        sheet.addCell(Label(c, index + 13, "%.2f".format(data.LuciasTRIMP)))
                        c += 1
                    }
                    if (Settings.showStango) {
                        sheet.addCell(Label(c, index + 13, "%.2f".format(data.StangosTRIMP)))
                        c += 1
                    }
                    if (Settings.showCustom) {
                        sheet.addCell(Label(c, index + 13, "%.2f".format(data.CustomTRIMP)))
                        c += 1
                    }
                }
                sheet.addCell(Label(c, index + 13, data.period?.toString()))
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
        } else if (method == "Custom") {
            return (Settings.zone0Coefficient*t*hrPercentageList.count { it < Settings.zone1 }) +
                    (Settings.zone1Coefficient*t*hrPercentageList.count { it >= Settings.zone1 && it < Settings.zone2 }) +
                    (Settings.zone2Coefficient*t*hrPercentageList.count { it >= Settings.zone2 && it < Settings.zone3 }) +
                    (Settings.zone3Coefficient*t*hrPercentageList.count { it >= Settings.zone3 && it < Settings.zone4 }) +
                    (Settings.zone4Coefficient*t*hrPercentageList.count { it >= Settings.zone4 && it < Settings.zone5 }) +
                    (Settings.zone5Coefficient*t*hrPercentageList.count { it >= Settings.zone5 && it < Settings.zone6 }) +
                    (Settings.zone6Coefficient*t*hrPercentageList.count { it >= Settings.zone6 })
        }
        return 0.0
    }

    // 平均心率，最大心率，最小心率，各个TRIMP的增加量
    // 计算总结，全区间
    private fun summaryAllSession(): Map<String, Double?> {
        val hrValues = deviceDataList.reversed().map { it.hr }
        val maxHr = hrValues.maxOrNull()?.toDouble()
        val minHr = hrValues.minOrNull()?.toDouble()
        val averageHr = hrValues.average().toDouble()
        val banistersTRIMPValues = deviceDataList.reversed().map { it.BanistersTRIMP }
        val banistersTRIMPGrowth = (banistersTRIMPValues.lastOrNull() ?: 0.0) - (banistersTRIMPValues.firstOrNull() ?: 0.0)
        val edwardsTRIMPValues = deviceDataList.reversed().map { it.EdwardsTRIMP }
        val edwardsTRIMPGrowth = (edwardsTRIMPValues.lastOrNull() ?: 0.0) - (edwardsTRIMPValues.firstOrNull() ?: 0.0)
        val luciasTRIMPValues = deviceDataList.reversed().map { it.LuciasTRIMP }
        val luciasTRIMPGrowth = (luciasTRIMPValues.lastOrNull() ?: 0.0) - (luciasTRIMPValues.firstOrNull() ?: 0.0)
        val stangosTRIMPValues = deviceDataList.reversed().map { it.StangosTRIMP }
        val stangosTRIMPGrowth = (stangosTRIMPValues.lastOrNull() ?: 0.0) - (stangosTRIMPValues.firstOrNull() ?: 0.0)
        val customTRIMPValues = deviceDataList.reversed().map { it.CustomTRIMP }
        val customTRIMPGrowth = (customTRIMPValues.lastOrNull() ?: 0.0) - (customTRIMPValues.firstOrNull() ?: 0.0)

        val map = mutableMapOf<String, Double?>()
        map["maxHr"] = maxHr
        map["minHr"] = minHr
        map["averageHr"] = "%.2f".format(averageHr).toDouble()
        map["banistersTRIMPGrowth"] = "%.2f".format(banistersTRIMPGrowth).toDouble()
        map["edwardsTRIMPGrowth"] = "%.2f".format(edwardsTRIMPGrowth).toDouble()
        map["luciasTRIMPGrowth"] = "%.2f".format(luciasTRIMPGrowth).toDouble()
        map["stangosTRIMPGrowth"] = "%.2f".format(stangosTRIMPGrowth).toDouble()
        map["customTRIMPGrowth"] = "%.2f".format(customTRIMPGrowth).toDouble()
        return map
    }

    // 计算总结，区间内，不包括间隔
    private fun summaryAllPeriodWithoutGap(): Map<String, Double?> {
        val dataList = mutableListOf<DeviceData>()
        var banistersTRIMPGrowth = 0.0
        var edwardsTRIMPGrowth = 0.0
        var luciasTRIMPGrowth = 0.0
        var stangosTRIMPGrowth = 0.0
        var customTRIMPGrowth = 0.0

        if (period == 1) {
            // 没有用过period
            for (data in deviceDataList) {
                dataList.add(data)
            }
            val banistersTRIMPValues = dataList.map { it.BanistersTRIMP }
            banistersTRIMPGrowth = (banistersTRIMPValues.lastOrNull() ?: 0.0) - (banistersTRIMPValues.firstOrNull() ?: 0.0)
            val edwardsTRIMPValues = dataList.map { it.EdwardsTRIMP }
            edwardsTRIMPGrowth = (edwardsTRIMPValues.lastOrNull() ?: 0.0) - (edwardsTRIMPValues.firstOrNull() ?: 0.0)
            val luciasTRIMPValues = dataList.map { it.LuciasTRIMP }
            luciasTRIMPGrowth = (luciasTRIMPValues.lastOrNull() ?: 0.0) - (luciasTRIMPValues.firstOrNull() ?: 0.0)
            val stangosTRIMPValues = dataList.map { it.StangosTRIMP }
            stangosTRIMPGrowth = (stangosTRIMPValues.lastOrNull() ?: 0.0) - (stangosTRIMPValues.firstOrNull() ?: 0.0)
            val customTRIMPValues = dataList.map { it.CustomTRIMP }
            customTRIMPGrowth = (customTRIMPValues.lastOrNull() ?: 0.0) - (customTRIMPValues.firstOrNull() ?: 0.0)
        } else {
            // 过滤掉不在区间内的
            for (data in deviceDataList) {
                if (data.period != null) {
                    dataList.add(data)
                }
            }
            for (i in 1 until period) {
                val summary = summaryPeriod(i)
                banistersTRIMPGrowth += summary["banistersTRIMPGrowth"]?: 0.0
                edwardsTRIMPGrowth += summary["edwardsTRIMPGrowth"]?: 0.0
                luciasTRIMPGrowth += summary["luciasTRIMPGrowth"]?: 0.0
                stangosTRIMPGrowth += summary["stangosTRIMPGrowth"]?: 0.0
                customTRIMPGrowth += summary["customTRIMPGrowth"]?: 0.0
            }
        }
        val hrValues = dataList.map { it.hr }
        val maxHr = hrValues.maxOrNull()?.toDouble()
        val minHr = hrValues.minOrNull()?.toDouble()
        val averageHr = hrValues.average().toDouble()

        val map = mutableMapOf<String, Double?>()
        map["maxHr"] = maxHr
        map["minHr"] = minHr
        map["averageHr"] = "%.2f".format(averageHr).toDouble()
        map["banistersTRIMPGrowth"] = "%.2f".format(banistersTRIMPGrowth).toDouble()
        map["edwardsTRIMPGrowth"] = "%.2f".format(edwardsTRIMPGrowth).toDouble()
        map["luciasTRIMPGrowth"] = "%.2f".format(luciasTRIMPGrowth).toDouble()
        map["stangosTRIMPGrowth"] = "%.2f".format(stangosTRIMPGrowth).toDouble()
        map["customTRIMPGrowth"] = "%.2f".format(customTRIMPGrowth).toDouble()
        return map
    }

    // 计算总结，区间内，包括间隔
    private fun summaryAllPeriodWithGap(): Map<String, Double?> {
        val dataList = mutableListOf<DeviceData>()
        if (period == 1) {
            // 没有用过period
            for (data in deviceDataList) {
                dataList.add(data)
            }
        } else {
            val reverseList = deviceDataList.reversed()
            val startIndex = reverseList.indexOfFirst { it.period == 1 }
            val endIndex = reverseList.indexOfLast { it.period == period-1 }
            if (startIndex != -1 && endIndex != -1 && startIndex <= endIndex) {
                val subList = reverseList.subList(startIndex, endIndex + 1)
                for (data in subList.reversed()) {
                    dataList.add(data)
                }
            }
        }
        val hrValues = dataList.map { it.hr }
        val maxHr = hrValues.maxOrNull()?.toDouble()
        val minHr = hrValues.minOrNull()?.toDouble()
        val averageHr = hrValues.average().toDouble()
        val banistersTRIMPValues = dataList.map { it.BanistersTRIMP }
        val banistersTRIMPGrowth = (banistersTRIMPValues.lastOrNull() ?: 0.0) - (banistersTRIMPValues.firstOrNull() ?: 0.0)
        val edwardsTRIMPValues = dataList.map { it.EdwardsTRIMP }
        val edwardsTRIMPGrowth = (edwardsTRIMPValues.lastOrNull() ?: 0.0) - (edwardsTRIMPValues.firstOrNull() ?: 0.0)
        val luciasTRIMPValues = dataList.map { it.LuciasTRIMP }
        val luciasTRIMPGrowth = (luciasTRIMPValues.lastOrNull() ?: 0.0) - (luciasTRIMPValues.firstOrNull() ?: 0.0)
        val stangosTRIMPValues = dataList.map { it.StangosTRIMP }
        val stangosTRIMPGrowth = (stangosTRIMPValues.lastOrNull() ?: 0.0) - (stangosTRIMPValues.firstOrNull() ?: 0.0)
        val customTRIMPValues = dataList.map { it.CustomTRIMP }
        val customTRIMPGrowth = (customTRIMPValues.lastOrNull() ?: 0.0) - (customTRIMPValues.firstOrNull() ?: 0.0)

        val map = mutableMapOf<String, Double?>()
        map["maxHr"] = maxHr
        map["minHr"] = minHr
        map["averageHr"] = "%.2f".format(averageHr).toDouble()
        map["banistersTRIMPGrowth"] = "%.2f".format(-1*banistersTRIMPGrowth).toDouble()
        map["edwardsTRIMPGrowth"] = "%.2f".format(-1*edwardsTRIMPGrowth).toDouble()
        map["luciasTRIMPGrowth"] = "%.2f".format(-1*luciasTRIMPGrowth).toDouble()
        map["stangosTRIMPGrowth"] = "%.2f".format(-1*stangosTRIMPGrowth).toDouble()
        map["customTRIMPGrowth"] = "%.2f".format(-1*customTRIMPGrowth).toDouble()
        return map
    }


    // 计算总结，单个区间
    private fun summaryPeriod(periodNum: Int): Map<String, Double?> {
        val dataList = mutableListOf<DeviceData>()
        if (period == 1) {
            // 没有用过period
            for (data in deviceDataList) {
                dataList.add(data)
            }
        } else {
            val reverseList = deviceDataList.reversed()
            val startIndex = reverseList.indexOfFirst { it.period == periodNum }
            val endIndex = reverseList.indexOfLast { it.period == periodNum }
            if (startIndex != -1 && endIndex != -1 && startIndex <= endIndex) {
                val subList = reverseList.subList(startIndex, endIndex + 1)
                for (data in subList.reversed()) {
                    dataList.add(data)
                }
            }
        }
        val hrValues = dataList.map { it.hr }
        val maxHr = hrValues.maxOrNull()?.toDouble()
        val minHr = hrValues.minOrNull()?.toDouble()
        val averageHr = hrValues.average().toDouble()
        val banistersTRIMPValues = dataList.map { it.BanistersTRIMP }
        val banistersTRIMPGrowth = (banistersTRIMPValues.lastOrNull() ?: 0.0) - (banistersTRIMPValues.firstOrNull() ?: 0.0)
        val edwardsTRIMPValues = dataList.map { it.EdwardsTRIMP }
        val edwardsTRIMPGrowth = (edwardsTRIMPValues.lastOrNull() ?: 0.0) - (edwardsTRIMPValues.firstOrNull() ?: 0.0)
        val luciasTRIMPValues = dataList.map { it.LuciasTRIMP }
        val luciasTRIMPGrowth = (luciasTRIMPValues.lastOrNull() ?: 0.0) - (luciasTRIMPValues.firstOrNull() ?: 0.0)
        val stangosTRIMPValues = dataList.map { it.StangosTRIMP }
        val stangosTRIMPGrowth = (stangosTRIMPValues.lastOrNull() ?: 0.0) - (stangosTRIMPValues.firstOrNull() ?: 0.0)
        val customTRIMPValues = dataList.map { it.CustomTRIMP }
        val customTRIMPGrowth = (customTRIMPValues.lastOrNull() ?: 0.0) - (customTRIMPValues.firstOrNull() ?: 0.0)

        val map = mutableMapOf<String, Double?>()
        map["maxHr"] = maxHr
        map["minHr"] = minHr
        map["averageHr"] = "%.2f".format(averageHr).toDouble()
        map["banistersTRIMPGrowth"] = "%.2f".format(-1*banistersTRIMPGrowth).toDouble()
        map["edwardsTRIMPGrowth"] = "%.2f".format(-1*edwardsTRIMPGrowth).toDouble()
        map["luciasTRIMPGrowth"] = "%.2f".format(-1*luciasTRIMPGrowth).toDouble()
        map["stangosTRIMPGrowth"] = "%.2f".format(-1*stangosTRIMPGrowth).toDouble()
        map["customTRIMPGrowth"] = "%.2f".format(-1*customTRIMPGrowth).toDouble()
        return map
    }

    // 底部提示信息条
    private fun showToast(message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast.show()
    }

}