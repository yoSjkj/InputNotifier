package com.hereng.inputnotifier

import android.content.Context
import android.provider.Settings
import androidx.core.content.edit

/** 현재 연결된 블루투스 기기(MAC 주소 → 이름)를 기록한다. 리시버가 상주하지 않아 디스크에 둔다. */
class ConnectionStore(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        // 재부팅하면 연결 해제 이벤트를 못 받으므로 이전 부팅의 기록은 버린다
        val bootCount =
            Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, 0)
        if (prefs.getInt(KEY_BOOT_COUNT, -1) != bootCount) {
            prefs.edit {
                clear()
                putInt(KEY_BOOT_COUNT, bootCount)
            }
        }
    }

    fun setConnected(address: String, name: String) {
        prefs.edit { putString(address, name) }
    }

    fun setDisconnected(address: String) {
        prefs.edit { remove(address) }
    }

    /** 연결된 기기의 MAC 주소 → 이름 */
    fun connected(): Map<String, String> =
        prefs.all.filterKeys { it != KEY_BOOT_COUNT }.mapValues { it.value.toString() }

    private companion object {
        const val PREFS_NAME = "connected_devices"
        const val KEY_BOOT_COUNT = "boot_count"
    }
}
