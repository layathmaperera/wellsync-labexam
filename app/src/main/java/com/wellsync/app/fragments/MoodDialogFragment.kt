package com.wellsync.app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.wellsync.app.databinding.DialogMoodBinding
import com.wellsync.app.models.Mood
import com.wellsync.app.viewmodels.WellSyncViewModel

class MoodDialogFragment : DialogFragment() {

    companion object {
        private const val TAG = "MoodDialogFragment"
    }

    private var _binding: DialogMoodBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WellSyncViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "MoodDialogFragment onCreateView called")
        _binding = DialogMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "MoodDialogFragment onViewCreated called")

        // XML handles emoji display, we just need click listeners
        setupClickListeners()

        Log.d(TAG, "MoodDialogFragment setup complete")
    }

    private fun setupClickListeners() {
        Log.d(TAG, "Setting up click listeners...")

        binding.buttonEcstatic.setOnClickListener {
            Log.d(TAG, "Ecstatic clicked")
            selectMood(Mood.ECSTATIC)
        }

        binding.buttonHappy.setOnClickListener {
            Log.d(TAG, "Happy clicked")
            selectMood(Mood.HAPPY)
        }

        binding.buttonNeutral.setOnClickListener {
            Log.d(TAG, "Neutral clicked")
            selectMood(Mood.NEUTRAL)
        }

        binding.buttonSad.setOnClickListener {
            Log.d(TAG, "Sad clicked")
            selectMood(Mood.SAD)
        }

        binding.buttonCrying.setOnClickListener {
            Log.d(TAG, "Crying clicked")
            selectMood(Mood.CRYING)
        }

        Log.d(TAG, "All click listeners set")
    }

    private fun selectMood(mood: Mood) {
        Log.d(TAG, "Selected mood: $mood")
        try {
            viewModel.addMoodEntry(mood)
            dismiss()
        } catch (e: Exception) {
            Log.e(TAG, "Error selecting mood: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "MoodDialogFragment destroyed")
        _binding = null
    }
}