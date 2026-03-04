package service.paymentpushlistener

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editServer: EditText
    private lateinit var btnSave: Button
    private lateinit var currentServer: TextView
    private lateinit var editKey: EditText
    private lateinit var currentKey: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editServer = findViewById(R.id.editServer)
        editKey = findViewById(R.id.editKey)
        currentServer = findViewById(R.id.currentServer)
        currentKey = findViewById(R.id.currentKey)
        btnSave = findViewById(R.id.btnSave)

        val prefs = getSharedPreferences("config", MODE_PRIVATE)

        updateCurrentServer(prefs.getString("server_url", "-") ?: "-")
        updateCurrentKey(prefs.getString("aes_key", "-") ?: "-")

        btnSave.setOnClickListener {
            val url = editServer.text.toString().trim()
            val key = editKey.text.toString().trim()

            if (url.isNotEmpty()) {
                prefs.edit().putString("server_url", url).apply()
                updateCurrentServer(url)
            }

            if (key.length == 16) {
                prefs.edit().putString("aes_key", key).apply()
                updateCurrentKey(key)
            } else if (key.isNotEmpty()) {
                Toast.makeText(this, "Key must be 16 characters", Toast.LENGTH_SHORT).show()
            }

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
}