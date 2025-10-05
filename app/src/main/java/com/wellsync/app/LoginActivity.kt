package com.wellsync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wellsync.app.databinding.ActivityLoginBinding
import com.wellsync.app.utils.AuthManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize auth manager
        authManager = AuthManager(this)

        // Check if already logged in
        if (authManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Login button click
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        // Sign up text click
        binding.textSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Forgot password click
        binding.textForgotPassword.setOnClickListener {
            Toast.makeText(this, "Password reset feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.editTextEmail.error = "Email is required"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "Invalid email format"
            return false
        }

        if (password.isEmpty()) {
            binding.editTextPassword.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            binding.editTextPassword.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun performLogin(email: String, password: String) {
        // Show loading
        binding.buttonLogin.isEnabled = false
        binding.buttonLogin.text = "Logging in..."

        // Simulate login (replace with actual backend call)
        if (authManager.login(email, password)) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            binding.buttonLogin.isEnabled = true
            binding.buttonLogin.text = "Login"
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}