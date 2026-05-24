package com.beautybooking.app.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beautybooking.app.R

data class CalendarDay(
    val day: Int, val month: Int, val year: Int,
    val isToday: Boolean = false, val hasAppointments: Boolean = false
)

class CalendarAdapter(
    private var days: List<CalendarDay>,
    private val onClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.VH>() {

    private var selectedPosition = -1

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDayNumber)
        val dot: View = view.findViewById(R.id.dotIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val day = days[position]
        if (day.day == 0) {
            holder.tvDay.text = ""; holder.tvDay.background = null
            holder.dot.visibility = View.INVISIBLE
            holder.itemView.isClickable = false; return
        }
        holder.tvDay.text = day.day.toString()
        when {
            position == selectedPosition -> {
                holder.tvDay.setBackgroundResource(R.drawable.bg_calendar_selected)
                holder.tvDay.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            }
            day.isToday -> {
                holder.tvDay.setBackgroundResource(R.drawable.bg_icon_circle)
                holder.tvDay.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            }
            else -> {
                holder.tvDay.background = null
                holder.tvDay.setTextColor(holder.itemView.context.getColor(R.color.text_primary))
            }
        }
        holder.dot.visibility = if (day.hasAppointments) View.VISIBLE else View.INVISIBLE
        holder.itemView.setOnClickListener {
            val old = selectedPosition; selectedPosition = holder.adapterPosition
            if (old >= 0) notifyItemChanged(old); notifyItemChanged(selectedPosition)
            onClick(day)
        }
    }

    override fun getItemCount() = days.size
    fun updateDays(newDays: List<CalendarDay>) { days = newDays; selectedPosition = -1; notifyDataSetChanged() }
}
