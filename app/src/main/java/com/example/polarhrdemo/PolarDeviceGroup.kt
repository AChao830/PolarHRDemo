package com.example.polarhrdemo


class PolarDeviceGroup (val groupId: String) {

    var polarDeviceList = mutableListOf<PolarDevice>() // 存储连接的多台设备

    // 添加设备到组
    fun addDevice(polarDevice: PolarDevice){
        polarDeviceList.add(polarDevice)
    }

    // 删除组内设备
    fun deleteDevice(){
        // TODO:以后实现
        return
    }

}