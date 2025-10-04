package com.wellsync.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.wellsync.app.databinding.DialogSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: DialogSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSettingsBinding.inflate(inflater, container, false)
        setupToolbar()
        setupUI()
        return binding.root
    }

    private fun setupToolbar() {
        // Set up the toolbar with back button
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Settings"
        }

        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle back button press
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupUI() {
        // Hydration Reminder Switch
        binding.switchHydrationReminder.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Save switch state
            binding.layoutReminderInterval.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // SeekBar interval
        binding.seekBarInterval.apply {
            max = 180 // 3 hours max
            progress = 60 // default 60 minutes
        }

        binding.seekBarInterval.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = if (progress < 15) 15 else progress // minimum 15 minutes
                binding.textIntervalValue.text = "$minutes min"
                // TODO: Save interval
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        // Theme preference
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioLight.id -> {
                    // TODO: Apply Light Theme
                }
                binding.radioDark.id -> {
                    // TODO: Apply Dark Theme
                }
                binding.radioSystemDefault.id -> {
                    // TODO: Apply System Default Theme
                }
            }
        }

        // Initially hide interval layout if switch is off
        if (!binding.switchHydrationReminder.isChecked) {
            binding.layoutReminderInterval.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Reset toolbar when leaving settings
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowHomeEnabled(false)
            title = "WellSync" // or your app name
        }
        _binding = null
    }
}