package com.collage.new_strishakti.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.collage.new_strishakti.R
// Use the same ReportReason as in your data.model.post
import com.collage.new_strishakti.data.model.post.ReportReason

class ReportReasonAdapter(
    private val reasons: List<ReportReason>
) : RecyclerView.Adapter<ReportReasonAdapter.ViewHolder>() {

    private var selectedPosition: Int = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)
        val container: LinearLayout = itemView.findViewById(R.id.itemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_reason, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reason = reasons[position]
        holder.title.text = reason.title
        holder.description.text = reason.description
        holder.radioButton.isChecked = position == selectedPosition

        holder.container.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }

        holder.radioButton.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int = reasons.size

    fun getSelectedItem(): ReportReason? {
        return if (selectedPosition != -1) reasons[selectedPosition] else null
    }
}
