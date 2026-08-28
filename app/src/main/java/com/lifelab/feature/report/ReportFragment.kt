package com.lifelab.feature.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.lifelab.LifeLabApplication
import com.lifelab.R
import com.lifelab.databinding.FragmentReportBinding
import kotlinx.coroutines.launch

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding: FragmentReportBinding
        get() = checkNotNull(_binding)

    private val args: ReportFragmentArgs by navArgs()

    private val viewModel: ReportViewModel by viewModels {
        val application =
            requireActivity().application as LifeLabApplication

        ReportViewModel.provideFactory(
            experimentId = args.experimentId,
            experimentRepository =
                application.experimentRepository,
            recordRepository =
                application.recordRepository,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentReportBinding.inflate(
            inflater,
            container,
            false,
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.reportToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: ReportUiState): Unit = with(binding) {
        reportLoading.isVisible = state.isLoading
        reportContent.isVisible = !state.isLoading

        if (state.isLoading) {
            return
        }

        reportExperimentName.text = state.experimentName
        reportCompletionPercent.text =
            "${state.completionPercent}%"
        reportCompletionProgress.progress =
            state.completionPercent
        reportCompletionDetail.text =
            "已完成 ${state.recordedDays} / ${state.totalDays} 天"

        reportEmptyMessage.isVisible = state.message != null
        reportEmptyMessage.text = state.message.orEmpty()

        val hasChartData = state.linePoints.isNotEmpty()
        reportCharts.isVisible = hasChartData

        if (hasChartData) {
            reportLineTitle.text = "${state.metricName}趋势"
            renderLineChart(state.linePoints)
            renderBarChart(
                baselineAverage = state.baselineAverage,
                interventionAverage = state.interventionAverage,
            )
        }
    }

    private fun renderLineChart(
        points: List<ReportPoint>,
    ) = with(binding.reportLineChart) {
        val entries = points.mapIndexed { index, point ->
            Entry(index.toFloat(), point.value)
        }

        val dataSet = LineDataSet(entries, "").apply {
            color = ContextCompat.getColor(
                requireContext(),
                R.color.lifelab_primary,
            )
            setCircleColor(color)
            lineWidth = 2.5f
            circleRadius = 4f
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(
                requireContext(),
                R.color.lifelab_primary_surface,
            )
        }

        data = LineData(dataSet)
        description.isEnabled = false
        legend.isEnabled = false
        axisRight.isEnabled = false
        setTouchEnabled(false)
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            valueFormatter = IndexAxisValueFormatter(
                points.map { it.label },
            )
        }
        animateY(300)
        invalidate()
    }

    private fun renderBarChart(
        baselineAverage: Float?,
        interventionAverage: Float?,
    ) = with(binding.reportBarChart) {
        val entries = buildList {
            baselineAverage?.let {
                add(BarEntry(0f, it))
            }
            interventionAverage?.let {
                add(BarEntry(1f, it))
            }
        }

        val dataSet = BarDataSet(entries, "").apply {
            setColors(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.lifelab_info,
                ),
                ContextCompat.getColor(
                    requireContext(),
                    R.color.lifelab_primary,
                ),
            )
            valueTextSize = 11f
        }

        data = BarData(dataSet).apply {
            barWidth = 0.45f
        }
        description.isEnabled = false
        legend.isEnabled = false
        axisRight.isEnabled = false
        setTouchEnabled(false)
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            axisMinimum = -0.5f
            axisMaximum = 1.5f
            setDrawGridLines(false)
            valueFormatter = IndexAxisValueFormatter(
                listOf("基线", "干预"),
            )
        }
        animateY(300)
        invalidate()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
