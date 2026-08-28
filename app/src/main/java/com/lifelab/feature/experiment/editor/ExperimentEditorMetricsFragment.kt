package com.lifelab.feature.experiment.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.lifelab.LifeLabApplication
import com.lifelab.R
import com.lifelab.databinding.FragmentEditorMetricsBinding

class ExperimentEditorMetricsFragment : Fragment() {

    private var _binding: FragmentEditorMetricsBinding? = null
    private val binding: FragmentEditorMetricsBinding
        get() = checkNotNull(_binding)
    private val viewModel: ExperimentEditorViewModel by navGraphViewModels(
        R.id.experiment_editor_graph, factoryProducer = {
            val application = requireActivity().application as LifeLabApplication

            ExperimentEditorViewModel.provideFactory(
                application.experimentRepository
            )

        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditorMetricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentMetricName = viewModel.draft.value
            .metrics
            .single()
            .name

        binding.metricNameInput.setText(currentMetricName)
        binding.metricNameInput.doAfterTextChanged { editable ->
            binding.metricNameInputLayout.error = null
            viewModel.updateMetricName(
                editable?.toString().orEmpty(),
            )
        }

        binding.metricsEditorToolbar.editorBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.metricsPreviousButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.metricsNextButton.setOnClickListener {
            val metricName = binding.metricNameInput.text
                ?.toString()
                ?.trim()
                .orEmpty()

            if (metricName.isBlank()) {
                binding.metricNameInputLayout.error =
                    "请输入指标名称"
                return@setOnClickListener
            }

            viewModel.updateMetricName(metricName)
            findNavController().navigate(
                R.id.action_experimentEditorMetricsFragment_to_experimentEditorPreviewFragment
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
