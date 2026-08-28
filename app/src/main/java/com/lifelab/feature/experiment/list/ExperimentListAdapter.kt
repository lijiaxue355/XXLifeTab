package com.lifelab.feature.experiment.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifelab.R
import com.lifelab.databinding.ItemExperimentBinding
import java.util.concurrent.TimeUnit

class ExperimentListAdapter(
    private val onExperimentClick: (Long) -> Unit = {},
    private val onDeleteClick: (ExperimentListItem) -> Unit = {},
) : ListAdapter<ExperimentListItem, ExperimentListAdapter.ExperimentViewHolder>(DiffCallback) {

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

        fun bind(item: ExperimentListItem) {
            val experimentWithMetrics = item.experiment
            val experiment = experimentWithMetrics.experiment
            val totalDays = experiment.durationDays.coerceAtLeast(1)
            val elapsedDays = (
                TimeUnit.MILLISECONDS.toDays(
                    (System.currentTimeMillis() - experiment.startDateMillis).coerceAtLeast(0L)
                ) + 1
            ).toInt().coerceIn(1, totalDays)
            val context = binding.root.context

            binding.experimentName.text = experiment.name
            binding.metricCount.text = context.getString(
                R.string.metric_count_format,
                experimentWithMetrics.metrics.size,
            )
            binding.experimentProgress.progress =
                item.completionPercent
            binding.completionText.text = context.getString(
                R.string.completion_format,
                item.completionPercent,
            )

            binding.recordStatus.text = if (item.isCompleted) {
                "已完成"
            } else {
                "进行中"
            }
            binding.recordStatus.setBackgroundResource(
                if (item.isCompleted) {
                    R.drawable.bg_success_chip
                } else {
                    R.drawable.bg_info_chip
                },
            )
            binding.recordStatus.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (item.isCompleted) {
                        R.color.lifelab_success_text
                    } else {
                        R.color.lifelab_info_text
                    },
                ),
            )

            if (item.isCompleted) {
                binding.experimentPhase.text = "实验已结束"
            } else if (elapsedDays <= experiment.baselineDays) {
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
            binding.moreButton.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ExperimentListItem>() {
        override fun areItemsTheSame(
            oldItem: ExperimentListItem,
            newItem: ExperimentListItem,
        ): Boolean = oldItem.experiment.experiment.id ==
            newItem.experiment.experiment.id

        override fun areContentsTheSame(
            oldItem: ExperimentListItem,
            newItem: ExperimentListItem,
        ): Boolean = oldItem == newItem
    }
}
