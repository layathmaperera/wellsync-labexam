package com.wellsync.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.wellsync.app.adapters.JournalEntryAdapter
import com.wellsync.app.databinding.FragmentJournalBinding
import com.wellsync.app.models.Mood
import com.wellsync.app.viewmodels.WellSyncViewModel

class JournalFragment : Fragment() {
    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WellSyncViewModel by activityViewModels()
    private lateinit var journalAdapter: JournalEntryAdapter
    private var selectedMood = Mood.NEUTRAL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        journalAdapter = JournalEntryAdapter()
        binding.recyclerViewEntries.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = journalAdapter
        }
    }

    // Handles button clicks
    private fun setupClickListeners() {
        binding.buttonSaveEntry.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val notes = binding.editTextNotes.text.toString()

            if (title.isNotEmpty() && notes.isNotEmpty()) {
                viewModel.addJournalEntry(title, notes, selectedMood)
                clearForm()
            }
        }

        // 🔹 Share button -> open WhatsApp
        binding.buttonShareEntry.setOnClickListener {
            val entries = viewModel.moodEntries.value
            if (!entries.isNullOrEmpty()) {
                val latestEntry = entries.last()
                val shareText =
                    "📔 *${latestEntry.title}*\n\n${latestEntry.notes}\n\nMood: ${latestEntry.mood.emoji}"

                try {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage("com.whatsapp") // force WhatsApp
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "WhatsApp not installed!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "No journal entries to share.", Toast.LENGTH_SHORT).show()
            }
        }

        setupMoodButtons()
    }

    private fun setupMoodButtons() {
        binding.buttonMoodEcstatic.setOnClickListener { selectMood(Mood.ECSTATIC) }
        binding.buttonMoodHappy.setOnClickListener { selectMood(Mood.HAPPY) }
        binding.buttonMoodNeutral.setOnClickListener { selectMood(Mood.NEUTRAL) }
        binding.buttonMoodSad.setOnClickListener { selectMood(Mood.SAD) }
        binding.buttonMoodCrying.setOnClickListener { selectMood(Mood.CRYING) }

        // Default selection
        selectMood(Mood.NEUTRAL)
    }

    private fun selectMood(mood: Mood) {
        selectedMood = mood
        resetMoodButtons()
        when (mood) {
            Mood.ECSTATIC -> binding.buttonMoodEcstatic.alpha = 1.0f
            Mood.HAPPY -> binding.buttonMoodHappy.alpha = 1.0f
            Mood.NEUTRAL -> binding.buttonMoodNeutral.alpha = 1.0f
            Mood.SAD -> binding.buttonMoodSad.alpha = 1.0f
            Mood.CRYING -> binding.buttonMoodCrying.alpha = 1.0f
        }
    }

    private fun resetMoodButtons() {
        binding.buttonMoodEcstatic.alpha = 0.5f
        binding.buttonMoodHappy.alpha = 0.5f
        binding.buttonMoodNeutral.alpha = 0.5f
        binding.buttonMoodSad.alpha = 0.5f
        binding.buttonMoodCrying.alpha = 0.5f
    }

    private fun clearForm() {
        binding.editTextTitle.text?.clear()
        binding.editTextNotes.text?.clear()
        selectMood(Mood.NEUTRAL)
    }

    private fun observeViewModel() {
        viewModel.moodEntries.observe(viewLifecycleOwner) { entries ->
            val journalEntries = entries.filter { it.notes != null }
            journalAdapter.submitList(journalEntries)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
