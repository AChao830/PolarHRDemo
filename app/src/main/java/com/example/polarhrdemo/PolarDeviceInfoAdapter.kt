package com.example.polarhrdemo

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PolarDeviceInfoAdapter(private val deviceList: List<PolarDevice>) :
    RecyclerView.Adapter<PolarDeviceInfoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDeviceInfo: TextView = itemView.findViewById(R.id.textViewDeviceInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device_info, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = deviceList[position]
        holder.textViewDeviceInfo.text = "Device Id: ${device.deviceId}  HeartRate: ${device.getLatestHeartRate()}"
    }

    override fun getItemCount() = deviceList.size
}