package com.lifelab.feature.experiment.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.lifelab.LifeLabApplication
import com.lifelab.R
import com.lifelab.databinding.FragmentEditorScheduleBinding

class ExperimentEditorScheduleFragment : Fragment() {

    private var _binding: FragmentEditorScheduleBinding? = null
    private val binding: FragmentEditorScheduleBinding
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
        _binding = FragmentEditorScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderSchedule()

        binding.decreaseDaysButton.setOnClickListener {
            val currentDuration = viewModel.draft.value.durationDays

            viewModel.updateSchedule(currentDuration - 1)

            renderSchedule()
        }

        binding.increaseDaysButton.setOnClickListener {
            val currentDuration = viewModel.draft.value.durationDays

            viewModel.updateSchedule(
                durationDays = currentDuration + 1
            )

            renderSchedule()
        }


        binding.scheduleEditorToolbar.editorBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.schedulePreviousButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.scheduleNextButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_experimentEditorScheduleFragment_to_experimentEditorMetricsFragment
            )
        }
    }
    private fun renderSchedule() {
        val draft = viewModel.draft.value

        binding.totalDurationValue.text = getString(
            R.string.duration_days_format,
            draft.durationDays
        )

        binding.baselineDays.text = getString(
            R.string.baseline_days_format,
            draft.baselineDays
        )

        binding.interventionDays.text = getString(
            R.string.intervention_days_format,
            draft.interventionDays
        )

        binding.decreaseDaysButton.isEnabled = draft.durationDays > 2
        binding.increaseDaysButton.isEnabled = draft.durationDays < 60
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
