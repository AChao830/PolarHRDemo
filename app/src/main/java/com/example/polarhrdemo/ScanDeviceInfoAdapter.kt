package com.example.polarhrdemo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidplot.xy.BoundaryMode
import com.androidplot.xy.StepMode
import com.androidplot.xy.XYGraphWidget
import com.androidplot.xy.XYPlot
import java.text.DecimalFormat

class ScanDeviceInfoAdapter(private val deviceList: List<String>) :
    RecyclerView.Adapter<ScanDeviceInfoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewScanDeviceInfo: TextView = itemView.findViewById(R.id.textViewScanDeviceInfo)
        val buttonCopyDeviceId: Button = itemView.findViewById(R.id.buttonCopyDeviceId)
        val buttonAssignDeviceToPlayer: Button = itemView.findViewById(R.id.buttonAssignDeviceToPlayer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_device_info, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = deviceList[position]
        holder.textViewScanDeviceInfo.text = "Device ID: $device"
        holder.buttonCopyDeviceId.tag = device
        holder.buttonAssignDeviceToPlayer.tag = device
    }

    override fun getItemCount() = deviceList.size

    fun updateData() {
        notifyDataSetChanged() // 通知适配器数据集发生变化
        // TODO:性能问题
    }
}