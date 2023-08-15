package com.example.polarhrdemo

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.PolarBleApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HRActivity : AppCompatActivity() , UpdateCallback {
    companion object {
        private const val TAG = "HRActivity"
    }

    private val polarDeviceGroupList = mutableListOf<PolarDeviceGroup>()
    private lateinit var groupName: String
    private lateinit var deviceId: String

    var testMode = Settings.testMode

    // 展示相关
    private lateinit var recyclerViewDeviceGroup: RecyclerView
    private lateinit var polarDeviceGroupAdapter: PolarDeviceGroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hr)

        // 初始化RecyclerView和适配器
        recyclerViewDeviceGroup = findViewById(R.id.recyclerViewDeviceGroup)
        recyclerViewDeviceGroup.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        polarDeviceGroupAdapter = PolarDeviceGroupAdapter(polarDeviceGroupList)
        recyclerViewDeviceGroup.adapter = polarDeviceGroupAdapter
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
        polarDeviceGroupAdapter.updateData()
    }

    // --------------------设备组管理--------------------
    // 处理点击添加组按钮事务
    fun onClickAddGroupButton (view: View) {
        showDialogAddGroup(view)
    }

    // 处理点击删除组按钮事务
    fun onClickDeleteGroupButton (view: View) {
        // TODO: not implemented
    }

    // --------------------组内设备管理--------------------
    // 处理点击新增设备按钮事务
    fun onClickAddDeviceButton (view: View) {
        val groupId = view.tag.toString()
        showDialogAddDevice(view, groupId)
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
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            deviceId = input.text.toString().uppercase()
            val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
            if (polarDeviceGroup != null) {
                val newDevice = PolarDevice(deviceId, applicationContext, testMode)
                newDevice.connectToDevice() // 开始连接
                newDevice.setUpdateCallback(this) // 设置更新callback
                polarDeviceGroup.addDevice(newDevice)
                polarDeviceGroupAdapter.updateData()
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
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
                        if (polarDeviceGroup != null) {
                            for (polarDevice in polarDeviceGroup.polarDeviceList) {
                                polarDevice.exportDataToCSV(applicationContext, "group:${groupId}_device:${polarDevice.deviceId}.csv")
                            }
                        }
                        showToast("Data Exported as CSV Files")
                    }
                    1 -> {
                        val polarDeviceGroup = polarDeviceGroupList.find { it.groupId == groupId }
                        if (polarDeviceGroup != null) {
                            for (polarDevice in polarDeviceGroup.polarDeviceList) {
                                polarDevice.exportDataToExcel(applicationContext, "group:${groupId}_device:${polarDevice.deviceId}.xls")
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