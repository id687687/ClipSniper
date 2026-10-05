package com.kolin.clipsniper.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kolin.clipsniper.R
import com.kolin.clipsniper.data.CleanedItem

class SnippetCheckAdapter(
    private val list: List<CleanedItem>,
    private val onCheckChanged: () -> Unit
) : RecyclerView.Adapter<SnippetCheckAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.itemCheckBox)
        val tvContent: TextView = view.findViewById(R.id.itemContent)
        val tvBadge: TextView = view.findViewById(R.id.itemBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_snippet_check, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvContent.text = item.cleaned
        holder.checkBox.isChecked = item.isSelected

        // 如果识别并清洗了时间/昵称，打上贴心小标签
        if (item.isCleaned) {
            holder.tvBadge.visibility = View.VISIBLE
            holder.tvBadge.text = "已滤昵称时间"
        } else {
            holder.tvBadge.visibility = View.GONE
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
            onCheckChanged()
        }

        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    override fun getItemCount(): Int = list.size
}
