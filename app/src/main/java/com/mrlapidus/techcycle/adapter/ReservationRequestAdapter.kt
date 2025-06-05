package com.mrlapidus.techcycle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrlapidus.techcycle.databinding.ItemReservationRequestBinding
import com.mrlapidus.techcycle.model.ReservationRequest
import java.text.SimpleDateFormat
import java.util.*

class ReservationRequestAdapter(
    private val requests: List<ReservationRequest>,
    private val onAccept: (ReservationRequest) -> Unit,
    private val onReject: (ReservationRequest) -> Unit
) : RecyclerView.Adapter<ReservationRequestAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(val binding: ItemReservationRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReservationRequestBinding.inflate(inflater, parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.binding.tvBuyerName.text = request.buyerName
        holder.binding.tvReservationDate.text = formatDate(request.fecha)
        holder.binding.tvReservationStatus.text = "Estado: ${request.estado}"

        Glide.with(holder.itemView.context)
            .load(request.buyerAvatarUrl)
            .into(holder.binding.ivBuyerAvatar)

        holder.binding.btnAccept.setOnClickListener { onAccept(request) }
        holder.binding.btnReject.setOnClickListener { onReject(request) }
    }

    override fun getItemCount(): Int = requests.size

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

