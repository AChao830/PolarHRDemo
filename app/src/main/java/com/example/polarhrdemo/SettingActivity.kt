package com.example.polarhrdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.DialogInterface
import android.content.Intent
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SettingActivity: AppCompatActivity() {
    companion object {
        private const val TAG = "SettingActivity"
    }

    private lateinit var textViewMaxHR: TextView
    private lateinit var textViewRestHR: TextView
    private lateinit var textViewGender: TextView
    private lateinit var textViewLT1: TextView
    private lateinit var textViewLT2: TextView
    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

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

        textViewRestHR = findViewById(R.id.textViewRestHeartRate)
        textViewRestHR.text = "REST HEART RATE: ${Settings.restHeartRate}"
        val buttonChangeRestHR: Button = findViewById(R.id.buttonChangeRestHeartRate)
        buttonChangeRestHR.setOnClickListener{
            onClickChangeRestHRButton(it)
        }

        textViewGender = findViewById(R.id.textViewGender)
        textViewGender.text = "Gender: ${Settings.gender}"
        val buttonChangeGender: Button = findViewById(R.id.buttonChangeGender)
        buttonChangeGender.setOnClickListener{
            onClickChangeGenderButton(it)
        }

        textViewLT1 = findViewById(R.id.textViewLT1)
        textViewLT1.text = "LT1: ${Settings.LT1}%"
        val buttonChangeLT1: Button = findViewById(R.id.buttonChangeLT1)
        buttonChangeLT1.setOnClickListener { onClickChangeLT1Button(it) }

        textViewLT2 = findViewById(R.id.textViewLT2)
        textViewLT2.text = "LT2: ${Settings.LT2}%"
        val buttonChangeLT2: Button = findViewById(R.id.buttonChangeLT2)
        buttonChangeLT2.setOnClickListener { onClickChangeLT2Button(it) }

        val buttonZoneSettings: Button = findViewById(R.id.buttonZoneSettings)
        buttonZoneSettings.setOnClickListener {
            onClickZoneSettingsButton(it)
        }

        val buttonHRSettings: Button = findViewById(R.id.buttonHRSettings)
        buttonHRSettings.setOnClickListener {
            onClickHRSettingsButton(it)
        }

        val buttonHRVSettings: Button = findViewById(R.id.buttonHRVSettings)
        buttonHRVSettings.setOnClickListener {
            onClickHRVSettingsButton(it)
        }

        val buttonTRIMPSettings: Button = findViewById(R.id.buttonTRIMPSettings)
        buttonTRIMPSettings.setOnClickListener {
            onClickTRIMPSettingsButton(it)
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
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.max_hr_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_max_hr)
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

    // 处理改变休息心率按钮事务逻辑
    private fun onClickChangeRestHRButton(view: View){
        showChangeRestHRDialog(view)
    }

    private fun showChangeRestHRDialog(view: View){
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new rest heart rate")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.max_hr_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_max_hr)
        input.setText(Settings.restHeartRate.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newRestHR = input.text.toString().toIntOrNull()
            if (newRestHR != null) {
                Settings.restHeartRate = newRestHR
                textViewRestHR.text = "REST HEART RATE: ${Settings.restHeartRate}"
                sharedPreferenceHelper.saveRestHeartRate(newRestHR)
                showToast("Changed rest heart rate to $newRestHR")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 处理改变性别按钮事务逻辑
    private fun onClickChangeGenderButton(view: View) {
        val options = arrayOf("Male", "Female")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Gender")
            .setSingleChoiceItems(options, -1) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> {
                        Settings.gender = "Male"
                        textViewGender.text = "Gender: ${Settings.gender}"
                        sharedPreferenceHelper.saveGender("Male")
                        showToast("Change Gender to Male")
                    }
                    1 -> {
                        Settings.gender = "Female"
                        textViewGender.text = "Gender: ${Settings.gender}"
                        sharedPreferenceHelper.saveGender("Female")
                        showToast("Change Gender to Female")
                    }
                }
            }
        val dialog = builder.create()
        dialog.show()
    }

    // 处理改变LT1按钮事务逻辑
    private fun onClickChangeLT1Button(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new LT1")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.max_hr_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_max_hr)
        input.setText(Settings.LT1.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newLT1 = input.text.toString().toIntOrNull()
            if (newLT1 != null) {
                Settings.LT1 = newLT1
                textViewLT1.text = "LT1: ${Settings.LT1}%"
                sharedPreferenceHelper.saveLT1(newLT1)
                showToast("Changed LT1 to $newLT1")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 处理改变LT2按钮事务逻辑
    private fun onClickChangeLT2Button(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new LT2")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.max_hr_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_max_hr)
        input.setText(Settings.LT2.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newLT2 = input.text.toString().toIntOrNull()
            if (newLT2 != null) {
                Settings.LT2 = newLT2
                textViewLT2.text = "LT2: ${Settings.LT2}%"
                sharedPreferenceHelper.saveLT2(newLT2)
                showToast("Changed LT1 to $newLT2")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // 处理zone settings按钮事务逻辑
    private fun onClickZoneSettingsButton(view: View) {
        val intent = Intent(this, ZoneSettingActivity::class.java)
        startActivity(intent)
    }

    // 处理hr settings按钮事务逻辑
    private fun onClickHRSettingsButton(view: View) {
        val intent = Intent(this, HRSettingActivity::class.java)
        startActivity(intent)
    }

    // 处理hrv settings按钮事务逻辑
    private fun onClickHRVSettingsButton(view: View) {
        val intent = Intent(this, HRVSettingActivity::class.java)
        startActivity(intent)
    }

    // 处理trimp settings按钮事务逻辑
    private fun onClickTRIMPSettingsButton(view: View) {
        val intent = Intent(this, TRIMPSettingActivity::class.java)
        startActivity(intent)
    }

    // 底部提示信息条
    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
    }
}

class ZoneSettingActivity: AppCompatActivity() {
    companion object {
        private const val TAG = "ZoneSettingActivity"
    }

    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    private lateinit var textViewZone0: TextView
    private lateinit var textViewZone1: TextView
    private lateinit var textViewZone2: TextView
    private lateinit var textViewZone3: TextView
    private lateinit var textViewZone4: TextView
    private lateinit var textViewZone5: TextView
    private lateinit var textViewZone6: TextView

    private lateinit var textViewZone0Coefficient: TextView
    private lateinit var textViewZone1Coefficient: TextView
    private lateinit var textViewZone2Coefficient: TextView
    private lateinit var textViewZone3Coefficient: TextView
    private lateinit var textViewZone4Coefficient: TextView
    private lateinit var textViewZone5Coefficient: TextView
    private lateinit var textViewZone6Coefficient: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zone_settings)

        sharedPreferenceHelper = SharedPreferenceHelper(this)

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

        textViewZone0Coefficient = findViewById(R.id.textViewZone0Coefficient)
        textViewZone0Coefficient.text = "ZONE 0 Coefficient: ${Settings.zone0Coefficient}"
        textViewZone1Coefficient = findViewById(R.id.textViewZone1Coefficient)
        textViewZone1Coefficient.text = "ZONE 1 Coefficient: ${Settings.zone1Coefficient}"
        textViewZone2Coefficient = findViewById(R.id.textViewZone2Coefficient)
        textViewZone2Coefficient.text = "ZONE 2 Coefficient: ${Settings.zone2Coefficient}"
        textViewZone3Coefficient = findViewById(R.id.textViewZone3Coefficient)
        textViewZone3Coefficient.text = "ZONE 3 Coefficient: ${Settings.zone3Coefficient}"
        textViewZone4Coefficient = findViewById(R.id.textViewZone4Coefficient)
        textViewZone4Coefficient.text = "ZONE 4 Coefficient: ${Settings.zone4Coefficient}"
        textViewZone5Coefficient = findViewById(R.id.textViewZone5Coefficient)
        textViewZone5Coefficient.text = "ZONE 5 Coefficient: ${Settings.zone5Coefficient}"
        textViewZone6Coefficient = findViewById(R.id.textViewZone6Coefficient)
        textViewZone6Coefficient.text = "ZONE 6 Coefficient: ${Settings.zone6Coefficient}"

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

        val buttonChangeZone0Coefficient: Button = findViewById(R.id.buttonChangeZone0Coefficient)
        buttonChangeZone0Coefficient.setOnClickListener { onClickChangeZone0Coefficient(it) }
        val buttonChangeZone1Coefficient: Button = findViewById(R.id.buttonChangeZone1Coefficient)
        buttonChangeZone1Coefficient.setOnClickListener { onClickChangeZone1Coefficient(it) }
        val buttonChangeZone2Coefficient: Button = findViewById(R.id.buttonChangeZone2Coefficient)
        buttonChangeZone2Coefficient.setOnClickListener { onClickChangeZone2Coefficient(it) }
        val buttonChangeZone3Coefficient: Button = findViewById(R.id.buttonChangeZone3Coefficient)
        buttonChangeZone3Coefficient.setOnClickListener { onClickChangeZone3Coefficient(it) }
        val buttonChangeZone4Coefficient: Button = findViewById(R.id.buttonChangeZone4Coefficient)
        buttonChangeZone4Coefficient.setOnClickListener { onClickChangeZone4Coefficient(it) }
        val buttonChangeZone5Coefficient: Button = findViewById(R.id.buttonChangeZone5Coefficient)
        buttonChangeZone5Coefficient.setOnClickListener { onClickChangeZone5Coefficient(it) }
        val buttonChangeZone6Coefficient: Button = findViewById(R.id.buttonChangeZone6Coefficient)
        buttonChangeZone6Coefficient.setOnClickListener { onClickChangeZone6Coefficient(it) }
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
        dialog.setTitle("Enter new zone2 lower boundary ")
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
        dialog.setTitle("Enter new zone3 lower boundary ")
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
        dialog.setTitle("Enter new zone4 lower boundary ")
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
        dialog.setTitle("Enter new zone5 lower boundary ")
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

    // Zone 0 Coefficient
    private fun onClickChangeZone0Coefficient(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone0 coefficient")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone0Coefficient.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone0Coefficient = input.text.toString().toDoubleOrNull()
            if (newZone0Coefficient != null) {
                Settings.zone0Coefficient = newZone0Coefficient
                textViewZone0Coefficient.text = "ZONE 0 Coefficient: ${Settings.zone0Coefficient}"
                sharedPreferenceHelper.saveZoneCoefficient("zone0Coefficient", newZone0Coefficient)
                showToast("Changed zone0 coefficient to $newZone0Coefficient")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // Zone 1 Coefficient
    private fun onClickChangeZone1Coefficient(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone1 coefficient")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone1Coefficient.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone1Coefficient = input.text.toString().toDoubleOrNull()
            if (newZone1Coefficient != null) {
                Settings.zone1Coefficient = newZone1Coefficient
                textViewZone1Coefficient.text = "ZONE 1 Coefficient: ${Settings.zone1Coefficient}"
                sharedPreferenceHelper.saveZoneCoefficient("zone1Coefficient", newZone1Coefficient)
                showToast("Changed zone1 coefficient to $newZone1Coefficient")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // Zone 2 Coefficient
    private fun onClickChangeZone2Coefficient(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone2 coefficient")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone2Coefficient.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone2Coefficient = input.text.toString().toDoubleOrNull()
            if (newZone2Coefficient != null) {
                Settings.zone2Coefficient = newZone2Coefficient
                textViewZone2Coefficient.text = "ZONE 2 Coefficient: ${Settings.zone2Coefficient}"
                sharedPreferenceHelper.saveZoneCoefficient("zone2Coefficient", newZone2Coefficient)
                showToast("Changed zone2 coefficient to $newZone2Coefficient")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // Zone 3 Coefficient
    private fun onClickChangeZone3Coefficient(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone3 coefficient")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone3Coefficient.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone3Coefficient = input.text.toString().toDoubleOrNull()
            if (newZone3Coefficient != null) {
                Settings.zone3Coefficient = newZone3Coefficient
                textViewZone3Coefficient.text = "ZONE 3 Coefficient: ${Settings.zone3Coefficient}"
                sharedPreferenceHelper.saveZoneCoefficient("zone3Coefficient", newZone3Coefficient)
                showToast("Changed zone3 coefficient to $newZone3Coefficient")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // Zone 4 Coefficient
    private fun onClickChangeZone4Coefficient(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone4 coefficient")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone4Coefficient.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone4Coefficient = input.text.toString().toDoubleOrNull()
            if (newZone4Coefficient != null) {
                Settings.zone4Coefficient = newZone4Coefficient
                textViewZone4Coefficient.text = "ZONE 4 Coefficient: ${Settings.zone4Coefficient}"
                sharedPreferenceHelper.saveZoneCoefficient("zone4Coefficient", newZone4Coefficient)
                showToast("Changed zone4 coefficient to $newZone4Coefficient")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // Zone 5 Coefficient
    private fun onClickChangeZone5Coefficient(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone5 coefficient")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone5Coefficient.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone5Coefficient = input.text.toString().toDoubleOrNull()
            if (newZone5Coefficient != null) {
                Settings.zone5Coefficient = newZone5Coefficient
                textViewZone5Coefficient.text = "ZONE 5 Coefficient: ${Settings.zone5Coefficient}"
                sharedPreferenceHelper.saveZoneCoefficient("zone5Coefficient", newZone5Coefficient)
                showToast("Changed zone5 coefficient to $newZone5Coefficient")
            } else {
                showToast("Invalid input")
            }
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    // Zone 6 Coefficient
    private fun onClickChangeZone6Coefficient(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter new zone6 coefficient")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.zone_lower_boundary_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_zone_lower_boundary)
        input.setText(Settings.zone6Coefficient.toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val newZone6Coefficient = input.text.toString().toDoubleOrNull()
            if (newZone6Coefficient != null) {
                Settings.zone6Coefficient = newZone6Coefficient
                textViewZone6Coefficient.text = "ZONE 6 Coefficient: ${Settings.zone6Coefficient}"
                sharedPreferenceHelper.saveZoneCoefficient("zone6Coefficient", newZone6Coefficient)
                showToast("Changed zone6 coefficient to $newZone6Coefficient")
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

class HRSettingActivity: AppCompatActivity() {
    companion object {
        private const val TAG = "HRSettingActivity"
    }

    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hr_settings)

        sharedPreferenceHelper = SharedPreferenceHelper(this)

        val showHRSwitch: SwitchCompat = findViewById(R.id.switchShowHR)
        showHRSwitch.isChecked = Settings.showHR // 初始化开关的状态
        showHRSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowHRSwitch(isChecked)
        }

        val showHeartRateSwitch: SwitchCompat = findViewById(R.id.switchShowHeartRate)
        showHeartRateSwitch.isChecked = Settings.showHeartRate // 初始化开关的状态
        showHeartRateSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowHeartRateSwitch(isChecked)
        }

        val showHRPercentageSwitch: SwitchCompat = findViewById(R.id.switchShowHRPercentage)
        showHRPercentageSwitch.isChecked = Settings.showHRPercentage // 初始化开关的状态
        showHRPercentageSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowHRPercentageSwitch(isChecked)
        }

        val showHRZoneSwitch: SwitchCompat = findViewById(R.id.switchShowHRZone)
        showHRZoneSwitch.isChecked = Settings.showHRZone // 初始化开关的状态
        showHRZoneSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowHRZoneSwitch(isChecked)
        }
    }

    private fun onCheckedChangeShowHRSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showHR = true
            sharedPreferenceHelper.saveShowHR(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showHR = false
            sharedPreferenceHelper.saveShowHR(false)
        }
    }

    private fun onCheckedChangeShowHeartRateSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showHeartRate = true
            sharedPreferenceHelper.saveShowHeartRate(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showHeartRate = false
            sharedPreferenceHelper.saveShowHeartRate(false)
        }
    }

    private fun onCheckedChangeShowHRPercentageSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showHRPercentage = true
            sharedPreferenceHelper.saveShowHRPercentage(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showHRPercentage = false
            sharedPreferenceHelper.saveShowHRPercentage(false)
        }
    }

    private fun onCheckedChangeShowHRZoneSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showHRZone = true
            sharedPreferenceHelper.saveShowHRZone(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showHRZone = false
            sharedPreferenceHelper.saveShowHRZone(false)
        }
    }
}

class HRVSettingActivity: AppCompatActivity() {
    companion object {
        private const val TAG = "HRVSettingActivity"
    }

    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hrv_settings)

        sharedPreferenceHelper = SharedPreferenceHelper(this)

        val showHRVSwitch: SwitchCompat = findViewById(R.id.switchShowHRV)
        showHRVSwitch.isChecked = Settings.showHRV // 初始化开关的状态
        showHRVSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowHRVSwitch(isChecked)
        }

        val showSDRRSwitch: SwitchCompat = findViewById(R.id.switchShowSDRR)
        showSDRRSwitch.isChecked = Settings.showSDRR // 初始化开关的状态
        showSDRRSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowSDRRSwitch(isChecked)
        }

        val showpNN50Switch: SwitchCompat = findViewById(R.id.switchShowpNN50)
        showpNN50Switch.isChecked = Settings.showpNN50 // 初始化开关的状态
        showpNN50Switch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowpNN50Switch(isChecked)
        }

        val showRMSSDSwitch: SwitchCompat = findViewById(R.id.switchShowRMSSD)
        showRMSSDSwitch.isChecked = Settings.showRMSSD // 初始化开关的状态
        showRMSSDSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowRMSSDSwitch(isChecked)
        }
    }

    private fun onCheckedChangeShowHRVSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showHRV = true
            sharedPreferenceHelper.saveShowHRV(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showHRV = false
            sharedPreferenceHelper.saveShowHRV(false)
        }
    }

    private fun onCheckedChangeShowSDRRSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showSDRR = true
            sharedPreferenceHelper.saveShowSDRR(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showSDRR = false
            sharedPreferenceHelper.saveShowSDRR(false)
        }
    }

    private fun onCheckedChangeShowpNN50Switch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showpNN50 = true
            sharedPreferenceHelper.saveShowpNN50(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showpNN50 = false
            sharedPreferenceHelper.saveShowpNN50(false)
        }
    }

    private fun onCheckedChangeShowRMSSDSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showRMSSD = true
            sharedPreferenceHelper.saveShowRMSSD(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showRMSSD = false
            sharedPreferenceHelper.saveShowRMSSD(false)
        }
    }
}

class TRIMPSettingActivity: AppCompatActivity() {
    companion object {
        private const val TAG = "TRIMPSettingActivity"
    }

    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimp_settings)

        sharedPreferenceHelper = SharedPreferenceHelper(this)

        val showTRIMPSwitch: SwitchCompat = findViewById(R.id.switchShowTRIMP)
        showTRIMPSwitch.isChecked = Settings.showTRIMP // 初始化开关的状态
        showTRIMPSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowTRIMPSwitch(isChecked)
        }

        val showBanisterSwitch: SwitchCompat = findViewById(R.id.switchShowBanister)
        showBanisterSwitch.isChecked = Settings.showBanister // 初始化开关的状态
        showBanisterSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowBanisterSwitch(isChecked)
        }

        val showEdwardSwitch: SwitchCompat = findViewById(R.id.switchShowEdward)
        showEdwardSwitch.isChecked = Settings.showEdward // 初始化开关的状态
        showEdwardSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowEdwardSwitch(isChecked)
        }

        val showLuciaSwitch: SwitchCompat = findViewById(R.id.switchShowLucia)
        showLuciaSwitch.isChecked = Settings.showLucia // 初始化开关的状态
        showLuciaSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowLuciaSwitch(isChecked)
        }

        val showStangoSwitch: SwitchCompat = findViewById(R.id.switchShowStango)
        showStangoSwitch.isChecked = Settings.showStango // 初始化开关的状态
        showStangoSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowStangoSwitch(isChecked)
        }

        val showCustomSwitch: SwitchCompat = findViewById(R.id.switchShowCustom)
        showCustomSwitch.isChecked = Settings.showCustom // 初始化开关的状态
        showCustomSwitch.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeShowCustomSwitch(isChecked)
        }
    }

    private fun onCheckedChangeShowTRIMPSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showTRIMP = true
            sharedPreferenceHelper.saveShowTRIMP(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showTRIMP = false
            sharedPreferenceHelper.saveShowTRIMP(false)
        }
    }

    private fun onCheckedChangeShowBanisterSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showBanister = true
            sharedPreferenceHelper.saveShowBanister(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showBanister = false
            sharedPreferenceHelper.saveShowBanister(false)
        }
    }

    private fun onCheckedChangeShowEdwardSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showEdward = true
            sharedPreferenceHelper.saveShowEdward(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showEdward = false
            sharedPreferenceHelper.saveShowEdward(false)
        }
    }

    private fun onCheckedChangeShowLuciaSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showLucia = true
            sharedPreferenceHelper.saveShowLucia(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showLucia = false
            sharedPreferenceHelper.saveShowLucia(false)
        }
    }

    private fun onCheckedChangeShowStangoSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showStango = true
            sharedPreferenceHelper.saveShowStango(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showStango = false
            sharedPreferenceHelper.saveShowStango(false)
        }
    }

    private fun onCheckedChangeShowCustomSwitch(isChecked: Boolean) {
        if (isChecked) {
            // 当开关按钮被打开时执行的操作
            Settings.showCustom = true
            sharedPreferenceHelper.saveShowCustom(true)
        } else {
            // 当开关按钮被关闭时执行的操作
            Settings.showCustom = false
            sharedPreferenceHelper.saveShowCustom(false)
        }
    }
}