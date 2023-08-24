package com.example.polarhrdemo

import android.annotation.SuppressLint
import android.graphics.Rect
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

class PolarDeviceInfoAdapter(private val deviceList: List<PolarDevice>) :
    RecyclerView.Adapter<PolarDeviceInfoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDeviceInfo: TextView = itemView.findViewById(R.id.textViewDeviceInfo)
        val buttonDeleteDevice: Button = itemView.findViewById(R.id.buttonDeleteDevice)
        val plot: XYPlot = itemView.findViewById(R.id.hr_view_plot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device_info, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = deviceList[position]

        holder.textViewDeviceInfo.text = "Device Id: ${device.deviceId}  Battery: ${device.getLatestBattery()}\n"
        if (Settings.showHR) {
            var textHR = ""
            if (Settings.showHeartRate) {
                textHR += "HeartRate: ${device.getLatestHeartRate()} "
            }
            if (Settings.showHRPercentage){
                textHR += "HR Percentage: ${device.getLatestHRPercentage()} "
            }
            if (Settings.showHRZone) {
                textHR += "HR Zone: ${device.getLatestHRZone()} "
            }
            if (textHR != "") {
                textHR += "\n"
            }
            holder.textViewDeviceInfo.text = holder.textViewDeviceInfo.text.toString() + textHR
        }
        if (Settings.showHRV) {
            var textHRV = ""
            if (Settings.showSDRR) {
                textHRV += "SDRR: ${device.getLatestSDRR()} "
            }
            if (Settings.showpNN50) {
                textHRV += "pNN50: ${device.getLatestpNN50()} "
            }
            if (Settings.showRMSSD) {
                textHRV += "RMSSD: ${device.getLatestRMSSD()} "
            }
            if (textHRV != "") {
                textHRV += "\n"
            }
            holder.textViewDeviceInfo.text = holder.textViewDeviceInfo.text.toString() + textHRV
        }
        if (Settings.showTRIMP) {
            var textTRIMP = ""
            if (Settings.showBanister) {
                textTRIMP += "BanistersTRIMP: ${device.getLatestBanistersTRIMP()} "
            }
            if (Settings.showEdward) {
                textTRIMP += "EdwardsTRIMP: ${device.getLatestEdwardsTRIMP()} "
            }
            if (Settings.showLucia) {
                textTRIMP += "LucaisTRIMP: ${device.getLatestLuciasTRIMP()} "
            }
            if (Settings.showStango) {
                textTRIMP += "StangosTRIMP: ${device.getLatestStangosTRIMP()} "
            }
            if (Settings.showCustom) {
                textTRIMP += "CustomTRIMP: ${device.getLatestCustomTRIMP()} "
            }
            if (textTRIMP != "") {
                textTRIMP += "\n"
            }
            holder.textViewDeviceInfo.text = holder.textViewDeviceInfo.text.toString() + textTRIMP
        }
        holder.buttonDeleteDevice.tag = "${device.groupId},${device.deviceId}"
        holder.plot.addSeries(device.plotter.hrSeries, device.plotter.hrFormatter)
        holder.plot.setRangeBoundaries(50, Settings.maxHeartRate, BoundaryMode.AUTO)
        holder.plot.setDomainBoundaries(0, 360000, BoundaryMode.AUTO)
        // Left labels will increment by 10
        holder.plot.setRangeStep(StepMode.INCREMENT_BY_VAL, 10.0)
        holder.plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 60000.0)
        // Make left labels be an integer (no decimal places)
        holder.plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).format = DecimalFormat("#")
        // These don't seem to have an effect
        holder.plot.linesPerRangeLabel = 2

    }

    override fun getItemCount() = deviceList.size
}