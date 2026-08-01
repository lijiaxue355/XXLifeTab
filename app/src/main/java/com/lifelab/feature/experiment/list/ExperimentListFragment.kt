package com.lifelab.feature.experiment.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.lifelab.LifeLabApplication
import com.lifelab.R
import com.lifelab.databinding.FragmentExperimentsBinding
import kotlinx.coroutines.launch

class ExperimentListFragment : Fragment() {
    private var _binding: FragmentExperimentsBinding? = null
    private val binding: FragmentExperimentsBinding
        get() = checkNotNull(_binding)
    private val viewModel: ExperimentListViewModel by viewModels {
        val application = requireActivity().application as LifeLabApplication
        ExperimentListViewModel.provideFactory(application.experimentRepository)
    }
    private val experimentAdapter = ExperimentListAdapter()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExperimentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val openEditor = View.OnClickListener {
            findNavController().navigate(
                R.id.action_experimentListFragment_to_experimentEditorGraph
            )
        }
        binding.experimentList.adapter = experimentAdapter
        binding.addExperimentToolbarButton.setOnClickListener(openEditor)
        binding.newExperimentFab.setOnClickListener(openEditor)
        observeExperiments()
    }

    private fun observeExperiments() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.experiments.collect { experimentWithMetrics ->
                    experimentAdapter.submitList(experimentWithMetrics)
                    binding.activeExperimentCount.text =
                        getString(
                            R.string.active_experiment_count_format,
                            experimentWithMetrics.size
                        )

                    binding.emptyExperimentsMessage.isVisible =
                        experimentWithMetrics.isEmpty()

                }
            }
        }
    }

    override fun onDestroyView() {
        binding.experimentList.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
