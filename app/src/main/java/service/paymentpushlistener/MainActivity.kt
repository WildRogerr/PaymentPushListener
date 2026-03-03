package service.paymentpushlistener

import android.content.Intent
import android.os.Bundle
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
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

        editServer = findViewById(R.id.editServer)
        btnSave = findViewById(R.id.btnSave)
        currentServer = findViewById(R.id.currentServer)
        editKey = findViewById(R.id.editKey)
        currentKey = findViewById(R.id.currentKey)


        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        updateCurrentServer(prefs.getString("server_url", "-") ?: "-")
        updateCurrentKey(enKey = prefs.getString("aes_key", "-") ?: "-")

        btnSave.setOnClickListener {
            val url = editServer.text.toString().trim()
            if (url.isNotEmpty()) {
                prefs.edit().putString("server_url", url).apply()
                editServer.text.clear()
                Toast.makeText(this, "Server saved!", Toast.LENGTH_SHORT).show()
                updateCurrentServer(url)
            } else {
                Toast.makeText(this, "Enter a valid server URL", Toast.LENGTH_SHORT).show()
            }

            val key = editKey.text.toString().trim()
            if (key.length == 16) {
                prefs.edit().putString("aes_key", key).apply()
                editKey.text.clear()
                Toast.makeText(this, "Encryption key saved!", Toast.LENGTH_SHORT).show()
                updateCurrentKey(key)
            } else {
                Toast.makeText(this, "Key must be 16 characters", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCurrentServer(url: String) {
        currentServer.text = "Current server: $url"
    }
    private fun updateCurrentKey(enKey: String) {
        currentKey.text = "Current Encryption Key: $enKey"
    }
}