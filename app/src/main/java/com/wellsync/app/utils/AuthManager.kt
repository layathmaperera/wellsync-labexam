package com.wellsync.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class User(
    val name: String,
    val email: String,
    val password: String // In real app, this should be hashed!
)

class AuthManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_USERS = "users"
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Get current logged in user
    fun getCurrentUser(): User? {
        val json = sharedPreferences.getString(KEY_CURRENT_USER, null) ?: return null
        return gson.fromJson(json, User::class.java)
    }

    // Sign up new user
    fun signUp(name: String, email: String, password: String): Boolean {
        val users = getAllUsers().toMutableList()

        // Check if email already exists
        if (users.any { it.email == email }) {
            return false
        }

        // Create new user
        val newUser = User(name, email, password)
        users.add(newUser)

        // Save users
        saveUsers(users)

        // Auto login after signup
        saveCurrentUser(newUser)
        setLoggedIn(true)

        return true
    }

    // Login existing user
    fun login(email: String, password: String): Boolean {
        val users = getAllUsers()

        // Find user with matching credentials
        val user = users.firstOrNull { it.email == email && it.password == password }

        return if (user != null) {
            saveCurrentUser(user)
            setLoggedIn(true)
            true
        } else {
            false
        }
    }

    // Logout current user
    fun logout() {
        sharedPreferences.edit()
            .remove(KEY_CURRENT_USER)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }

    // Private helper methods
    private fun getAllUsers(): List<User> {
        val json = sharedPreferences.getString(KEY_USERS, null) ?: return emptyList()
        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveUsers(users: List<User>) {
        val json = gson.toJson(users)
        sharedPreferences.edit().putString(KEY_USERS, json).apply()
    }

    private fun saveCurrentUser(user: User) {
        val json = gson.toJson(user)
        sharedPreferences.edit().putString(KEY_CURRENT_USER, json).apply()
    }

    private fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
}