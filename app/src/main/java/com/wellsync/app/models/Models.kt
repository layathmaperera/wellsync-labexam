package com.wellsync.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Habit(
    val id: String,
    val name: String,
    val icon: String,
    val completed: Boolean
) : Parcelable

enum class Mood(val emoji: String, val label: String, val value: Int) {
    ECSTATIC("🤩", "Ecstatic", 5),
    HAPPY("😊", "Happy", 4),
    NEUTRAL("😐", "Neutral", 3),
    SAD("😔", "Sad", 2),
    CRYING("😭", "Crying", 1)
}

@Parcelize
data class MoodEntry(
    val id: String,
    val mood: Mood,
    val date: Date,
    val title: String? = null,
    val notes: String? = null
) : Parcelable

@Parcelize
data class Settings(
    val hydrationReminder: Boolean = false,
    val reminderInterval: Int = 60, // minutes
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
) : Parcelable