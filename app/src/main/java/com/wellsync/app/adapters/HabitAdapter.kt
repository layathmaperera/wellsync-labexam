// Updated HabitAdapter.kt with edit/delete functionality
package com.wellsync.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wellsync.app.databinding.ItemHabitBinding
import com.wellsync.app.fragments.EditHabitDialogFragment
import com.wellsync.app.models.Habit

class HabitAdapter(
    private val onHabitToggle: (Habit) -> Unit,
    private val onHabitDelete: (Habit) -> Unit,   // 👈 added delete callback
    private val fragmentManager: FragmentManager
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(
        private val binding: ItemHabitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            // Replaces React's JSX rendering
            binding.textHabitName.text = habit.name
            binding.checkboxHabit.isChecked = habit.completed

            // Replaces React's conditional styling
            if (habit.completed) {
                binding.textHabitName.alpha = 0.6f
                binding.textHabitName.paint.isStrikeThruText = true
            } else {
                binding.textHabitName.alpha = 1.0f
                binding.textHabitName.paint.isStrikeThruText = false
            }

            // Replaces React's onClick handlers
            binding.checkboxHabit.setOnClickListener {
                onHabitToggle(habit)
            }

            binding.buttonEditHabit.setOnClickListener {
                val dialog = EditHabitDialogFragment.newInstance(habit)
                dialog.show(fragmentManager, "EditHabitDialog")
            }

            // 👇 Added delete button handler
            binding.buttonDeleteHabit.setOnClickListener {
                onHabitDelete(habit)
            }
        }
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }
}
