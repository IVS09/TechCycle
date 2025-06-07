package com.mrlapidus.techcycle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrlapidus.techcycle.R
import com.mrlapidus.techcycle.Utilities
import com.mrlapidus.techcycle.databinding.ItemReservationRequestBinding
import com.mrlapidus.techcycle.model.ReservationRequest

class ReservationRequestAdapter(
    private val reservationList: List<ReservationRequest>,
    private val onAccept: (ReservationRequest) -> Unit,
    private val onReject: (ReservationRequest) -> Unit
) : RecyclerView.Adapter<ReservationRequestAdapter.ReservationRequestViewHolder>() {

    inner class ReservationRequestViewHolder(val binding: ItemReservationRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationRequestViewHolder {
        val binding = ItemReservationRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReservationRequestViewHolder(binding)
    }

    override fun getItemCount(): Int = reservationList.size

    override fun onBindViewHolder(holder: ReservationRequestViewHolder, position: Int) {
        val request = reservationList[position]

        holder.binding.textBuyerName.text = request.buyerName
        holder.binding.textFechaReserva.text = "Fecha reserva: ${Utilities.formatTimestampToDate(request.fecha)}"
        holder.binding.textEstadoReserva.text = "Estado: ${request.estado}"

        Glide.with(holder.itemView.context)
            .load(request.buyerAvatarUrl)
            .placeholder(R.drawable.ic_profile)
            .into(holder.binding.imageAvatar)

        // Control de botones
        if (request.estado == "aceptado") {
            holder.binding.btnAceptar.visibility = View.GONE
        } else {
            holder.binding.btnAceptar.visibility = View.VISIBLE
        }

        holder.binding.btnAceptar.setOnClickListener {
            onAccept(request)
        }

        holder.binding.btnRechazar.setOnClickListener {
            onReject(request)
        }
    }
}

