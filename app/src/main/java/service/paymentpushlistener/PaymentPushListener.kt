package service.paymentpushlistener

import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class PaymentPushListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "ru.sberbankmobile") return
        val extras: Bundle = sbn.notification.extras ?: return
        val text: CharSequence = extras.getCharSequence("android.text") ?: return
        Log.d("SBER_PUSH", text.toString())
    }
}