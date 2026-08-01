package com.lifelab.feature.experiment.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifelab.R
import com.lifelab.databinding.ItemExperimentBinding
import com.lifelab.feature.experiment.data.local.relation.ExperimentWithMetrics
import java.util.concurrent.TimeUnit

class ExperimentListAdapter(
    private val onExperimentClick: (Long) -> Unit = {},
) : ListAdapter<ExperimentWithMetrics, ExperimentListAdapter.ExperimentViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperimentViewHolder {
        val binding = ItemExperimentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ExperimentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExperimentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExperimentViewHolder(
        private val binding: ItemExperimentBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExperimentWithMetrics) {
            val experiment = item.experiment
            val totalDays = experiment.durationDays.coerceAtLeast(1)
            val elapsedDays = (
                TimeUnit.MILLISECONDS.toDays(
                    (System.currentTimeMillis() - experiment.startDateMillis).coerceAtLeast(0L)
                ) + 1
            ).toInt().coerceIn(1, totalDays)
            val completion = (elapsedDays * 100 / totalDays).coerceIn(0, 100)
            val context = binding.root.context

            binding.experimentName.text = experiment.name
            binding.metricCount.text = context.getString(
                R.string.metric_count_format,
                item.metrics.size,
            )
            binding.experimentProgress.progress = completion
            binding.completionText.text = context.getString(
                R.string.completion_format,
                completion,
            )

            if (elapsedDays <= experiment.baselineDays) {
                binding.experimentPhase.text = context.getString(
                    R.string.phase_baseline_day_format,
                    elapsedDays,
                    experiment.baselineDays,
                )
            } else {
                val interventionDay = elapsedDays - experiment.baselineDays
                binding.experimentPhase.text = context.getString(
                    R.string.phase_intervention_day_format,
                    interventionDay,
                    experiment.interventionDays,
                )
            }

            binding.root.setOnClickListener {
                onExperimentClick(experiment.id)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ExperimentWithMetrics>() {
        override fun areItemsTheSame(
            oldItem: ExperimentWithMetrics,
            newItem: ExperimentWithMetrics,
        ): Boolean = oldItem.experiment.id == newItem.experiment.id

        override fun areContentsTheSame(
            oldItem: ExperimentWithMetrics,
            newItem: ExperimentWithMetrics,
        ): Boolean = oldItem == newItem
    }
}
