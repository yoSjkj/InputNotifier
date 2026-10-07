package com.hereng.inputnotifier

import android.content.Context
import androidx.core.content.edit

/** 기기 MAC 주소별로 지정된 종류를 SharedPreferences에 저장한다. */
class DeviceStore(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getType(address: String): DeviceType? {
        val name = prefs.getString(address, null) ?: return null
        return DeviceType.entries.find { it.name == name }
    }

    fun setType(address: String, type: DeviceType?) {
        prefs.edit {
            if (type == null) remove(address) else putString(address, type.name)
        }
    }

    private companion object {
        const val PREFS_NAME = "device_types"
    }
}
