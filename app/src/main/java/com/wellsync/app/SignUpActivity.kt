package com.wellsync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wellsync.app.databinding.ActivitySignUpBinding
import com.wellsync.app.utils.AuthManager

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authManager = AuthManager(this)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Sign up button click
        binding.buttonSignUp.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                performSignUp(name, email, password)
            }
        }

        // Login text click
        binding.textLogin.setOnClickListener {
            finish() // Go back to login
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            binding.editTextName.error = "Name is required"
            return false
        }

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

        if (confirmPassword.isEmpty()) {
            binding.editTextConfirmPassword.error = "Please confirm password"
            return false
        }

        if (password != confirmPassword) {
            binding.editTextConfirmPassword.error = "Passwords do not match"
            return false
        }

        return true
    }

    private fun performSignUp(name: String, email: String, password: String) {
        // Show loading
        binding.buttonSignUp.isEnabled = false
        binding.buttonSignUp.text = "Creating account..."

        // Simulate sign up (replace with actual backend call)
        if (authManager.signUp(name, email, password)) {
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
            binding.buttonSignUp.isEnabled = true
            binding.buttonSignUp.text = "Sign Up"
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}