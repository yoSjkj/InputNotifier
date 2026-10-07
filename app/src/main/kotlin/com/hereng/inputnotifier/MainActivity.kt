package com.hereng.inputnotifier

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hereng.inputnotifier.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var store: DeviceStore
    private val adapter = DeviceAdapter(::showTypeDialog)

    private var requestedFromButton = false

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            // 다시 묻지 않음 상태면 시스템 대화상자가 안 뜨므로 앱 설정으로 보낸다
            val permanentlyDenied = !hasBluetoothPermission() &&
                !shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT)
            if (requestedFromButton && permanentlyDenied) openAppSettings()
            requestedFromButton = false
            refresh()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        store = DeviceStore(this)
        binding.deviceList.adapter = adapter
        binding.grantButton.setOnClickListener {
            requestedFromButton = true
            requestPermission.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
        }
        binding.notificationBanner.setOnClickListener { openNotificationSettings() }

        if (savedInstanceState == null) {
            val missing = buildList {
                if (!hasBluetoothPermission()) add(Manifest.permission.BLUETOOTH_CONNECT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !hasPermission(Manifest.permission.POST_NOTIFICATIONS)
                ) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            if (missing.isNotEmpty()) requestPermission.launch(missing.toTypedArray())
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun hasBluetoothPermission() = hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    @SuppressLint("MissingPermission")
    private fun refresh() {
        binding.notificationBanner.isVisible =
            !NotificationManagerCompat.from(this).areNotificationsEnabled()

        if (!hasBluetoothPermission()) {
            showEmpty(R.string.empty_need_permission, showGrantButton = true)
            return
        }
        val bluetooth = getSystemService(BluetoothManager::class.java)?.adapter
        if (bluetooth == null) {
            showEmpty(R.string.empty_no_bluetooth)
            return
        }
        if (!bluetooth.isEnabled) {
            showEmpty(R.string.empty_bluetooth_off)
            return
        }

        val devices = bluetooth.bondedDevices.orEmpty()
        val items = devices
            .map { device ->
                DeviceItem(
                    address = device.address,
                    name = device.displayName(),
                    type = store.getType(device.address),
                )
            }
            .sortedBy { it.name.lowercase() }
        syncNotification(devices)
        if (items.isEmpty()) {
            showEmpty(R.string.empty_no_devices)
            return
        }

        binding.emptyView.isVisible = false
        binding.deviceList.isVisible = true
        adapter.submitList(items)
    }

    /** 리시버가 놓친 변화(종류 지정, 강제 종료 후 재실행 등)를 실제 연결 상태에 맞춘다. */
    private fun syncNotification(devices: Collection<BluetoothDevice>) {
        val connections = ConnectionStore(this)
        for (device in devices) {
            when (device.isConnectedOrNull()) {
                true -> connections.setConnected(device.address, device.displayName())
                false -> connections.setDisconnected(device.address)
                null -> Unit
            }
        }
        ConnectionNotifier.update(this)
    }

    private fun showEmpty(@StringRes message: Int, showGrantButton: Boolean = false) {
        adapter.submitList(emptyList())
        binding.deviceList.isVisible = false
        binding.emptyView.isVisible = true
        binding.emptyText.setText(message)
        binding.grantButton.isVisible = showGrantButton
    }

    private fun showTypeDialog(item: DeviceItem) {
        val choices: List<DeviceType?> = listOf(null) + DeviceType.entries
        val labels = choices
            .map { getString(it?.labelRes ?: R.string.type_none) }
            .toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle(item.name)
            .setSingleChoiceItems(labels, choices.indexOf(item.type)) { dialog, which ->
                store.setType(item.address, choices[which])
                dialog.dismiss()
                refresh()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openNotificationSettings() {
        startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        )
    }

    private fun openAppSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            )
        )
    }
}
