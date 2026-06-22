package com.king.kevin.tiroparabolico.presentation.screens

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.king.kevin.tiroparabolico.core.utils.toDisplay
import com.king.kevin.tiroparabolico.databinding.ItemExperimentBinding
import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExperimentHistoryAdapter(
    private val onExperimentClick: (ProjectileExperiment) -> Unit
) : ListAdapter<ProjectileExperiment, ExperimentHistoryAdapter.ExperimentViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperimentViewHolder {
        val binding = ItemExperimentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExperimentViewHolder(binding, onExperimentClick)
    }

    override fun onBindViewHolder(holder: ExperimentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExperimentViewHolder(
        private val binding: ItemExperimentBinding,
        private val onExperimentClick: (ProjectileExperiment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(experiment: ProjectileExperiment) {
            binding.experimentTitle.text = "v0 ${experiment.initialVelocity.toDisplay()} m/s | ${experiment.launchAngleDegrees.toDisplay()} grados"
            binding.experimentDetails.text = "R ${experiment.horizontalRange.toDisplay()} m | H ${experiment.maxHeight.toDisplay()} m | g ${experiment.gravity.toDisplay()} m/s2"
            binding.experimentDate.text = formatter.format(Date(experiment.createdAtMillis))
            binding.root.setOnClickListener { onExperimentClick(experiment) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ProjectileExperiment>() {
        override fun areItemsTheSame(oldItem: ProjectileExperiment, newItem: ProjectileExperiment): Boolean {
            return oldItem.id == newItem.id && oldItem.createdAtMillis == newItem.createdAtMillis
        }

        override fun areContentsTheSame(oldItem: ProjectileExperiment, newItem: ProjectileExperiment): Boolean {
            return oldItem == newItem
        }
    }
}
