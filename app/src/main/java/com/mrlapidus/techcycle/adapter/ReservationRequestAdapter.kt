package com.mrlapidus.techcycle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.model.ReservationRequest
import java.text.SimpleDateFormat
import java.util.*

class ReservationRequestAdapter(
    private val reservationList: List<ReservationRequest>,
    private val onAcceptClick: (ReservationRequest) -> Unit,
    private val onRejectClick: (ReservationRequest) -> Unit
) : RecyclerView.Adapter<ReservationRequestAdapter.ReservationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation_request, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reserva = reservationList[position]

        // Cargar nombre del comprador + avatar
        val userId = reserva.buyerId

        val userRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombreCompleto").getValue(String::class.java) ?: "Usuario"
                holder.textNombreComprador.text = nombre

                val avatarUrl = snapshot.child("urlAvatar").getValue(String::class.java) ?: ""
                Glide.with(holder.itemView.context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.avatar_profile)
                    .circleCrop()
                    .into(holder.imageAvatar)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Fecha de reserva
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaTexto = sdf.format(Date(reserva.fecha))
        holder.textFechaReserva.text = "Fecha reserva: $fechaTexto"

        // Estado de la reserva
        holder.textEstado.text = "Estado: ${reserva.estado}"

        // Color del estado
        when (reserva.estado) {
            "aceptado" -> holder.textEstado.setTextColor(holder.itemView.context.getColor(R.color.green))
            "rechazado" -> holder.textEstado.setTextColor(holder.itemView.context.getColor(R.color.red))
            else -> holder.textEstado.setTextColor(holder.itemView.context.getColor(R.color.black))
        }

        // Botones
        holder.btnAceptar.setOnClickListener {
            onAcceptClick(reserva)
        }

        holder.btnRechazar.setOnClickListener {
            onRejectClick(reserva)
        }

        // Mostrar / ocultar botones según el estado actual
        if (reserva.estado == "aceptado") {
            holder.btnAceptar.visibility = View.GONE
            holder.btnRechazar.visibility = View.VISIBLE
        } else if (reserva.estado == "rechazado") {
            holder.btnAceptar.visibility = View.VISIBLE
            holder.btnRechazar.visibility = View.VISIBLE
        } else {
            // Estado pendiente
            holder.btnAceptar.visibility = View.VISIBLE
            holder.btnRechazar.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = reservationList.size

    class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageAvatar: ImageView = itemView.findViewById(R.id.imageAvatar)
        val textNombreComprador: TextView = itemView.findViewById(R.id.textNombreComprador)
        val textFechaReserva: TextView = itemView.findViewById(R.id.textFechaReserva)
        val textEstado: TextView = itemView.findViewById(R.id.textEstado)
        val btnAceptar: Button = itemView.findViewById(R.id.btnAceptar)
        val btnRechazar: Button = itemView.findViewById(R.id.btnRechazar)
    }
}


