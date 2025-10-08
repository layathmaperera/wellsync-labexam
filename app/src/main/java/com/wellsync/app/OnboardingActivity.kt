package com.wellsync.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.wellsync.app.adapters.OnboardingAdapter
import com.wellsync.app.databinding.ActivityOnboardingBinding
import com.wellsync.app.models.OnboardingItem

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOnboarding()
        setupClickListeners()
    }

    private fun setupOnboarding() {
        val onboardingItems = listOf(
            OnboardingItem(
                title = "Track Your Wellness",
                description = "Monitor your daily habits, mood, and overall well-being in one place.",
                imageResId = R.drawable.ic_onboarding_wellness
            ),
            OnboardingItem(
                title = "Build Healthy Habits",
                description = "Create and track daily habits to build a healthier lifestyle, one step at a time.",
                imageResId = R.drawable.ic_onboarding_habits
            ),
            OnboardingItem(
                title = "Journal Your Thoughts",
                description = "Record your thoughts and feelings. Reflect on your emotional journey.",
                imageResId = R.drawable.ic_onboarding_journal
            ),
            OnboardingItem(
                title = "Stay Hydrated",
                description = "Get gentle reminders to drink water throughout the day and maintain optimal hydration.",
                imageResId = R.drawable.ic_onboarding_hydration
            )
        )

        onboardingAdapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = onboardingAdapter

        // Setup dots indicator
        TabLayoutMediator(binding.dotsIndicator, binding.viewPager) { _, _ -> }.attach()

        // Handle page changes
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonsVisibility(position, onboardingItems.size)
            }
        })

        // Initial button visibility
        updateButtonsVisibility(0, onboardingItems.size)
    }

    private fun setupClickListeners() {
        // Next button - go to next page
        binding.buttonNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < (onboardingAdapter.itemCount - 1)) {
                binding.viewPager.currentItem = currentItem + 1
            }
        }

        // Get Started button - go to login
        binding.buttonGetStarted.setOnClickListener {
            navigateToLogin()
        }

        // Skip button - go directly to login
        binding.textSkip.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun updateButtonsVisibility(position: Int, totalPages: Int) {
        if (position == totalPages - 1) {
            // Last page - show "Get Started" button
            binding.buttonNext.visibility = View.GONE
            binding.buttonGetStarted.visibility = View.VISIBLE
            binding.textSkip.visibility = View.GONE
        } else {
            // Other pages - show "Next" and "Skip" buttons
            binding.buttonNext.visibility = View.VISIBLE
            binding.buttonGetStarted.visibility = View.GONE
            binding.textSkip.visibility = View.VISIBLE
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}