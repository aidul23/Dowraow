package com.aidul23.dowraow.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aidul23.dowraow.R
import com.aidul23.dowraow.db.Run
import com.aidul23.dowraow.utility.TrackingUtility
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter: RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.ivRunImage)
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val avgSpeed: TextView = itemView.findViewById(R.id.tvAvgSpeed)
        val distanceInMeter: TextView = itemView.findViewById(R.id.tvDistance)
        val caloriesBurned: TextView = itemView.findViewById(R.id.tvCalories)
        val time: TextView = itemView.findViewById(R.id.tvTime)
    }

    val diffCallBack = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this,diffCallBack)

    fun submitList(list: List<Run>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_run,parent,false)
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.img).into(holder.img)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp!!
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy",Locale.getDefault())
            holder.date.text = dateFormat.format(calendar.time)

            holder.avgSpeed.text = "${run.avgSpeedInKMH}km/h"

            val distanceInKm = "${run.distanceInMeter?.div(1000f)}km"
            holder.distanceInMeter.text = distanceInKm

            holder.caloriesBurned.text = "${run.caloriesBurned}kcal"

            holder.time.text = "${run.timeInMillis?.let {
                TrackingUtility.getFormattedStopWatchTime(
                    it
                )
            }}"
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}