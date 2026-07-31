package com.lifelab.feature.experiment.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lifelab.R
import com.lifelab.databinding.FragmentExperimentsBinding

class ExperimentListFragment : Fragment() {
    private  var _binding : FragmentExperimentsBinding ?= null
    private  val binding : FragmentExperimentsBinding
        get() = checkNotNull(_binding)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExperimentsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val openEditor = View.OnClickListener {
            findNavController().navigate(
                R.id.action_experimentListFragment_to_experimentEditorGraph
            )
        }
        binding.addExperimentToolbarButton.setOnClickListener(openEditor)
        binding.newExperimentFab.setOnClickListener(openEditor)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
