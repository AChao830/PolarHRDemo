package com.example.polarhrdemo

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidplot.util.PixelUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HRActivity : AppCompatActivity() , UpdateCallback {
    companion object {
        private const val TAG = "HRActivity"
    }

    private val polarDeviceGroupList = mutableListOf<PolarDeviceGroup>()
    private lateinit var groupName: String
    private lateinit var deviceId: String
    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    var testMode = Settings.testMode

    // 展示相关
    private lateinit var recyclerViewDeviceGroup: RecyclerView
    private lateinit var polarDeviceGroupAdapter: PolarDeviceGroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hr)

        sharedPreferenceHelper = SharedPreferenceHelper(this)

        // 初始化RecyclerView和适配器
        recyclerViewDeviceGroup = findViewById(R.id.recyclerViewDeviceGroup)
        recyclerViewDeviceGroup.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        polarDeviceGroupAdapter = PolarDeviceGroupAdapter(polarDeviceGroupList)
        recyclerViewDeviceGroup.adapter = polarDeviceGroupAdapter

        PixelUtils.init(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清空所有订阅和关闭所有api
        for (deviceGroup in polarDeviceGroupList) {
            for (device in deviceGroup.polarDeviceList) {
                device.disconnectDevice()
            }
        }
    }

    // 更新页面数据
    override fun updateDeviceInfo() {
        runOnUiThread { polarDeviceGroupAdapter.updateData() }
    }

    // 更新页面数据
    override fun updateGraph() {
        runOnUiThread { polarDeviceGroupAdapter.updateData() }
    }

    // --------------------设备组管理--------------------
    // 处理点击添加组按钮事务
    fun onClickAddGroupButton (view: View) {
        showDialogAddGroup(view)
    }

    // 处理点击删除组按钮事务
    fun onClickDeleteGroupButton (view: View) {
        val groupId = view.tag.toString()
        val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
        if (polarDeviceGroup != null) {
            for (device in polarDeviceGroup.polarDeviceList) {
                device.disconnectDevice()
            }
            polarDeviceGroupList.remove(polarDeviceGroup)
            polarDeviceGroupAdapter.updateData()
        }
    }

    // --------------------组内设备管理--------------------
    // 处理点击新增设备按钮事务
    fun onClickAddDeviceButton (view: View) {
        val groupId = view.tag.toString()
        showDialogAddDevice(view, groupId)
    }

    // 处理点击删除设备按钮事务
    // TODO: 有个bug，只能从底下删
    fun onClickDeleteDeviceButton(view: View) {
        val groupId = view.tag.toString().split(",")[0]
        val deviceId = view.tag.toString().split(",")[1]
        polarDeviceGroupList.find { it.groupId == groupId }?.deleteDevice(deviceId)
        polarDeviceGroupAdapter.updateData()
    }

    // 处理点击输出组数据按钮事务
    fun onClickExportGroupDataButton (view: View) {
        val groupId = view.tag.toString()
        showDialogExportData(view, groupId)
    }

    // 处理点击开始录制按钮事务
    fun onClickStartGroupRecordButton (view: View) {
        val groupId = view.tag.toString()
        val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
        if (polarDeviceGroup != null) {
            for (polarDevice in polarDeviceGroup.polarDeviceList) {
                polarDevice.startRecord()
            }
        }

    }

    // 处理点击结束录制按钮事务
    fun onClickStopGroupRecordButton (view: View) {
        val groupId = view.tag.toString()
        val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
        if (polarDeviceGroup != null) {
            for (polarDevice in polarDeviceGroup.polarDeviceList) {
                polarDevice.stopRecord()
            }
        }
    }

    // 处理点击开始区间按钮事务
    fun onClickStartGroupPeriodButton (view: View) {
        val groupId = view.tag.toString()
        val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
        if (polarDeviceGroup != null) {
            for (polarDevice in polarDeviceGroup.polarDeviceList) {
                polarDevice.startPeriod()
            }
        }
    }

    // 处理点击结束区间按钮事务
    fun onClickEndGroupPeriodButton (view: View) {
        val groupId = view.tag.toString()
        val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
        if (polarDeviceGroup != null) {
            for (polarDevice in polarDeviceGroup.polarDeviceList) {
                polarDevice.endPeriod()
            }
        }
    }

    // --------------------其它--------------------

    // 处理输入组ID
    private fun showDialogAddGroup(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new group's name")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.group_name_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_group_name)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            groupName = input.text.toString()
            polarDeviceGroupList.add(0, PolarDeviceGroup(groupName))
            polarDeviceGroupAdapter.updateData()
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 处理输入设备ID
    private fun showDialogAddDevice(view: View, groupId: String) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter your Polar device's ID")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_device_id)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setNeutralButton("Choose") { _: DialogInterface?, _: Int ->
            showDialogChooseFromPlayer(view, groupId)
        }
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            deviceId = input.text.toString().uppercase()
            val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
            if (polarDeviceGroup != null) {
                val newDevice = PolarDevice(groupId, deviceId, applicationContext, testMode)
                newDevice.connectToDevice() // 开始连接
                newDevice.setUpdateCallback(this) // 设置更新callback
                newDevice.setPlotterListener(this)
                polarDeviceGroup.addDevice(newDevice)
                polarDeviceGroupAdapter.updateData()
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 处理从Player中选择
    private fun showDialogChooseFromPlayer(view: View, groupId: String) {
        val options = playerList.toTypedArray()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Player")
            .setSingleChoiceItems(options, -1) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> {
                        deviceId = Settings.Player1
                        if (deviceId != "") {
                            val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
                            if (polarDeviceGroup != null) {
                                val newDevice = PolarDevice(groupId, deviceId, applicationContext, testMode)
                                newDevice.connectToDevice() // 开始连接
                                newDevice.setUpdateCallback(this) // 设置更新callback
                                newDevice.setPlotterListener(this)
                                polarDeviceGroup.addDevice(newDevice)
                                polarDeviceGroupAdapter.updateData()
                            }
                        } else {
                            showToast("Player does not have a valid device ID")
                        }
                    }
                    1 -> {
                        deviceId = Settings.Player2
                        if (deviceId != "") {
                            val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
                            if (polarDeviceGroup != null) {
                                val newDevice = PolarDevice(groupId, deviceId, applicationContext, testMode)
                                newDevice.connectToDevice() // 开始连接
                                newDevice.setUpdateCallback(this) // 设置更新callback
                                newDevice.setPlotterListener(this)
                                polarDeviceGroup.addDevice(newDevice)
                                polarDeviceGroupAdapter.updateData()
                            }
                        } else {
                            showToast("Player does not have a valid device ID")
                        }
                    }
                    2 -> {
                        deviceId = Settings.Player3
                        if (deviceId != "") {
                            val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
                            if (polarDeviceGroup != null) {
                                val newDevice = PolarDevice(groupId, deviceId, applicationContext, testMode)
                                newDevice.connectToDevice() // 开始连接
                                newDevice.setUpdateCallback(this) // 设置更新callback
                                newDevice.setPlotterListener(this)
                                polarDeviceGroup.addDevice(newDevice)
                                polarDeviceGroupAdapter.updateData()
                            }
                        } else {
                            showToast("Player does not have a valid device ID")
                        }
                    }
                    3 -> {
                        deviceId = Settings.Player4
                        if (deviceId != "") {
                            val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
                            if (polarDeviceGroup != null) {
                                val newDevice = PolarDevice(groupId, deviceId, applicationContext, testMode)
                                newDevice.connectToDevice() // 开始连接
                                newDevice.setUpdateCallback(this) // 设置更新callback
                                newDevice.setPlotterListener(this)
                                polarDeviceGroup.addDevice(newDevice)
                                polarDeviceGroupAdapter.updateData()
                            }
                        } else {
                            showToast("Player does not have a valid device ID")
                        }
                    }
                    4 -> {
                        deviceId = Settings.Player5
                        if (deviceId != "") {
                            val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
                            if (polarDeviceGroup != null) {
                                val newDevice = PolarDevice(groupId, deviceId, applicationContext, testMode)
                                newDevice.connectToDevice() // 开始连接
                                newDevice.setUpdateCallback(this) // 设置更新callback
                                newDevice.setPlotterListener(this)
                                polarDeviceGroup.addDevice(newDevice)
                                polarDeviceGroupAdapter.updateData()
                            }
                        } else {
                            showToast("Player does not have a valid device ID")
                        }
                    }
                    5 -> {
                        deviceId = Settings.Player6
                        if (deviceId != "") {
                            val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
                            if (polarDeviceGroup != null) {
                                val newDevice = PolarDevice(groupId, deviceId, applicationContext, testMode)
                                newDevice.connectToDevice() // 开始连接
                                newDevice.setUpdateCallback(this) // 设置更新callback
                                newDevice.setPlotterListener(this)
                                polarDeviceGroup.addDevice(newDevice)
                                polarDeviceGroupAdapter.updateData()
                            }
                        } else {
                            showToast("Player does not have a valid device ID")
                        }
                    }
                    6 -> {
                        deviceId = Settings.Player7
                        if (deviceId != "") {
                            val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
                            if (polarDeviceGroup != null) {
                                val newDevice = PolarDevice(groupId, deviceId, applicationContext, testMode)
                                newDevice.connectToDevice() // 开始连接
                                newDevice.setUpdateCallback(this) // 设置更新callback
                                newDevice.setPlotterListener(this)
                                polarDeviceGroup.addDevice(newDevice)
                                polarDeviceGroupAdapter.updateData()
                            }
                        } else {
                            showToast("Player does not have a valid device ID")
                        }
                    }
                    7 -> {
                        deviceId = Settings.Player8
                        if (deviceId != "") {
                            val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
                            if (polarDeviceGroup != null) {
                                val newDevice = PolarDevice(groupId, deviceId, applicationContext, testMode)
                                newDevice.connectToDevice() // 开始连接
                                newDevice.setUpdateCallback(this) // 设置更新callback
                                newDevice.setPlotterListener(this)
                                polarDeviceGroup.addDevice(newDevice)
                                polarDeviceGroupAdapter.updateData()
                            }
                        } else {
                            showToast("Player does not have a valid device ID")
                        }
                    }
                }
            }
        val dialog = builder.create()
        dialog.show()
    }

    // 处理选择输出格式
    private fun showDialogExportData(view:View, groupId: String) {
        val options = arrayOf("CSV", "Excel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Output Format")
            .setSingleChoiceItems(options, -1) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> {
                        val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
                        val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
                        if (polarDeviceGroup != null) {
                            for (polarDevice in polarDeviceGroup.polarDeviceList) {
                                polarDevice.exportDataToCSV(groupId, applicationContext, "${groupId}_${polarDevice.deviceId}_${currentDateTime}.csv")
                            }
                        }
                        showToast("Data Exported as CSV Files")
                    }
                    1 -> {
                        val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
                        val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
                        if (polarDeviceGroup != null) {
                            for (polarDevice in polarDeviceGroup.polarDeviceList) {
                                polarDevice.exportDataToExcel(groupId, applicationContext, "${groupId}_${polarDevice.deviceId}_${currentDateTime}.xls")
                            }
                        }
                        showToast("Data Exported as Excel Files")
                    }
                }
            }
        val dialog = builder.create()
        dialog.show()
    }

    // 底部提示信息条
    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
    }
}