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
import com.lifelab.databinding.FragmentEditorPreviewBinding

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

        binding.previewEditorToolbar.editorBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.previewSaveDraftButton.setOnClickListener {
            findNavController().popBackStack(R.id.experimentListFragment, false)
        }
        binding.previewStartButton.setOnClickListener {
            findNavController().popBackStack(R.id.experimentListFragment, false)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
