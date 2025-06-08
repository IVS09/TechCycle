package com.mrlapidus.techcycle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.databinding.ItemReservationRequestBinding
import com.mrlapidus.techcycle.model.ReservationRequest

class ReservationRequestAdapter(
    private val reservationList: List<ReservationRequest>,
    private val onAcceptClick: (ReservationRequest) -> Unit,
    private val onRejectClick: (ReservationRequest) -> Unit
) : RecyclerView.Adapter<ReservationRequestAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemReservationRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReservationRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = reservationList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reserva = reservationList[position]

        // Fecha
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val fechaStr = dateFormat.format(java.util.Date(reserva.fecha))
        holder.binding.textFechaReserva.text = holder.itemView.context.getString(R.string.reservation_date_format, fechaStr)

        // Estado
        holder.binding.textEstado.text = reserva.estado
        when (reserva.estado.lowercase()) {
            "aceptado" -> {
                holder.binding.textEstado.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
                holder.binding.btnAceptar.visibility = View.GONE
                holder.binding.btnRechazar.visibility = View.VISIBLE
            }
            "rechazado" -> {
                holder.binding.textEstado.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
                holder.binding.btnAceptar.visibility = View.VISIBLE
                holder.binding.btnRechazar.visibility = View.GONE
            }
            else -> {
                holder.binding.textEstado.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_gray_accessible))
                holder.binding.btnAceptar.visibility = View.VISIBLE
                holder.binding.btnRechazar.visibility = View.VISIBLE
            }
        }

        // Botones
        holder.binding.btnAceptar.setOnClickListener {
            onAcceptClick(reserva)
        }
        holder.binding.btnRechazar.setOnClickListener {
            onRejectClick(reserva)
        }

        // Avatar → de momento imagen por defecto
        holder.binding.imageAvatar.setImageResource(R.drawable.ic_profile)

        // Nombre comprador (por defecto, porque no lo tenemos en ReservationRequest)
        holder.binding.textNombreComprador.text = holder.itemView.context.getString(R.string.default_username)
    }
}



