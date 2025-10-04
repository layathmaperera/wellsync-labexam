package com.wellsync.app.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.wellsync.app.databinding.DialogSettingsBinding
import com.wellsync.app.services.HydrationReminderService
import com.wellsync.app.viewmodels.WellSyncViewModel

class SettingsDialogFragment : DialogFragment() {

    private var _binding: DialogSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WellSyncViewModel by activityViewModels()

    companion object { private const val TAG = "SettingsDialog" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.switchHydrationReminder.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateHydrationReminder(isChecked)
            binding.layoutReminderInterval.visibility = if (isChecked) View.VISIBLE else View.GONE

            val intent = Intent(context, HydrationReminderService::class.java)
            intent.action = if (isChecked) HydrationReminderService.ACTION_START_REMINDERS else HydrationReminderService.ACTION_STOP_REMINDERS
            if (isChecked) intent.putExtra(HydrationReminderService.EXTRA_INTERVAL, 5) // test interval

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) context?.startForegroundService(intent)
            else context?.startService(intent)
        }

        binding.seekBarInterval.apply {
            max = 105
            progress = 45
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val interval = progress + 15
                    viewModel.updateReminderInterval(interval)
                    binding.textIntervalValue.text = "$interval min"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun observeViewModel() {
        viewModel.hydrationReminder.observe(viewLifecycleOwner) { enabled ->
            binding.switchHydrationReminder.isChecked = enabled
            binding.layoutReminderInterval.visibility = if (enabled) View.VISIBLE else View.GONE
        }
        viewModel.reminderInterval.observe(viewLifecycleOwner) { interval ->
            binding.seekBarInterval.progress = interval - 15
            binding.textIntervalValue.text = "$interval min"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
