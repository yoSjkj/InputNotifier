package com.hereng.inputnotifier

import androidx.annotation.StringRes

enum class DeviceType(@StringRes val labelRes: Int) {
    KEYBOARD(R.string.type_keyboard),
    MOUSE(R.string.type_mouse),
    OTHER(R.string.type_other),
}
