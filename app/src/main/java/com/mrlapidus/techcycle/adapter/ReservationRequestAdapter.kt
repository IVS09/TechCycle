package com.mrlapidus.techcycle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.model.ReservationRequest
import java.text.SimpleDateFormat
import java.util.*

class ReservationRequestAdapter(
    private val reservationList: List<ReservationRequest>,
    private val onAcceptClick: (ReservationRequest) -> Unit,
    private val onRejectClick: (ReservationRequest) -> Unit
) : RecyclerView.Adapter<ReservationRequestAdapter.ReservationViewHolder>() {

    inner class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageAvatar: ImageView = itemView.findViewById(R.id.imageAvatar)
        val textNombreComprador: TextView = itemView.findViewById(R.id.textNombreComprador)
        val textFechaReserva: TextView = itemView.findViewById(R.id.textFechaReserva)
        val textEstado: TextView = itemView.findViewById(R.id.textEstado)
        val btnAceptar: Button = itemView.findViewById(R.id.btnAceptar)
        val btnRechazar: Button = itemView.findViewById(R.id.btnRechazar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation_request, parent, false)
        return ReservationViewHolder(view)
    }

    override fun getItemCount(): Int = reservationList.size

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reserva = reservationList[position]

        // Formatear la fecha
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaTexto = sdf.format(Date(reserva.fecha))

        holder.textFechaReserva.text = "Fecha de reserva: $fechaTexto"
        holder.textEstado.text = "Estado: ${reserva.estado}"

        // Avatar
        Glide.with(holder.itemView.context)
            .load(R.drawable.ic_profile)
            .circleCrop()
            .into(holder.imageAvatar)

        holder.textNombreComprador.text = "Usuario"


        // Botón Aceptar visible solo si pendiente
        holder.btnAceptar.visibility = if (reserva.estado == "pendiente") View.VISIBLE else View.GONE

        // Botón Rechazar siempre visible
        holder.btnRechazar.visibility = View.VISIBLE

        // Click listeners
        holder.btnAceptar.setOnClickListener { onAcceptClick(reserva) }
        holder.btnRechazar.setOnClickListener { onRejectClick(reserva) }
    }
}





