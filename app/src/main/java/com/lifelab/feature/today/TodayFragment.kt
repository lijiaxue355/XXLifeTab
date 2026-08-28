package com.lifelab.feature.today

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.lifelab.LifeLabApplication
import com.lifelab.R
import com.lifelab.databinding.FragmentTodayBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding: FragmentTodayBinding
        get() = checkNotNull(_binding)

    private val viewModel: TodayViewModel by viewModels {
        val application =
            requireActivity().application as LifeLabApplication

        TodayViewModel.provideFactory(
            experimentRepository =
                application.experimentRepository,
            recordRepository =
                application.recordRepository,
        )
    }

    private val todayAdapter by lazy {
        TodayExperimentAdapter(
            onRecordClick = ::showRecordDialog,
            onReportClick = { item ->
                findNavController().navigate(
                    TodayFragmentDirections
                        .actionTodayFragmentToReportFragment(
                            item.experiment.experiment.id,
                        ),
                )
            },
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTodayBinding.inflate(
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

        binding.todayDate.text = SimpleDateFormat(
            "M 月 d 日 · EEEE",
            Locale.CHINA,
        ).format(Date())

        binding.todayExperimentList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = todayAdapter
        }

        binding.viewAllExperiments.setOnClickListener {
            findNavController().navigate(
                R.id.navigation_graph_lab,
            )
        }

        observeUi()
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                launch {
                    viewModel.uiState.collect(::render)
                }

                launch {
                    viewModel.uiEffect.collect { effect ->
                        when (effect) {
                            is TodayUiEffect.ShowMessage -> {
                                showMessage(effect.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun render(state: TodayUiState) = with(binding) {
        emptyStateMessage.isVisible = state.items.isEmpty()
        todayExperimentList.isVisible = state.items.isNotEmpty()
        todayAdapter.submitList(state.items)
    }

    private fun showRecordDialog(item: TodayExperimentItem) {
        val context = requireContext()
        val experiment = item.experiment

        val formContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                24.dp(),
                8.dp(),
                24.dp(),
                8.dp(),
            )
        }

        val metricInputs =
            linkedMapOf<Long, TextInputEditText>()

        experiment.metrics
            .sortedBy { it.sortOrder }
            .forEach { metric ->
                val input = TextInputEditText(context).apply {
                    setText(item.values[metric.id].orEmpty())
                    inputType = when (metric.type) {
                        "YES_NO" -> InputType.TYPE_CLASS_TEXT

                        else ->
                            InputType.TYPE_CLASS_NUMBER or
                                InputType.TYPE_NUMBER_FLAG_DECIMAL
                    }
                }

                val inputLayout = TextInputLayout(context).apply {
                    hint = if (metric.required) {
                        "${metric.name} *"
                    } else {
                        metric.name
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        topMargin = 8.dp()
                    }
                    addView(input)
                }

                formContainer.addView(inputLayout)
                metricInputs[metric.id] = input
            }

        val noteInput = TextInputEditText(context).apply {
            setText(item.note)
            minLines = 2
            maxLines = 4
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        val noteLayout = TextInputLayout(context).apply {
            hint = "今日备注（可选）"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = 8.dp()
            }
            addView(noteInput)
        }

        formContainer.addView(noteLayout)

        val scrollView = ScrollView(context).apply {
            addView(formContainer)
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(
                if (item.hasRecord) {
                    "修改${experiment.experiment.name}"
                } else {
                    "记录${experiment.experiment.name}"
                },
            )
            .setView(scrollView)
            .setNegativeButton("取消", null)
            .setPositiveButton("保存") { _, _ ->
                val values = metricInputs.mapValues {
                    it.value.text
                        ?.toString()
                        ?.trim()
                        .orEmpty()
                }

                viewModel.dispatch(
                    TodayUiAction.SaveRecordClicked(
                        experimentId = experiment.experiment.id,
                        values = values,
                        note = noteInput.text
                            ?.toString()
                            ?.trim(),
                    ),
                )
            }
            .show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(
            binding.todayRoot,
            message,
            Snackbar.LENGTH_SHORT,
        )
            .setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.lifelab_text_primary,
                ),
            )
            .setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white,
                ),
            )
            .show()
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        binding.todayExperimentList.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
