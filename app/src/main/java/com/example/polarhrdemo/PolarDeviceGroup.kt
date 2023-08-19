package com.example.polarhrdemo

import java.time.Period


class PolarDeviceGroup (val groupId: String) {

    var polarDeviceList = mutableListOf<PolarDevice>() // 存储连接的多台设备

    // 添加设备到组
    fun addDevice(polarDevice: PolarDevice){
        polarDeviceList.add(polarDevice)
    }

    // 删除组内设备
    fun deleteDevice(deviceId: String): Boolean{
        for (device in polarDeviceList) {
            if (device.deviceId == deviceId) {
                device.disconnectDevice()
                polarDeviceList.remove(device)
                return true
            }
        }
        return false
    }

    // 获取是否正在录取状态
    fun getRecordStatus():Boolean {
        return if (polarDeviceList.isEmpty()) { false } else { polarDeviceList[0].isRecord }
    }

    // 获取是否区间状态
    fun getPeriodStatus():Boolean {
        return if (polarDeviceList.isEmpty()) { false } else { polarDeviceList[0].isPeriod }
    }

    // 获取在哪一个区间
    fun getPeriod(): Int {
        return if (polarDeviceList.isEmpty()) { 0 } else { polarDeviceList[0].period }
    }

}