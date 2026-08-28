package com.lifelab.feature.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lifelab.LifeLabApplication
import com.lifelab.BuildConfig
import com.lifelab.MainActivity
import com.lifelab.R
import com.lifelab.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(
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

        val tokenStore = (
            requireActivity().application as LifeLabApplication
        ).authTokenStore

        binding.settingsAccount.text =
            tokenStore.getAccount() ?: "LifeLab 用户"
        binding.settingsUserId.text =
            "用户 ID：${tokenStore.getUserId().orEmpty()}"

        bindTouchTargetInspection()

        bindPreviewItem(
            binding.accountSecurityItem,
            "账号与安全功能暂未开放",
        )
        bindPreviewItem(
            binding.syncSettingsItem,
            "同步设置功能暂未开放",
        )
        bindPreviewItem(
            binding.contactOfficialItem,
            "联系官方功能暂未开放",
        )
        bindPreviewItem(
            binding.privacyPolicyItem,
            "隐私政策功能暂未开放",
        )
        bindPreviewItem(
            binding.aboutItem,
            "LifeLab 1.0",
        )

        binding.logoutButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("退出登录")
                .setMessage("本地缓存会保留，下次登录后继续同步。")
                .setNegativeButton("取消", null)
                .setPositiveButton("退出") { _, _ ->
                    tokenStore.clearAccessToken()

                    val navController = findNavController()
                    navController.navigate(
                        R.id.auth_nav_graph,
                        null,
                        navOptions {
                            popUpTo(navController.graph.id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        },
                    )
                }
                .show()
        }
    }

    private fun bindTouchTargetInspection() {
        binding.touchTargetInspectionCard.isVisible = BuildConfig.DEBUG
        if (!BuildConfig.DEBUG) return

        val activity = requireActivity() as MainActivity
        binding.touchTargetInspectionSwitch.isChecked =
            activity.isTouchTargetInspectionEnabled()
        binding.touchTargetInspectionSwitch
            .setOnCheckedChangeListener { _, isChecked ->
                activity.setTouchTargetInspectionEnabled(isChecked)
                Toast.makeText(
                    requireContext(),
                    if (isChecked) {
                        R.string.touch_target_inspection_enabled
                    } else {
                        R.string.touch_target_inspection_disabled
                    },
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    private fun bindPreviewItem(
        view: View,
        message: String,
    ) {
        view.setOnClickListener {
            Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
