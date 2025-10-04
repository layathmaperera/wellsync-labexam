package com.wellsync.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wellsync.app.models.Habit
import com.wellsync.app.models.MoodEntry

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("wellsync_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HABITS = "habits"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HYDRATION_REMINDER = "hydration_reminder"
        private const val KEY_REMINDER_INTERVAL = "reminder_interval"
        private const val KEY_LAST_VISITED = "last_visited"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }

    // Replaces React's localStorage.setItem/getItem for habits
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        sharedPreferences.edit().putString(KEY_HABITS, json).apply()
    }

    fun loadHabits(): List<Habit>? {
        val json = sharedPreferences.getString(KEY_HABITS, null) ?: return null
        val type = object : TypeToken<List<Habit>>() {}.type
        return gson.fromJson(json, type)
    }

    // Replaces React's localStorage for mood entries
    fun saveMoodEntries(entries: List<MoodEntry>) {
        val json = gson.toJson(entries)
        sharedPreferences.edit().putString(KEY_MOOD_ENTRIES, json).apply()
    }

    fun loadMoodEntries(): List<MoodEntry>? {
        val json = sharedPreferences.getString(KEY_MOOD_ENTRIES, null) ?: return null
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    // Replaces React's localStorage for settings
    fun saveHydrationReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HYDRATION_REMINDER, enabled).apply()
    }

    fun isHydrationReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_HYDRATION_REMINDER, false)
    }

    fun saveReminderInterval(interval: Int) {
        sharedPreferences.edit().putInt(KEY_REMINDER_INTERVAL, interval).apply()
    }

    fun getReminderInterval(): Int {
        return sharedPreferences.getInt(KEY_REMINDER_INTERVAL, 60)
    }

    // Replaces React's date tracking for habit reset
    fun saveLastVisitedDate(date: String) {
        sharedPreferences.edit().putString(KEY_LAST_VISITED, date).apply()
    }

    fun getLastVisitedDate(): String? {
        return sharedPreferences.getString(KEY_LAST_VISITED, null)
    }

    // First launch tracking
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchComplete() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }
}