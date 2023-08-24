package com.example.polarhrdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.DialogInterface
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidplot.util.PixelUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PlayerActivity:AppCompatActivity() {
    companion object {
        private const val TAG = "PlayerActivity"
    }

    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    private lateinit var textViewPlayer1: TextView
    private lateinit var textViewPlayer2: TextView
    private lateinit var textViewPlayer3: TextView
    private lateinit var textViewPlayer4: TextView
    private lateinit var textViewPlayer5: TextView
    private lateinit var textViewPlayer6: TextView
    private lateinit var textViewPlayer7: TextView
    private lateinit var textViewPlayer8: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        sharedPreferenceHelper = SharedPreferenceHelper(this)
        // 初始化textView
        textViewPlayer1 = findViewById(R.id.textViewPlayer1Info)
        textViewPlayer2 = findViewById(R.id.textViewPlayer2Info)
        textViewPlayer3 = findViewById(R.id.textViewPlayer3Info)
        textViewPlayer4 = findViewById(R.id.textViewPlayer4Info)
        textViewPlayer5 = findViewById(R.id.textViewPlayer5Info)
        textViewPlayer6 = findViewById(R.id.textViewPlayer6Info)
        textViewPlayer7 = findViewById(R.id.textViewPlayer7Info)
        textViewPlayer8 = findViewById(R.id.textViewPlayer8Info)

        textViewPlayer1.text = "Player1, Device Id: ${Settings.Player1}"
        textViewPlayer2.text = "Player2, Device Id: ${Settings.Player2}"
        textViewPlayer3.text = "Player3, Device Id: ${Settings.Player3}"
        textViewPlayer4.text = "Player4, Device Id: ${Settings.Player4}"
        textViewPlayer5.text = "Player5, Device Id: ${Settings.Player5}"
        textViewPlayer6.text = "Player6, Device Id: ${Settings.Player6}"
        textViewPlayer7.text = "Player7, Device Id: ${Settings.Player7}"
        textViewPlayer8.text = "Player8, Device Id: ${Settings.Player8}"

        // 初始化按钮
        // Player1
        val buttonChangePlayer1DeviceId: Button = findViewById(R.id.buttonChangePlayer1DeviceId)
        buttonChangePlayer1DeviceId.setOnClickListener { onClickButtonChangePlayer1DeviceId(it) }
        val buttonClearPlayer1DeviceId: Button = findViewById(R.id.buttonClearPlayer1DeviceId)
        buttonClearPlayer1DeviceId.setOnClickListener { onClickButtonClearPlayer1DeviceId(it) }
        // Player 2
        val buttonChangePlayer2DeviceId: Button = findViewById(R.id.buttonChangePlayer2DeviceId)
        buttonChangePlayer2DeviceId.setOnClickListener { onClickButtonChangePlayer2DeviceId(it) }
        val buttonClearPlayer2DeviceId: Button = findViewById(R.id.buttonClearPlayer2DeviceId)
        buttonClearPlayer2DeviceId.setOnClickListener { onClickButtonClearPlayer2DeviceId(it) }
        // Player 3
        val buttonChangePlayer3DeviceId: Button = findViewById(R.id.buttonChangePlayer3DeviceId)
        buttonChangePlayer3DeviceId.setOnClickListener { onClickButtonChangePlayer3DeviceId(it) }
        val buttonClearPlayer3DeviceId: Button = findViewById(R.id.buttonClearPlayer3DeviceId)
        buttonClearPlayer3DeviceId.setOnClickListener { onClickButtonClearPlayer3DeviceId(it) }
        // Player 4
        val buttonChangePlayer4DeviceId: Button = findViewById(R.id.buttonChangePlayer4DeviceId)
        buttonChangePlayer4DeviceId.setOnClickListener { onClickButtonChangePlayer4DeviceId(it) }
        val buttonClearPlayer4DeviceId: Button = findViewById(R.id.buttonClearPlayer4DeviceId)
        buttonClearPlayer4DeviceId.setOnClickListener { onClickButtonClearPlayer4DeviceId(it) }
        // Player 5
        val buttonChangePlayer5DeviceId: Button = findViewById(R.id.buttonChangePlayer5DeviceId)
        buttonChangePlayer5DeviceId.setOnClickListener { onClickButtonChangePlayer5DeviceId(it) }
        val buttonClearPlayer5DeviceId: Button = findViewById(R.id.buttonClearPlayer5DeviceId)
        buttonClearPlayer5DeviceId.setOnClickListener { onClickButtonClearPlayer5DeviceId(it) }
        // Player 6
        val buttonChangePlayer6DeviceId: Button = findViewById(R.id.buttonChangePlayer6DeviceId)
        buttonChangePlayer6DeviceId.setOnClickListener { onClickButtonChangePlayer6DeviceId(it) }
        val buttonClearPlayer6DeviceId: Button = findViewById(R.id.buttonClearPlayer6DeviceId)
        buttonClearPlayer6DeviceId.setOnClickListener { onClickButtonClearPlayer6DeviceId(it) }
        // Player 7
        val buttonChangePlayer7DeviceId: Button = findViewById(R.id.buttonChangePlayer7DeviceId)
        buttonChangePlayer7DeviceId.setOnClickListener { onClickButtonChangePlayer7DeviceId(it) }
        val buttonClearPlayer7DeviceId: Button = findViewById(R.id.buttonClearPlayer7DeviceId)
        buttonClearPlayer7DeviceId.setOnClickListener { onClickButtonClearPlayer7DeviceId(it) }
        // Player 8
        val buttonChangePlayer8DeviceId: Button = findViewById(R.id.buttonChangePlayer8DeviceId)
        buttonChangePlayer8DeviceId.setOnClickListener { onClickButtonChangePlayer8DeviceId(it) }
        val buttonClearPlayer8DeviceId: Button = findViewById(R.id.buttonClearPlayer8DeviceId)
        buttonClearPlayer8DeviceId.setOnClickListener { onClickButtonClearPlayer8DeviceId(it) }
    }

    // Player1
    private fun onClickButtonChangePlayer1DeviceId(view: View) {
        showDialogChangePlayer1DeviceId(view)
    }

    private fun showDialogChangePlayer1DeviceId(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter Player1's Device Id")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_device_id)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val deviceId = input.text.toString().uppercase()
            Settings.Player1 = deviceId
            textViewPlayer1.text = "Player1, Deivce Id: ${Settings.Player1}"
            sharedPreferenceHelper.savePlayer("Player1", deviceId)
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    private fun onClickButtonClearPlayer1DeviceId(view: View) {
        Settings.Player1 = ""
        textViewPlayer1.text = "Player1, Deivce Id: "
        sharedPreferenceHelper.savePlayer("Player1", "")
    }

    // Player2
    private fun onClickButtonChangePlayer2DeviceId(view: View) {
        showDialogChangePlayer2DeviceId(view)
    }

    private fun showDialogChangePlayer2DeviceId(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter Player2's Device Id")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_device_id)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val deviceId = input.text.toString().uppercase()
            Settings.Player2 = deviceId
            textViewPlayer2.text = "Player2, Device Id: ${Settings.Player2}"
            sharedPreferenceHelper.savePlayer("Player2", deviceId)
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    private fun onClickButtonClearPlayer2DeviceId(view: View) {
        Settings.Player2 = ""
        textViewPlayer2.text = "Player2, Device Id: "
        sharedPreferenceHelper.savePlayer("Player2", "")
    }

    // Player3
    private fun onClickButtonChangePlayer3DeviceId(view: View) {
        showDialogChangePlayer3DeviceId(view)
    }

    private fun showDialogChangePlayer3DeviceId(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter Player3's Device Id")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_device_id)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val deviceId = input.text.toString().uppercase()
            Settings.Player3 = deviceId
            textViewPlayer3.text = "Player3, Device Id: ${Settings.Player3}"
            sharedPreferenceHelper.savePlayer("Player3", deviceId)
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    private fun onClickButtonClearPlayer3DeviceId(view: View) {
        Settings.Player3 = ""
        textViewPlayer3.text = "Player3, Device Id: "
        sharedPreferenceHelper.savePlayer("Player3", "")
    }

    // Player4
    private fun onClickButtonChangePlayer4DeviceId(view: View) {
        showDialogChangePlayer4DeviceId(view)
    }

    private fun showDialogChangePlayer4DeviceId(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter Player4's Device Id")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_device_id)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val deviceId = input.text.toString().uppercase()
            Settings.Player4 = deviceId
            textViewPlayer4.text = "Player4, Device Id: ${Settings.Player4}"
            sharedPreferenceHelper.savePlayer("Player4", deviceId)
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    private fun onClickButtonClearPlayer4DeviceId(view: View) {
        Settings.Player4 = ""
        textViewPlayer4.text = "Player4, Device Id: "
        sharedPreferenceHelper.savePlayer("Player4", "")
    }

    // Player5
    private fun onClickButtonChangePlayer5DeviceId(view: View) {
        showDialogChangePlayer5DeviceId(view)
    }

    private fun showDialogChangePlayer5DeviceId(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter Player5's Device Id")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_device_id)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val deviceId = input.text.toString().uppercase()
            Settings.Player5 = deviceId
            textViewPlayer5.text = "Player5, Device Id: ${Settings.Player5}"
            sharedPreferenceHelper.savePlayer("Player5", deviceId)
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    private fun onClickButtonClearPlayer5DeviceId(view: View) {
        Settings.Player5 = ""
        textViewPlayer5.text = "Player5, Device Id: "
        sharedPreferenceHelper.savePlayer("Player5", "")
    }

    // Player6
    private fun onClickButtonChangePlayer6DeviceId(view: View) {
        showDialogChangePlayer6DeviceId(view)
    }

    private fun showDialogChangePlayer6DeviceId(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter Player6's Device Id")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_device_id)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val deviceId = input.text.toString().uppercase()
            Settings.Player6 = deviceId
            textViewPlayer6.text = "Player6, Device Id: ${Settings.Player6}"
            sharedPreferenceHelper.savePlayer("Player6", deviceId)
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    private fun onClickButtonClearPlayer6DeviceId(view: View) {
        Settings.Player6 = ""
        textViewPlayer6.text = "Player6, Device Id: "
        sharedPreferenceHelper.savePlayer("Player6", "")
    }

    // Player7
    private fun onClickButtonChangePlayer7DeviceId(view: View) {
        showDialogChangePlayer7DeviceId(view)
    }

    private fun showDialogChangePlayer7DeviceId(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter Player7's Device Id")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_device_id)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val deviceId = input.text.toString().uppercase()
            Settings.Player7 = deviceId
            textViewPlayer7.text = "Player7, Device Id: ${Settings.Player7}"
            sharedPreferenceHelper.savePlayer("Player7", deviceId)
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    private fun onClickButtonClearPlayer7DeviceId(view: View) {
        Settings.Player7 = ""
        textViewPlayer7.text = "Player7, Device Id: "
        sharedPreferenceHelper.savePlayer("Player7", "")
    }

    // Player8
    private fun onClickButtonChangePlayer8DeviceId(view: View) {
        showDialogChangePlayer8DeviceId(view)
    }

    private fun showDialogChangePlayer8DeviceId(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter Player8's Device Id")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_input_dialog, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_device_id)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val deviceId = input.text.toString().uppercase()
            Settings.Player8 = deviceId
            textViewPlayer8.text = "Player8, Device Id: ${Settings.Player8}"
            sharedPreferenceHelper.savePlayer("Player8", deviceId)
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    private fun onClickButtonClearPlayer8DeviceId(view: View) {
        Settings.Player8 = ""
        textViewPlayer8.text = "Player8, Device Id: "
        sharedPreferenceHelper.savePlayer("Player8", "")
    }

}