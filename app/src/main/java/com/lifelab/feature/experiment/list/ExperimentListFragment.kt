package com.lifelab.feature.experiment.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        ExperimentListViewModel.provideFactory(
            experimentRepository = application.experimentRepository,
            recordRepository = application.recordRepository,
        )
    }
    private val experimentAdapter = ExperimentListAdapter(
        onDeleteClick = ::showDeleteDialog,
    )
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
            viewModel.selectTab(ExperimentTab.ACTIVE)
            findNavController().navigate(
                R.id.action_experimentListFragment_to_experimentEditorGraph
            )
        }
        binding.experimentList.adapter = experimentAdapter
        binding.addExperimentToolbarButton.setOnClickListener(openEditor)
        binding.newExperimentFab.setOnClickListener(openEditor)
        binding.activeFilter.setOnClickListener {
            viewModel.selectTab(ExperimentTab.ACTIVE)
        }
        binding.completedFilter.setOnClickListener {
            viewModel.selectTab(ExperimentTab.COMPLETED)
        }
        observeExperiments()
    }

    private fun observeExperiments() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.uiState.collect { state ->
                        experimentAdapter.submitList(state.experiments)
                        renderTabs(state.selectedTab)

                        binding.activeExperimentCount.text = when (
                            state.selectedTab
                        ) {
                            ExperimentTab.ACTIVE ->
                                "${state.activeCount} 个进行中的实验"

                            ExperimentTab.COMPLETED ->
                                "${state.completedCount} 个已完成的实验"
                        }

                        binding.emptyExperimentsMessage.isVisible =
                            state.experiments.isEmpty()
                        binding.emptyExperimentsMessage.text = when (
                            state.selectedTab
                        ) {
                            ExperimentTab.ACTIVE ->
                                "暂无进行中的实验"

                            ExperimentTab.COMPLETED ->
                                "暂无已完成的实验"
                        }
                    }
                }

                launch {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is ExperimentListEffect.ShowMessage -> {
                                Toast.makeText(
                                    requireContext(),
                                    effect.message,
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(item: ExperimentListItem) {
        val experiment = item.experiment.experiment

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("删除实验")
            .setMessage(
                "确定删除“${experiment.name}”吗？相关打卡记录也会删除。",
            )
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteExperiment(experiment.id)
            }
            .show()
    }

    private fun renderTabs(selectedTab: ExperimentTab) = with(binding) {
        val primaryColor = ContextCompat.getColor(
            requireContext(),
            R.color.lifelab_primary_dark,
        )
        val secondaryColor = ContextCompat.getColor(
            requireContext(),
            R.color.lifelab_text_secondary,
        )

        activeFilter.setBackgroundResource(
            if (selectedTab == ExperimentTab.ACTIVE) {
                R.drawable.bg_primary_chip
            } else {
                android.R.color.transparent
            },
        )
        completedFilter.setBackgroundResource(
            if (selectedTab == ExperimentTab.COMPLETED) {
                R.drawable.bg_primary_chip
            } else {
                android.R.color.transparent
            },
        )
        activeFilter.setTextColor(
            if (selectedTab == ExperimentTab.ACTIVE) {
                primaryColor
            } else {
                secondaryColor
            },
        )
        completedFilter.setTextColor(
            if (selectedTab == ExperimentTab.COMPLETED) {
                primaryColor
            } else {
                secondaryColor
            },
        )
    }

    override fun onDestroyView() {
        binding.experimentList.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
