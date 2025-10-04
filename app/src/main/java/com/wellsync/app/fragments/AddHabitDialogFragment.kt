// AddHabitDialogFragment.kt - Replaces React's Add Habit Dialog
package com.wellsync.app.fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.wellsync.app.R
import com.wellsync.app.viewmodels.WellSyncViewModel

class AddHabitDialogFragment : DialogFragment() {
    private val viewModel: WellSyncViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val editText = EditText(context).apply {
            hint = "e.g., Read for 15 minutes"
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Add a New Habit")
            .setMessage("What new habit would you like to track?")
            .setView(editText)
            .setPositiveButton("Add Habit") { _, _ ->
                val habitName = editText.text.toString().trim()
                if (habitName.isNotEmpty()) {
                    viewModel.addHabit(habitName)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}