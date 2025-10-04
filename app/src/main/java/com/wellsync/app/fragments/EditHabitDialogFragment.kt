// EditHabitDialogFragment.kt - Replaces React's Edit Habit functionality
package com.wellsync.app.fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.wellsync.app.models.Habit
import com.wellsync.app.viewmodels.WellSyncViewModel

class EditHabitDialogFragment : DialogFragment() {
    private val viewModel: WellSyncViewModel by activityViewModels()

    companion object {
        private const val ARG_HABIT_ID = "habit_id"
        private const val ARG_HABIT_NAME = "habit_name"

        fun newInstance(habit: Habit): EditHabitDialogFragment {
            return EditHabitDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_HABIT_ID, habit.id)
                    putString(ARG_HABIT_NAME, habit.name)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val habitId = arguments?.getString(ARG_HABIT_ID) ?: return super.onCreateDialog(savedInstanceState)
        val habitName = arguments?.getString(ARG_HABIT_NAME) ?: ""

        val editText = EditText(context).apply {
            setText(habitName)
            selectAll()
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(editText)
            .setPositiveButton("Save Changes") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.updateHabit(habitId, newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Delete") { _, _ ->
                viewModel.deleteHabit(habitId)
            }
            .create()
    }
}
