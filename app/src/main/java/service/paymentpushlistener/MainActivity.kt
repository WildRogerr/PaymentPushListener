package service.paymentpushlistener

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editServer: EditText
    private lateinit var currentServer: TextView
    private lateinit var editKey: EditText
    private lateinit var currentKey: TextView
    private lateinit var editMinAmount: EditText
    private lateinit var editMaxAmount: EditText
    private lateinit var currentMinAmount: TextView
    private lateinit var currentMaxAmount: TextView
    private lateinit var btnSave: Button
    private lateinit var cbSber: CheckBox
    private lateinit var cbTbank: CheckBox
    private lateinit var cbAlpha: CheckBox
    private lateinit var cbVTB: CheckBox
    private val enabledBanks = mutableSetOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editServer = findViewById(R.id.editServer)
        editKey = findViewById(R.id.editKey)
        currentServer = findViewById(R.id.currentServer)
        currentKey = findViewById(R.id.currentKey)
        editMinAmount = findViewById(R.id.editMinAmount)
        editMaxAmount = findViewById(R.id.editMaxAmount)
        currentMinAmount = findViewById(R.id.currentMinAmount)
        currentMaxAmount = findViewById(R.id.currentMaxAmount)
        btnSave = findViewById(R.id.btnSave)
        cbSber = findViewById(R.id.cbSber)
        cbTbank = findViewById(R.id.cbTbank)
        cbAlpha = findViewById(R.id.cbAlpha)
        cbVTB = findViewById(R.id.cbVTB)

        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        updateCurrentServer(prefs.getString("server_url", "-") ?: "-")
        updateCurrentKey(prefs.getString("aes_key", "-") ?: "-")
        updateCurrentMinAmount(prefs.getString("min_amount", "-") ?: "-")
        updateCurrentMaxAmount(prefs.getString("max_amount", "-") ?: "-")

        val savedBanks = prefs.getStringSet("enabled_banks", emptySet()) ?: emptySet()
        enabledBanks.addAll(savedBanks)

        cbSber.isChecked = "ru.sberbankmobile" in enabledBanks
        cbTbank.isChecked = "com.idamob.tinkoff.android" in enabledBanks
        cbAlpha.isChecked = "ru.alfabank.mobile" in enabledBanks
        cbVTB.isChecked = "ru.vtb.mobilebanking" in enabledBanks

        cbSber.setOnCheckedChangeListener { _, checked ->
            if (checked) enabledBanks.add("ru.sberbankmobile") else enabledBanks.remove("ru.sberbankmobile")
        }
        cbTbank.setOnCheckedChangeListener { _, checked ->
            if (checked) enabledBanks.add("com.idamob.tinkoff.android") else enabledBanks.remove("com.idamob.tinkoff.android")
        }
        cbAlpha.setOnCheckedChangeListener { _, checked ->
            if (checked) enabledBanks.add("ru.alfabank.mobile") else enabledBanks.remove("ru.alfabank.mobile")
        }
        cbVTB.setOnCheckedChangeListener { _, checked ->
            if (checked) enabledBanks.add("ru.vtb.mobilebanking") else enabledBanks.remove("ru.vtb.mobilebanking")
        }

        btnSave.setOnClickListener {
            val url = editServer.text.toString().trim()
            val key = editKey.text.toString().trim()
            val min_amount_value = editMinAmount.text.toString().trim()
            val max_amount_value = editMaxAmount.text.toString().trim()

            if (url.isNotEmpty()) {
                prefs.edit().putString("server_url", url).apply()
                updateCurrentServer(url)
            }

            if (min_amount_value.isNotEmpty()) {
                prefs.edit().putString("min_amount", min_amount_value).apply()
                updateCurrentMinAmount(min_amount_value)
            }

            if (max_amount_value.isNotEmpty()) {
                prefs.edit().putString("max_amount", max_amount_value).apply()
                updateCurrentMaxAmount(max_amount_value)
            }

            if (key.length == 16) {
                prefs.edit().putString("aes_key", key).apply()
                updateCurrentKey(key)
            } else if (key.isNotEmpty()) {
                Toast.makeText(this, "Key must be 16 characters", Toast.LENGTH_SHORT).show()
            }

            prefs.edit().putStringSet("enabled_banks", enabledBanks).apply()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }

        if (!hasNotificationAccess()) {
            Toast.makeText(
                this,
                "Notification access is disabled. Please enable it.",
                Toast.LENGTH_LONG
            ).show()
        }

        findViewById<Button>(R.id.btnNotif).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun hasNotificationAccess(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        return enabled.contains(packageName)
    }

    private fun updateCurrentServer(url: String) {
        currentServer.text = "Current server: $url"
    }

    private fun updateCurrentKey(enKey: String) {
        currentKey.text = "Current Encryption Key: $enKey"
    }

    private fun updateCurrentMinAmount(min_amount_value: String) {
        currentMinAmount.text = "Current min amount: $min_amount_value ₽"
    }

    private fun updateCurrentMaxAmount(max_amount_value: String) {
        currentMaxAmount.text = "Current max amount: $max_amount_value ₽"
    }
}