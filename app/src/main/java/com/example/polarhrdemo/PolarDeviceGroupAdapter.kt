package com.example.polarhrdemo

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class PolarDeviceGroupAdapter(private val deviceGroups: List<PolarDeviceGroup>) :
    RecyclerView.Adapter<PolarDeviceGroupAdapter.ViewHolder>() {

    // 用于存储PolarDeviceAdapter的实例列表
    private val deviceAdapters = mutableListOf<PolarDeviceInfoAdapter>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewGroupName: TextView = itemView.findViewById(R.id.textViewGroupName)
        val textViewRecordStatus: TextView = itemView.findViewById(R.id.textViewRecordStatus)
        val textViewPeriodStatus: TextView = itemView.findViewById(R.id.textViewPeriodStatus)
        val recyclerViewDevices: RecyclerView = itemView.findViewById(R.id.recyclerViewDeviceInfo)
        val buttonAddDevice: Button = itemView.findViewById(R.id.buttonAddDevice)
        val buttonDeleteGroup: Button = itemView.findViewById(R.id.buttonDeleteGroup)
        val buttonExportData: Button = itemView.findViewById(R.id.buttonExportData)
        val buttonStartRecord: Button = itemView.findViewById(R.id.buttonStartRecord)
        val buttonStopRecord: Button = itemView.findViewById(R.id.buttonStopRecord)
        val buttonStartPeriod: Button = itemView.findViewById(R.id.buttonStartPeriod)
        val buttonEndPeriod: Button = itemView.findViewById(R.id.buttonEndPeriod)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device_group, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = deviceGroups[position]
        holder.textViewGroupName.text = group.groupId
        holder.buttonAddDevice.tag = group.groupId
        holder.buttonDeleteGroup.tag = group.groupId
        holder.buttonExportData.tag = group.groupId
        holder.buttonStartRecord.tag = group.groupId
        holder.buttonStopRecord.tag = group.groupId
        holder.buttonStartPeriod.tag = group.groupId
        holder.buttonEndPeriod.tag = group.groupId

        // 初始化设备列表RecyclerView和适配器
        holder.recyclerViewDevices.layoutManager = LinearLayoutManager(holder.itemView.context)
        val deviceInfoAdapter = PolarDeviceInfoAdapter(group.polarDeviceList)
        holder.recyclerViewDevices.adapter = deviceInfoAdapter

        holder.textViewRecordStatus.text = if (group.getRecordStatus()) { "Recording:True" } else { "Recording:False" }
        holder.textViewPeriodStatus.text = if (group.getPeriodStatus()) { "Period:${group.getPeriod()}" } else { "Period:" }
    }

    override fun getItemCount() = deviceGroups.size

    fun updateData() {
        notifyDataSetChanged() // 通知适配器数据集发生变化
        // TODO:性能问题
    }
}