package com.lifelab.feature.today

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifelab.databinding.ItemTodayExperimentBinding
import com.lifelab.feature.record.data.local.entity.RecordSyncStatus

class TodayExperimentAdapter(
    private val onRecordClick: (TodayExperimentItem) -> Unit,
    private val onReportClick: (TodayExperimentItem) -> Unit,
) : ListAdapter<
        TodayExperimentItem,
        TodayExperimentAdapter.TodayExperimentViewHolder,
    >(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TodayExperimentViewHolder {
        val binding = ItemTodayExperimentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return TodayExperimentViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TodayExperimentViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    inner class TodayExperimentViewHolder(
        private val binding: ItemTodayExperimentBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TodayExperimentItem) = with(binding) {
            val experiment = item.experiment.experiment
            val metrics = item.experiment.metrics
                .sortedBy { it.sortOrder }

            experimentTitle.text = experiment.name
            metricCount.text = "${metrics.size} 项指标"
            metricNames.text = metrics.joinToString(
                prefix = "今日记录：",
                separator = "、",
            ) { metric ->
                metric.name
            }

            syncStatus.text = when (item.syncStatus) {
                RecordSyncStatus.PENDING ->
                    "本地已保存，等待同步"

                RecordSyncStatus.SYNCED ->
                    "已同步到服务端"

                else ->
                    "今天还没有记录"
            }

            recordButton.isEnabled = !item.isSaving
            recordButton.text = when {
                item.isSaving -> "保存中…"
                item.hasRecord -> "修改今日记录"
                else -> "开始记录"
            }

            recordButton.setOnClickListener {
                onRecordClick(item)
            }
            reportButton.setOnClickListener {
                onReportClick(item)
            }
        }
    }

    private object DiffCallback :
        DiffUtil.ItemCallback<TodayExperimentItem>() {

        override fun areItemsTheSame(
            oldItem: TodayExperimentItem,
            newItem: TodayExperimentItem,
        ): Boolean {
            return oldItem.experiment.experiment.id ==
                newItem.experiment.experiment.id
        }

        override fun areContentsTheSame(
            oldItem: TodayExperimentItem,
            newItem: TodayExperimentItem,
        ): Boolean = oldItem == newItem
    }
}
