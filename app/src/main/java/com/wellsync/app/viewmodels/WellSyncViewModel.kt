package com.wellsync.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wellsync.app.R
import com.wellsync.app.models.Habit
import com.wellsync.app.models.MoodEntry
import com.wellsync.app.models.Mood
import com.wellsync.app.utils.SharedPreferencesHelper
import java.text.SimpleDateFormat
import java.util.*

class WellSyncViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsHelper = SharedPreferencesHelper(application)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Replaces React useState for habits
    private val _habits = MutableLiveData<MutableList<Habit>>().apply {
        value = (prefsHelper.loadHabits()?.toMutableList() ?: getDefaultHabits())
    }
    val habits: LiveData<MutableList<Habit>> = _habits

    // Replaces React useState for mood entries
    private val _moodEntries = MutableLiveData<MutableList<MoodEntry>>().apply {
        value = (prefsHelper.loadMoodEntries()?.toMutableList() ?: mutableListOf())
    }
    val moodEntries: LiveData<MutableList<MoodEntry>> = _moodEntries

    // Replaces React useState for settings
    private val _hydrationReminder = MutableLiveData<Boolean>().apply {
        value = prefsHelper.isHydrationReminderEnabled()
    }
    val hydrationReminder: LiveData<Boolean> = _hydrationReminder

    private val _reminderInterval = MutableLiveData<Int>().apply {
        value = prefsHelper.getReminderInterval()
    }
    val reminderInterval: LiveData<Int> = _reminderInterval

    init {
        // Replaces React's useEffect for daily habit reset
        checkAndResetHabits()
    }

    // Replaces React's date checking logic
    private fun checkAndResetHabits() {
        val today = dateFormat.format(Date())
        val lastVisited = prefsHelper.getLastVisitedDate()

        if (today != lastVisited) {
            val currentHabits = _habits.value ?: return
            val resetHabits = currentHabits.map { it.copy(completed = false) }.toMutableList()
            _habits.value = resetHabits
            saveHabits()
            prefsHelper.saveLastVisitedDate(today)
        }
    }

    // Replaces React's habit management functions with persistence
    fun toggleHabit(habitId: String) {
        val currentHabits = _habits.value ?: return
        val updatedHabits = currentHabits.map { habit ->
            if (habit.id == habitId) {
                habit.copy(completed = !habit.completed)
            } else {
                habit
            }
        }.toMutableList()
        _habits.value = updatedHabits
        saveHabits()
    }

    fun addHabit(name: String) {
        val currentHabits = _habits.value ?: mutableListOf()
        val newHabit = Habit(
            id = UUID.randomUUID().toString(),
            name = name,
            icon = "default",
            completed = false
        )
        currentHabits.add(newHabit)
        _habits.value = currentHabits
        saveHabits()
    }

    fun deleteHabit(habitId: String) {
        val currentHabits = _habits.value ?: return
        currentHabits.removeAll { it.id == habitId }
        _habits.value = currentHabits
        saveHabits()
    }

    fun updateHabit(habitId: String, newName: String) {
        val currentHabits = _habits.value ?: return
        val updatedHabits = currentHabits.map { habit ->
            if (habit.id == habitId) {
                habit.copy(name = newName)
            } else {
                habit
            }
        }.toMutableList()
        _habits.value = updatedHabits
        saveHabits()
    }

    // Replaces React's mood logging function with persistence
    fun addMoodEntry(mood: Mood) {
        val currentEntries = _moodEntries.value ?: mutableListOf()
        val newEntry = MoodEntry(
            id = UUID.randomUUID().toString(),
            mood = mood,
            date = Date(),
            title = null,
            notes = null
        )
        currentEntries.add(0, newEntry)
        _moodEntries.value = currentEntries
        saveMoodEntries()
    }

    fun addJournalEntry(title: String, notes: String, mood: Mood) {
        val currentEntries = _moodEntries.value ?: mutableListOf()
        val newEntry = MoodEntry(
            id = UUID.randomUUID().toString(),
            mood = mood,
            date = Date(),
            title = title,
            notes = notes
        )
        currentEntries.add(0, newEntry)
        _moodEntries.value = currentEntries
        saveMoodEntries()
    }

    // Replaces React's settings management with persistence
    fun updateHydrationReminder(enabled: Boolean) {
        _hydrationReminder.value = enabled
        prefsHelper.saveHydrationReminderEnabled(enabled)
    }

    fun updateReminderInterval(interval: Int) {
        _reminderInterval.value = interval
        prefsHelper.saveReminderInterval(interval)
    }

    // Helper methods for persistence
    private fun saveHabits() {
        _habits.value?.let { prefsHelper.saveHabits(it) }
    }

    private fun saveMoodEntries() {
        _moodEntries.value?.let { prefsHelper.saveMoodEntries(it) }
    }

    // Replaces React's habit completion percentage calculation
    fun getHabitCompletionPercentage(): Int {
        val currentHabits = _habits.value ?: return 0
        if (currentHabits.isEmpty()) return 0
        val completedCount = currentHabits.count { it.completed }
        return (completedCount * 100 / currentHabits.size)
    }

    // Replaces React's default habits
    private fun getDefaultHabits(): MutableList<Habit> {
        val context = getApplication<Application>()
        return mutableListOf(
            Habit("1", context.getString(R.string.default_habit_water), "water", false),
            Habit("2", context.getString(R.string.default_habit_meditation), "meditation", false),
            Habit("3", context.getString(R.string.default_habit_steps), "steps", false),
            Habit("4", context.getString(R.string.default_habit_sleep), "sleep", false)
        )
    }
}