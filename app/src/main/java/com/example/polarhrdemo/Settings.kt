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
    var gender: String = "Male"
    var LT1: Int = 88 // LT1阈值
    var LT2: Int = 92 // LT2阈值
    // Zone的自定义coefficient
    var zone0Coefficient: Double = 0.0
    var zone1Coefficient: Double = 0.0
    var zone2Coefficient: Double = 0.0
    var zone3Coefficient: Double = 0.0
    var zone4Coefficient: Double = 0.0
    var zone5Coefficient: Double = 0.0
    var zone6Coefficient: Double = 0.0
    // HR控制展示与记录
    var showHR:Boolean = true // 总控
    var showHeartRate: Boolean = true
    var showHRPercentage: Boolean = true
    var showHRZone: Boolean = true
    // HRV控制展示与记录
    var showHRV: Boolean = false
    var showSDRR:Boolean = true
    var showpNN50: Boolean = true
    var showRMSSD: Boolean = true
    // TRIMP控制展示与记录
    var showTRIMP: Boolean = true
    var showBanister: Boolean = false
    var showEdward: Boolean = true
    var showLucia: Boolean = false
    var showStango: Boolean = true

    var Player1: String = ""
    var Player2: String = ""
    var Player3: String = ""
    var Player4: String = ""
    var Player5: String = ""
    var Player6: String = ""
    var Player7: String = ""
    var Player8: String = ""
}

val playerList = listOf<String>("Player1", "Player2", "Player3", "Player4", "Player5", "Player6", "Player7", "Player8")