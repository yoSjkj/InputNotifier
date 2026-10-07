package com.hereng.inputnotifier

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hereng.inputnotifier.databinding.ItemDeviceBinding

data class DeviceItem(val address: String, val name: String, val type: DeviceType?)

class DeviceAdapter(private val onClick: (DeviceItem) -> Unit) :
    ListAdapter<DeviceItem, DeviceAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeviceItem) {
            binding.deviceName.text = item.name
            binding.deviceAddress.text = item.address
            binding.deviceType.setText(item.type?.labelRes ?: R.string.type_none)
            binding.deviceType.alpha = if (item.type == null) 0.5f else 1f
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemDeviceBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<DeviceItem>() {
            override fun areItemsTheSame(oldItem: DeviceItem, newItem: DeviceItem) =
                oldItem.address == newItem.address

            override fun areContentsTheSame(oldItem: DeviceItem, newItem: DeviceItem) =
                oldItem == newItem
        }
    }
}
