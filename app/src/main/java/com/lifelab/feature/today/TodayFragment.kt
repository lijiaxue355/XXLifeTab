package com.lifelab.feature.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.lifelab.R
import com.lifelab.databinding.FragmentTodayBinding

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding: FragmentTodayBinding
        get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindPreviewInteractions()
    }

    private fun bindPreviewInteractions() = with(binding) {
        startRecordingButton.setOnClickListener {
            showPreviewMessage(getString(R.string.preview_record_message))
        }
        editTodayRecord.setOnClickListener {
            showPreviewMessage(getString(R.string.preview_edit_message))
        }
        activeExperimentCard.setOnClickListener {
            showPreviewMessage(getString(R.string.preview_detail_message))
        }
        reportCard.setOnClickListener {
            showPreviewMessage(getString(R.string.preview_report_message))
        }
        viewAllExperiments.setOnClickListener {
            showPreviewMessage(getString(R.string.preview_all_message))
        }
    }

    private fun showPreviewMessage(message: String) {
        Snackbar.make(binding.todayRoot, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(
                ContextCompat.getColor(requireContext(), R.color.lifelab_text_primary),
            )
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
