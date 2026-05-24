package com.beautybooking.app.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beautybooking.app.R
import com.beautybooking.app.models.Appointment
import com.beautybooking.app.models.Service
import com.beautybooking.app.utils.Constants
import com.google.android.material.button.MaterialButton

class AppointmentAdapter(
    private var appointments: MutableList<Appointment>,
    private val onApprove: (Appointment) -> Unit,
    private val onReject: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivServiceIcon)
        val tvName: TextView = view.findViewById(R.id.tvServiceName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvClient: TextView = view.findViewById(R.id.tvClientName)
        val tvPhone: TextView = view.findViewById(R.id.tvClientPhone)
        val tvEmail: TextView = view.findViewById(R.id.tvClientEmail)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val llActions: LinearLayout = view.findViewById(R.id.llActions)
        val btnApprove: MaterialButton = view.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = appointments[position]; val ctx = holder.itemView.context

        val service = Service.getAllServices().firstOrNull { it.key == a.serviceKey }
        if (service != null) holder.ivIcon.setImageResource(service.iconResId)
        else holder.ivIcon.setImageResource(R.drawable.ic_service_haircut)

        holder.tvName.text = a.getDisplayService()

        when (a.status) {
            Appointment.STATUS_PENDING -> {
                holder.tvStatus.text = ctx.getString(R.string.status_pending_label)
                holder.tvStatus.setTextColor(ctx.getColor(R.color.status_pending))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                holder.llActions.visibility = View.VISIBLE
            }
            Appointment.STATUS_APPROVED -> {
                holder.tvStatus.text = ctx.getString(R.string.status_approved_label)
                holder.tvStatus.setTextColor(ctx.getColor(R.color.status_approved))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_approved)
                holder.llActions.visibility = View.GONE
            }
            Appointment.STATUS_CANCELLED -> {
                holder.tvStatus.text = ctx.getString(R.string.status_cancelled_label)
                holder.tvStatus.setTextColor(ctx.getColor(R.color.text_hint))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                holder.llActions.visibility = View.GONE
            }
            else -> {
                holder.tvStatus.text = ctx.getString(R.string.status_rejected_label)
                holder.tvStatus.setTextColor(ctx.getColor(R.color.status_rejected))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                holder.llActions.visibility = View.GONE
            }
        }

        holder.tvClient.text = a.clientName; holder.tvPhone.text = a.clientPhone; holder.tvEmail.text = a.clientEmail
        val locDisplay = if (a.location.isNotEmpty()) "${a.getLocationDisplay()} \u2022 " else ""
        holder.tvDate.text = "$locDisplay${Constants.formatDateForDisplay(a.date)}"; holder.tvTime.text = a.time
        holder.btnApprove.setOnClickListener { onApprove(a) }; holder.btnReject.setOnClickListener { onReject(a) }
    }

    override fun getItemCount() = appointments.size
    fun updateData(newData: List<Appointment>) { appointments.clear(); appointments.addAll(newData); notifyDataSetChanged() }
}
