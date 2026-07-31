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
import com.lifelab.databinding.FragmentEditorBasicBinding

class ExperimentEditorBasicFragment : Fragment() {

    private var _binding: FragmentEditorBasicBinding? = null
    private val binding: FragmentEditorBasicBinding
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
        _binding = FragmentEditorBasicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fragment 返回或重建时，把 ViewModel 中的数据恢复到输入框
        val currentDraft = viewModel.draft.value

        binding.experimentNameInput.setText(currentDraft.name)
        binding.hypothesisInput.setText(currentDraft.hypothesis)
        binding.descriptionInput.setText(currentDraft.description)

        // 返回列表会弹出整个编辑器导航图，草稿 ViewModel 随之销毁
        binding.basicEditorToolbar.editorBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.basicNextButton.setOnClickListener {
            val name = binding.experimentNameInput.text.toString()
            val hypothesis = binding.hypothesisInput.text.toString()
            val description = binding.descriptionInput.text.toString()

            if (name.isBlank()) {
                binding.experimentNameInput.error = "请输入实验名称"
                return@setOnClickListener
            }

            if (hypothesis.isBlank()) {
                binding.hypothesisInput.error = "请输入实验假设"
                return@setOnClickListener
            }

            viewModel.updateBasicInfo(
                name,
                hypothesis,
                description
            )

            findNavController().navigate(
                R.id.action_experimentEditorBasicFragment_to_experimentEditorScheduleFragment
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
