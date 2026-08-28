package com.lifelab.feature.experiment.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.lifelab.LifeLabApplication
import com.lifelab.R
import com.lifelab.databinding.FragmentEditorPreviewBinding
import kotlinx.coroutines.launch

class ExperimentEditorPreviewFragment : Fragment() {

    private var _binding: FragmentEditorPreviewBinding? = null
    private val binding: FragmentEditorPreviewBinding
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
        _binding = FragmentEditorPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.previewSaveDraftButton.isVisible = false

        val draft = viewModel.draft.value

        binding.previewExperimentName.text = draft.name
        binding.previewHypothesis.text = if (
            draft.hypothesis.isBlank()
        ) {
            "未填写实验假设"
        } else {
            "假设：${draft.hypothesis}"
        }
        binding.previewMetricName.text =
            draft.metrics.single().name

        binding.durationBadge.text = getString(
            R.string.duration_days_format,
            draft.durationDays
        )

        binding.previewEditorToolbar.editorBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
//        binding.previewSaveDraftButton.setOnClickListener {
//            findNavController().popBackStack(R.id.experimentListFragment, false)
//        }
        binding.previewStartButton.setOnClickListener {
            viewModel.startExperiment()
        }
        observeSaveState()
    }

    private fun observeSaveState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.saveState.collect { saveState ->
                    when(saveState){
                        ExperimentSaveState.Idle -> {
                            binding.previewStartButton.isEnabled = true
                            binding.previewStartButton.setText(
                                R.string.start_experiment_now
                            )
                        }

                        ExperimentSaveState.Saving -> {
                            binding.previewStartButton.isEnabled = false
                            binding.previewStartButton.setText(
                                R.string.saving_experiment
                            )
                        }
                        is ExperimentSaveState.Success -> {
                            findNavController().popBackStack(R.id.experimentListFragment,false)
                        }
                        is ExperimentSaveState.Error -> {
                            binding.previewStartButton.isEnabled = true

                            Toast.makeText(
                                requireContext(),
                                getString(
                                    R.string.save_experiment_failed,
                                    saveState.message
                                ),
                                Toast.LENGTH_SHORT
                            ).show()

                            viewModel.consumeSaveError()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
