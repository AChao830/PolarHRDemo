package com.example.polarhrdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.DialogInterface
import android.content.SharedPreferences
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat

class SettingActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "SettingActivity"
    }

    private lateinit var textViewMaxHR: TextView
    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    private lateinit var textViewZone0: TextView
    private lateinit var textViewZone1: TextView
    private lateinit var textViewZone2: TextView
    private lateinit var textViewZone3: TextView
    private lateinit var textViewZone4: TextView
    private lateinit var textViewZone5: TextView
    private lateinit var textViewZone6: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        sharedPreferenceHelper = SharedPreferenceHelper(this)

        val testModeSwitch: SwitchCompat = findViewById(R.id.switchTestMode)
        testModeSwitch.isChecked = Settings.testMode // 初始化开关的状态
        testModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeTestModeSwitch(isChecked)
        }

        textViewMaxHR = findViewById(R.id.textViewMaxHeartRate)
        textViewMaxHR.text = "MAX HEART RATE: ${Settings.maxHeartRate}"

        val buttonChangeMaxHR: Button = findViewById(R.id.buttonChangeMaxHeartRate)
        buttonChangeMaxHR.setOnClickListener {
            onClickChangeMaxHRButton(it)
        }

        textViewZone0 = findViewById(R.id.textViewZone0)
        textViewZone0.text = "ZONE 0: 0% - ${Settings.zone1}%"
        textViewZone1 = findViewById(R.id.textViewZone1)
        textViewZone1.text = "ZONE 1: ${Settings.zone1}% - ${Settings.zone2}%"
        textViewZone2 = findViewById(R.id.textViewZone2)
        textViewZone2.text = "ZONE 2: ${Settings.zone2}% - ${Settings.zone3}%"
        textViewZone3 = findViewById(R.id.textViewZone3)
        textViewZone3.text = "ZONE 3: ${Settings.zone3}% - ${Settings.zone4}%"
        textViewZone4 = findViewById(R.id.textViewZone4)
        textViewZone4.text = "ZONE 4: ${Settings.zone4}% - ${Settings.zone5}%"
        textViewZone5 = findViewById(R.id.textViewZone5)
        textViewZone5.text = "ZONE 5: ${Settings.zone5}% - ${Settings.zone6}%"
        textViewZone6 = findViewById(R.id.textViewZone6)
        textViewZone6.text = "ZONE 6: ${Settings.zone6}% - Infinity"

        val buttonChangeZone1: Button = findViewById(R.id.buttonChangeZone1)
        buttonChangeZone1.setOnClickListener {
            onClickChangeZone1(it)
        }
        val buttonChangeZone2: Button = findViewById(R.id.buttonChangeZone2)
        buttonChangeZone2.setOnClickListener {
            onClickChangeZone2(it)
        }
        val buttonChangeZone3: Button = findViewById(R.id.buttonChangeZone3)
        buttonChangeZone3.setOnClickListener {
            onClickChangeZone3(it)
        }
        val buttonChangeZone4: Button = findViewById(R.id.buttonChangeZone4)
        buttonChangeZone4.setOnClickListener {
            onClickChangeZone4(it)
        }
        val buttonChangeZone5: Button = findViewById(R.id.buttonChangeZone5)
        buttonChangeZone5.setOnClickListener {
            onClickChangeZone5(it)
        }

    }

    // 处理test mode开关事务逻辑
    private fun onCheckedChangeTestModeSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.testMode = true
            showToast("Test Mode is on")
            sharedPreferenceHelper.saveTestMode(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.testMode = false
            showToast("Test Mode is off")
            sharedPreferenceHelper.saveTestMode(false)
        }
    }

    // 处理改变最大心率按钮事务逻辑
    private fun onClickChangeMaxHRButton(view: View){
        showChangeMaxHRDialog(view)
    }

    private fun showChangeMaxHRDialog(view: View){
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new max heart rate")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_device_id)
        input.setText(Settings.maxHeartRate.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newMaxHR = input.text.toString().toIntOrNull()
            if (newMaxHR != null) {
                Settings.maxHeartRate = newMaxHR
                textViewMaxHR.text = "MAX HEART RATE: ${Settings.maxHeartRate}"
                sharedPreferenceHelper.saveMaxHeartRate(newMaxHR)
                showToast("Changed max heart rate to $newMaxHR")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 处理改变zone1下界事务按钮
    private fun onClickChangeZone1(view: View) {
        showChangeZone1Dialog(view)
    }

    private fun showChangeZone1Dialog(view: View){
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone1 lower boundary ")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone1.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone1 = input.text.toString().toIntOrNull()
            if (newZone1 != null) {
                Settings.zone1 = newZone1
                textViewZone0.text = "ZONE 0: 0% - ${Settings.zone1}%"
                textViewZone1.text = "ZONE 1: ${Settings.zone1}% - ${Settings.zone2}%"
                sharedPreferenceHelper.saveZone1(newZone1)
                showToast("Changed zone1 lower boundary to $newZone1")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 处理改变zone2下界事务按钮
    private fun onClickChangeZone2(view: View) {
        showChangeZone2Dialog(view)
    }

    private fun showChangeZone2Dialog(view: View){
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone1 lower boundary ")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone2.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone2 = input.text.toString().toIntOrNull()
            if (newZone2 != null) {
                Settings.zone2 = newZone2
                textViewZone1.text = "ZONE 1: ${Settings.zone1}% - ${Settings.zone2}%"
                textViewZone2.text = "ZONE 2: ${Settings.zone2}% - ${Settings.zone3}%"
                sharedPreferenceHelper.saveZone2(newZone2)
                showToast("Changed zone2 lower boundary to $newZone2")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 处理改变zone3下界事务按钮
    private fun onClickChangeZone3(view: View) {
        showChangeZone3Dialog(view)
    }

    private fun showChangeZone3Dialog(view: View){
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone1 lower boundary ")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone3.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone3 = input.text.toString().toIntOrNull()
            if (newZone3 != null) {
                Settings.zone3 = newZone3
                textViewZone2.text = "ZONE 2: ${Settings.zone2}% - ${Settings.zone3}%"
                textViewZone3.text = "ZONE 3: ${Settings.zone3}% - ${Settings.zone4}%"
                sharedPreferenceHelper.saveZone3(newZone3)
                showToast("Changed zone1 lower boundary to $newZone3")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 处理改变zone4下界事务按钮
    private fun onClickChangeZone4(view: View) {
        showChangeZone4Dialog(view)
    }

    private fun showChangeZone4Dialog(view: View){
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone1 lower boundary ")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone4.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone4 = input.text.toString().toIntOrNull()
            if (newZone4 != null) {
                Settings.zone4 = newZone4
                textViewZone3.text = "ZONE 3: ${Settings.zone3}% - ${Settings.zone4}%"
                textViewZone4.text = "ZONE 4: ${Settings.zone4}% - ${Settings.zone5}%"
                sharedPreferenceHelper.saveZone4(newZone4)
                showToast("Changed zone4 lower boundary to $newZone4")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 处理改变zone5下界事务按钮
    private fun onClickChangeZone5(view: View) {
        showChangeZone5Dialog(view)
    }

    private fun showChangeZone5Dialog(view: View){
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone1 lower boundary ")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone5.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone5 = input.text.toString().toIntOrNull()
            if (newZone5 != null) {
                Settings.zone5 = newZone5
                textViewZone4.text = "ZONE 4: ${Settings.zone4}% - ${Settings.zone5}%"
                textViewZone5.text = "ZONE 5: ${Settings.zone5}% - ${Settings.zone6}%"
                sharedPreferenceHelper.saveZone5(newZone5)
                showToast("Changed zone5 lower boundary to $newZone5")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 底部提示信息条
    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
    }

}