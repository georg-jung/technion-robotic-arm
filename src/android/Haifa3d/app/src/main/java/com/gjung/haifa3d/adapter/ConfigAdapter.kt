package com.gjung.haifa3d.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gjung.haifa3d.ble.IConfigField
import com.gjung.haifa3d.databinding.ConfigItemBinding

@ExperimentalUnsignedTypes
class ConfigAdapter(var fields: List<IConfigField>): RecyclerView.Adapter<ConfigAdapter.ViewHolder>() {
    var onItemClickListener: OnItemClickListener? = null
    var onItemEditClickListener: OnItemClickListener? = null

    @FunctionalInterface
    interface OnItemClickListener {
        fun onItemClick(field: IConfigField)
    }

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(val binding: ConfigItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var caption: String
            get() = binding.configName.text.toString()
            set(value) { binding.configName.text = value }

        var value: String
            get() = binding.configSubtitle.text.toString()
            set(value) { binding.configSubtitle.text = value }

        init {
            binding.configContainer.setOnClickListener {
                onItemClickListener?.onItemClick(fields[adapterPosition])
            }

            binding.editButton.setOnClickListener {
                onItemEditClickListener?.onItemClick(fields[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ConfigItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = fields.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val field = fields[position]
        holder.caption = field.caption
        holder.value = field.value.value?.toString() ?: "-- ? --"
    }

    override fun getItemId(position: Int): Long = position.toLong()
}