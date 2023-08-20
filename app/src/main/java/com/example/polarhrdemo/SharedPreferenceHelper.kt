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
}