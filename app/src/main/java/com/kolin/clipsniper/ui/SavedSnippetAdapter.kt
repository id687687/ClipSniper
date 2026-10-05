package com.kolin.clipsniper.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kolin.clipsniper.R
import com.kolin.clipsniper.data.Snippet

class SavedSnippetAdapter(
    private val list: List<Snippet>,
    private val onCopy: (Snippet) -> Unit,
    private val onDelete: (Snippet) -> Unit
) : RecyclerView.Adapter<SavedSnippetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvSavedContent)
        val tvDate: TextView = view.findViewById(R.id.tvSavedDate)
        val btnCopy: ImageButton = view.findViewById(R.id.btnItemCopy)
        val btnDelete: ImageButton = view.findViewById(R.id.btnItemDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_snippet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvContent.text = item.content
        holder.tvDate.text = item.getFormattedDate()

        holder.btnCopy.setOnClickListener { onCopy(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = list.size
}
