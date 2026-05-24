package com.beautybooking.app.client

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beautybooking.app.R
import com.beautybooking.app.models.Service

class ServiceAdapter(
    private val services: List<Service>,
    private val onSelect: (Service, Int) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.VH>() {

    private var selectedPosition = -1

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivServiceIcon)
        val tvName: TextView = view.findViewById(R.id.tvServiceName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = services[position]; val ctx = holder.itemView.context
        holder.ivIcon.setImageResource(s.iconResId)
        holder.tvName.text = s.getLocalizedName(ctx)
        holder.itemView.setBackgroundResource(
            if (position == selectedPosition) R.drawable.bg_service_card_selected else R.drawable.bg_service_card
        )
        holder.itemView.setOnClickListener {
            val old = selectedPosition; selectedPosition = holder.adapterPosition
            if (old >= 0) notifyItemChanged(old); notifyItemChanged(selectedPosition)
            onSelect(s, selectedPosition)
        }
        holder.itemView.contentDescription = if (position == selectedPosition)
            ctx.getString(R.string.a11y_service_selected, s.getLocalizedName(ctx))
        else ctx.getString(R.string.a11y_service_not_selected, s.getLocalizedName(ctx))
    }

    override fun getItemCount() = services.size
}
