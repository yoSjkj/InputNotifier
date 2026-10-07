package com.hereng.inputnotifier

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

/** 기기 이름. 이름이 없거나 권한이 없으면 MAC 주소를 돌려준다. */
@SuppressLint("MissingPermission")
fun BluetoothDevice.displayName(): String =
    try {
        name
    } catch (e: SecurityException) {
        null
    } ?: address

/**
 * 현재 연결 여부. 공개 API가 없어 숨겨진 BluetoothDevice.isConnected()를 호출하고,
 * 호출할 수 없으면 null(알 수 없음)을 돌려준다.
 */
fun BluetoothDevice.isConnectedOrNull(): Boolean? =
    try {
        BluetoothDevice::class.java.getMethod("isConnected").invoke(this) as? Boolean
    } catch (e: ReflectiveOperationException) {
        null
    } catch (e: SecurityException) {
        null
    }
