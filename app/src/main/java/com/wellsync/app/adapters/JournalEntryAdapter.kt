package com.wellsync.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wellsync.app.databinding.ItemJournalEntryBinding
import com.wellsync.app.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class JournalEntryAdapter : ListAdapter<MoodEntry, JournalEntryAdapter.JournalEntryViewHolder>(
    JournalEntryDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalEntryViewHolder {
        val binding = ItemJournalEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JournalEntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JournalEntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class JournalEntryViewHolder(
        private val binding: ItemJournalEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        fun bind(entry: MoodEntry) {
            // Replaces React's JSX rendering for journal entries
            binding.textEntryTitle.text = entry.title ?: ""
            binding.textEntryNotes.text = entry.notes ?: ""
            binding.textEntryDate.text = dateFormat.format(entry.date)
            binding.textMoodEmoji.text = entry.mood.emoji
        }
    }

    class JournalEntryDiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
        override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem == newItem
        }
    }
}