package com.example.polarhrdemo

object Settings {
    /**
     * 储存设置
     */
    var testMode: Boolean = true // 是否是测试模式，测试模式下使用伪数据
    var maxHeartRate: Int = 200 // 最大心率
    var restHeartRate: Int = 60 // 休息心率
    // Zone Settings, all lower boundaries
    var zone0: Int = 0 // unchangeable
    var zone1: Int = 50
    var zone2: Int = 60
    var zone3: Int = 70
    var zone4: Int = 80
    var zone5: Int = 90
    var zone6: Int = 100 // unchangeable

    var sex: String = "Male"

    var LT1: Int = 88 // LT1阈值
    var LT2: Int = 92 // LT2阈值
}