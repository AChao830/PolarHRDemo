package com.example.polarhrdemo

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PolarDeviceInfoAdapter(private val deviceList: List<PolarDevice>) :
    RecyclerView.Adapter<PolarDeviceInfoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDeviceInfo: TextView = itemView.findViewById(R.id.textViewDeviceInfo)
        val buttonDeleteDevice: Button = itemView.findViewById(R.id.buttonDeleteDevice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device_info, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = deviceList[position]
        holder.textViewDeviceInfo.text = "Device Id: ${device.deviceId}  Battery: ${device.getLatestBattery()}\n" +
                "HeartRate: ${device.getLatestHeartRate()} HR Percentage: ${device.getLatestHRPercentage()} HR Quantile: ${device.getLatestHRQuantile()}\n" +
                "SDRR: ${device.getLatestSDRR()} pNN50: ${device.getLatestpNN50()} RMSSD: ${device.getLatestRMSSD()}"
        holder.buttonDeleteDevice.tag = "${device.groupId},${device.deviceId}"
    }

    override fun getItemCount() = deviceList.size
}