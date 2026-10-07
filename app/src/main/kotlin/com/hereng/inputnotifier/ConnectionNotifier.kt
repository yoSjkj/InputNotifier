package com.hereng.inputnotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

/**
 * 지정된 기기의 연결 상태를 상태표시줄 아이콘으로 표시한다.
 *
 * 기기마다 알림을 따로 띄우면 시스템이 같은 앱 알림을 하나로 묶어 아이콘이 하나만 남으므로,
 * 알림은 하나만 쓰고 연결된 종류 조합에 맞는 아이콘으로 바꿔 준다.
 */
object ConnectionNotifier {
    private const val CHANNEL_ID = "connection"
    private const val NOTIFICATION_ID = 1

    /** 저장된 연결 기록과 종류 지정에 맞춰 알림을 다시 그린다. */
    fun update(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val deviceStore = DeviceStore(context)
        val devices = ConnectionStore(context).connected()
            .mapNotNull { (address, name) -> deviceStore.getType(address)?.let { it to name } }
            .sortedWith(compareBy({ it.first }, { it.second.lowercase() }))
        if (devices.isEmpty()) {
            manager.cancel(NOTIFICATION_ID)
            return
        }

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_connection),
                NotificationManager.IMPORTANCE_LOW,
            )
        )

        val types = devices.map { it.first }.distinct()
        val typeLabels = types.joinToString(" · ") { context.getString(it.labelRes) }
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconFor(types.toSet()))
            .setContentTitle(context.getString(R.string.notification_connected, typeLabels))
            .setContentText(devices.joinToString(", ") { it.second })
            .setContentIntent(openApp)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    @DrawableRes
    private fun iconFor(types: Set<DeviceType>): Int {
        val keyboard = DeviceType.KEYBOARD in types
        val mouse = DeviceType.MOUSE in types
        val other = DeviceType.OTHER in types
        return when {
            keyboard && mouse && other -> R.drawable.ic_stat_all
            keyboard && mouse -> R.drawable.ic_stat_keyboard_mouse
            keyboard && other -> R.drawable.ic_stat_keyboard_other
            mouse && other -> R.drawable.ic_stat_mouse_other
            else -> types.first().iconRes
        }
    }
}
