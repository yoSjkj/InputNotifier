package com.hereng.inputnotifier

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class DeviceType(@StringRes val labelRes: Int, @DrawableRes val iconRes: Int) {
    KEYBOARD(R.string.type_keyboard, R.drawable.ic_stat_keyboard),
    MOUSE(R.string.type_mouse, R.drawable.ic_stat_mouse),
    OTHER(R.string.type_other, R.drawable.ic_stat_devices_other),
}
