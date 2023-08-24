package com.example.polarhrdemo

import android.content.Context

class SharedPreferenceHelper(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

    fun saveTestMode (testMode: Boolean) {
        sharedPreferences.edit().putBoolean("testMode", testMode).apply()
    }

    fun loadTestMode (): Boolean {
        return sharedPreferences.getBoolean("testMode", false)
    }

    fun saveMaxHeartRate (maxHeartRate: Int) {
        sharedPreferences.edit().putInt("maxHeartRate", maxHeartRate).apply()
    }

    fun loadMaxHeartRate(): Int {
        return sharedPreferences.getInt("maxHeartRate", 200)
    }

    fun saveRestHeartRate (restHeartRate: Int) {
        sharedPreferences.edit().putInt("restHeartRate", restHeartRate).apply()
    }

    fun loadRestHeartRate(): Int {
        return sharedPreferences.getInt("restHeartRate", 60)
    }

    fun saveGender (gender: String) {
        sharedPreferences.edit().putString("gender", gender).apply()
    }

    fun loadGender(): String {
        return sharedPreferences.getString("gender", "Male")?: "Male"
    }

    fun saveZone1(zone1: Int) {
        sharedPreferences.edit().putInt("zone1", zone1).apply()
    }

    fun loadZone1(): Int {
        return sharedPreferences.getInt("zone1", 50)
    }

    fun saveZone2(zone2: Int) {
        sharedPreferences.edit().putInt("zone2", zone2).apply()
    }

    fun loadZone2(): Int {
        return sharedPreferences.getInt("zone2", 60)
    }

    fun saveZone3(zone3: Int) {
        sharedPreferences.edit().putInt("zone3", zone3).apply()
    }

    fun loadZone3(): Int {
        return sharedPreferences.getInt("zone3", 70)
    }

    fun saveZone4(zone4: Int) {
        sharedPreferences.edit().putInt("zone4", zone4).apply()
    }

    fun loadZone4(): Int {
        return sharedPreferences.getInt("zone4", 80)
    }

    fun saveZone5(zone5: Int) {
        sharedPreferences.edit().putInt("zone5", zone5).apply()
    }

    fun loadZone5(): Int {
        return sharedPreferences.getInt("zone5", 90)
    }

    fun saveShowHR(showHR: Boolean) {
        sharedPreferences.edit().putBoolean("showHR", showHR).apply()
    }

    fun loadShowHR(): Boolean {
        return sharedPreferences.getBoolean("showHR", true)
    }

    fun saveShowHeartRate(showHeartRate: Boolean) {
        sharedPreferences.edit().putBoolean("showHeartRate", showHeartRate).apply()
    }

    fun loadShowHeartRate(): Boolean {
        return sharedPreferences.getBoolean("showHeartRate", true)
    }

    fun saveShowHRPercentage(showHRPercentage: Boolean) {
        sharedPreferences.edit().putBoolean("showHRPercentage", showHRPercentage).apply()
    }

    fun loadShowHRPercentage(): Boolean {
        return sharedPreferences.getBoolean("showHRPercentage", true)
    }

    fun saveShowHRZone(showHRZone: Boolean) {
        sharedPreferences.edit().putBoolean("showHRZone", showHRZone).apply()
    }

    fun loadShowHRZone(): Boolean {
        return sharedPreferences.getBoolean("showHRZone", true)
    }

    fun saveShowHRV(showHRV: Boolean) {
        sharedPreferences.edit().putBoolean("showHRV", showHRV).apply()
    }

    fun loadShowHRV(): Boolean {
        return sharedPreferences.getBoolean("showHRV", true)
    }

    fun saveShowSDRR(showSDRR: Boolean) {
        sharedPreferences.edit().putBoolean("showSDRR", showSDRR).apply()
    }

    fun loadShowSDRR(): Boolean {
        return sharedPreferences.getBoolean("showSDRR", true)
    }

    fun saveShowpNN50(showpNN50: Boolean) {
        sharedPreferences.edit().putBoolean("showpNN50", showpNN50).apply()
    }

    fun loadShowpNN50(): Boolean {
        return sharedPreferences.getBoolean("showpNN50", true)
    }

    fun saveShowRMSSD(showRMSSD: Boolean) {
        sharedPreferences.edit().putBoolean("showRMSSD", showRMSSD).apply()
    }

    fun loadShowRMSSD(): Boolean {
        return sharedPreferences.getBoolean("showRMSSD", true)
    }

    fun saveShowTRIMP(showTRIMP: Boolean) {
        sharedPreferences.edit().putBoolean("showTRIMP", showTRIMP).apply()
    }

    fun loadShowTRIMP(): Boolean {
        return sharedPreferences.getBoolean("showTRIMP", true)
    }

    fun saveShowBanister(showBanister: Boolean) {
        sharedPreferences.edit().putBoolean("showBanister", showBanister).apply()
    }

    fun loadShowBanister(): Boolean {
        return sharedPreferences.getBoolean("showBanister", true)
    }

    fun saveShowEdward(showEdward: Boolean) {
        sharedPreferences.edit().putBoolean("showEdward", showEdward).apply()
    }

    fun loadShowEdward(): Boolean {
        return sharedPreferences.getBoolean("showEdward", true)
    }

    fun saveShowLucia(showLucia: Boolean) {
        sharedPreferences.edit().putBoolean("showLucia", showLucia).apply()
    }

    fun loadShowLucia(): Boolean {
        return sharedPreferences.getBoolean("showLucia", true)
    }

    fun saveShowStango(showStango: Boolean) {
        sharedPreferences.edit().putBoolean("showStango", showStango).apply()
    }

    fun loadShowStango(): Boolean {
        return sharedPreferences.getBoolean("showStango", true)
    }

    fun savePlayer(player: String, deviceId: String) {
        sharedPreferences.edit().putString(player, deviceId).apply()
    }

    fun loadPlayer(player: String): String? {
        return sharedPreferences.getString(player, "")
    }

    fun saveLT1(LT1: Int) {
        sharedPreferences.edit().putInt("LT1", LT1).apply()
    }

    fun loadLT1(): Int {
        return sharedPreferences.getInt("LT1", 88)
    }

    fun saveLT2(LT2: Int) {
        sharedPreferences.edit().putInt("LT2", LT2).apply()
    }

    fun loadLT2(): Int {
        return sharedPreferences.getInt("LT2", 92)
    }

    fun saveZoneCoefficient(zone:String, coefficient:Double) {
        sharedPreferences.edit().putFloat(zone, coefficient.toFloat()).apply()
    }

    fun loadZoneCoefficient(zone: String): Double {
        return sharedPreferences.getFloat(zone, 0.0f).toDouble()
    }
}