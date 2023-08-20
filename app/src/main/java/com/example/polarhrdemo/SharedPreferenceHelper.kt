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
}