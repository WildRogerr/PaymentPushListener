package service.paymentpushlistener

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class PaymentPushListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        val extras = sbn.notification.extras ?: return
        val text = extras.getCharSequence("android.text")?.toString() ?: return

        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val enabledBanks = prefs.getStringSet("enabled_banks", emptySet()) ?: emptySet()
        if (pkg !in enabledBanks) return

        val amount = parseAmount(text) ?: return
        val currentMin = prefs.getString("min_amount", "")?.toDoubleOrNull()?.times(100)?.toLong()
        val currentMax = prefs.getString("max_amount", "")?.toDoubleOrNull()?.times(100)?.toLong()

        if (currentMin != null && amount < currentMin) return
        if (currentMax != null && amount > currentMax) return

        val event = PaymentEvent(
            amount = amount,
            timestamp = System.currentTimeMillis()
        )

        val json = Gson().toJson(event)
        val key = prefs.getString("aes_key", "1234567890123456") ?: "1234567890123456"
        val encrypted = encrypt(json, key)
        val url = prefs.getString("server_url", "") ?: ""

        sendToServer(encrypted, url)
    }

    private fun parseAmount(text: String?): Long? {
        if (text.isNullOrEmpty()) return null
        try {
            val cleaned = text.replace("[\\s\u00A0]".toRegex(), "")
            val regex = Regex("(\\d+[.,]?\\d*)\\s*₽")
            val match = regex.find(cleaned) ?: return null
            val valueStr = match.groupValues[1].replace(",", ".")
            val value = valueStr.toDouble()
            val ret = (value * 100).toLong()
            return ret
        } catch (e: Exception) {
            Log.e("PARSE", "Failed to parse amount", e)
            return null
        }
    }

    data class PaymentEvent(
        val amount: Long,
        val timestamp: Long
    )

    private fun encrypt(json: String, key: String): String {
        return try {
            val secretKey = SecretKeySpec(key.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            Base64.encodeToString(cipher.doFinal(json.toByteArray()), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("ENCRYPT", "Error encrypting", e)
            json
        }
    }

    private fun sendToServer(json: String, serverUrl: String) {
        if (serverUrl.isBlank()) {
            Log.e("HTTP", "Server URL empty, skipping send")
            return
        }

        val client = unsafeClient()//val client = OkHttpClient()
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(serverUrl).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HTTP", "Send failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("HTTP", "Response code: ${response.code}")
                response.close()
            }
        })
    }

    fun unsafeClient(): OkHttpClient {
        val trustAll = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            }
        )
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAll, java.security.SecureRandom())
        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAll[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }
}