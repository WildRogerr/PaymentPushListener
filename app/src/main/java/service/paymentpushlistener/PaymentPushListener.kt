package service.paymentpushlistener

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

class PaymentPushListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "ru.sberbankmobile") return
        val extras = sbn.notification.extras ?: return
        val text = extras.getCharSequence("android.text")?.toString() ?: return
        val amount = parseAmount(text) ?: return

        val event = PaymentEvent(
            amount = amount,
            timestamp = nowEpoch()
        )

        val json = Gson().toJson(event)

        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val aesKey = prefs.getString("aes_key", "1234567890123456") ?: "1234567890123456"
        val encryptedJson = encrypt(json, aesKey)
        sendToServer(encryptedJson, serverUrlFromPrefs())
    }

    private fun parseAmount(text: String): Long? {
        val cleaned = text.replace(" ", "")
        val regex = Regex("(\\d+[.,]?\\d*)₽")
        val match = regex.find(cleaned) ?: return null
        val value = match.groupValues[1]
            .replace(",", ".")
            .toDouble()
        return (value * 100).toLong()
    }

    private fun nowEpoch(): Long {
        return System.currentTimeMillis()
    }

    data class PaymentEvent(
        val amount: Long,
        val timestamp: Long
    )

    private fun sendToServer(json: String, serverUrl: String, retries: Int = 3) {
        if (serverUrl.isBlank()) {
            Log.e("HTTP", "Server URL is empty, skipping send")
            return
        }

        val client = OkHttpClient()
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(serverUrl).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HTTP", "Send failed", e)
                if (retries > 0) {
                    android.os.Handler(mainLooper).postDelayed({
                        sendToServer(json, serverUrl, retries - 1)
                    }, 5000)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("HTTP", "Response: ${response.code}")
                response.close()
            }
        })
    }

    fun serverUrlFromPrefs(): String {
        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        return prefs.getString("server_url", "") ?: ""
    }

    private fun encrypt(json: String, key: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(json.toByteArray())
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }
}