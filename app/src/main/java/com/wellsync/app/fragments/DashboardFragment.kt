package com.wellsync.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.wellsync.app.adapters.HabitAdapter
import com.wellsync.app.databinding.FragmentDashboardBinding
import com.wellsync.app.fragments.AddHabitDialogFragment
import com.wellsync.app.models.Habit
import com.wellsync.app.models.MoodEntry
import com.wellsync.app.viewmodels.WellSyncViewModel
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WellSyncViewModel by activityViewModels()
    private lateinit var habitAdapter: HabitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
        setupMoodChart()
        setupMoodChartInteraction()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            onHabitToggle = { habit -> viewModel.toggleHabit(habit.id) },
            onHabitDelete = { habit -> viewModel.deleteHabit(habit.id) },
            fragmentManager = parentFragmentManager
        )
        binding.recyclerViewHabits.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = habitAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            habitAdapter.submitList(habits.toList())
            updateProgressWidget(habits)
        }
        viewModel.moodEntries.observe(viewLifecycleOwner) { entries ->
            updateMoodChart(entries)
        }
    }

    private fun updateProgressWidget(habits: List<Habit>) {
        val completedCount = habits.count { it.completed }
        val totalCount = habits.size
        val percentage = if (totalCount > 0) (completedCount * 100 / totalCount) else 0
        binding.progressBar.progress = percentage
        binding.textProgressPercentage.text = "$percentage%"
        binding.textProgressDescription.text = "$completedCount of $totalCount habits completed today"
    }

    private fun setupClickListeners() {
        binding.buttonAddHabit.setOnClickListener {
            val dialog = AddHabitDialogFragment()
            dialog.show(parentFragmentManager, "AddHabitDialog")
        }
    }

    private fun setupMoodChart() {
        binding.moodBarChart.apply {
            description.isEnabled = false
            setPinchZoom(false)
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            isHighlightFullBarEnabled = false
            setTouchEnabled(true)

            axisRight.isEnabled = false

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 5f
                granularity = 1f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#F5F5F5")
                textColor = Color.parseColor("#757575")
                textSize = 12f
                setDrawAxisLine(false)
                setDrawLabels(true)
                setLabelCount(6, true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (value.toInt()) {
                            1 -> "😢"
                            2 -> "😔"
                            3 -> "😐"
                            4 -> "😊"
                            5 -> "🤩"
                            else -> ""
                        }
                    }
                }
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.parseColor("#757575")
                textSize = 10f
                setDrawAxisLine(false)
                setDrawLabels(true)
                labelRotationAngle = -45f
                setLabelCount(7, true)
            }

            legend.isEnabled = false
            animateY(800)
            setBackgroundColor(Color.WHITE)
            setNoDataTextColor(Color.parseColor("#757575"))
        }
    }

    private fun setupMoodChartInteraction() {
        binding.moodBarChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    val moodValue = it.y
                    // Example: show a Toast
                    // Toast.makeText(requireContext(), "Avg mood: $moodValue", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected() {}
        })
    }

    private fun updateMoodChart(entries: List<MoodEntry>) {
        if (entries.isEmpty()) {
            binding.moodBarChart.clear()
            binding.moodBarChart.setNoDataText("Log your mood to see your trends here.")
            return
        }

        val last7Days = generateLast7DaysData(entries)
        val chartEntries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        last7Days.forEachIndexed { index, dayData ->
            chartEntries.add(BarEntry(index.toFloat(), dayData.second))
            labels.add(dayData.first)
        }

        val dataSet = BarDataSet(chartEntries, "Mood").apply {
            setColors(*chartEntries.map { getMoodColor(it.y) }.toIntArray())
            valueTextColor = Color.parseColor("#212121")
            valueTextSize = 11f
            setDrawValues(false)
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.6f
        }

        binding.moodBarChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            setFitBars(true)
            invalidate()
        }
    }

    private fun generateLast7DaysData(entries: List<MoodEntry>): List<Pair<String, Float>> {
        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
        val dayFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val result = mutableListOf<Pair<String, Float>>()

        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = dayFormatter.format(calendar.time)
            val displayDate = dateFormatter.format(calendar.time)
            val entriesForDay = entries.filter { entry ->
                dayFormatter.format(entry.date) == dateString
            }
            val avgMood = if (entriesForDay.isNotEmpty()) {
                entriesForDay.map { it.mood.value }.average().toFloat()
            } else 0f
            result.add(Pair(displayDate, avgMood))
        }
        return result
    }

    private fun getMoodColor(moodValue: Float): Int {
        return when {
            moodValue <= 1f -> Color.parseColor("#B0C4DE") // Crying
            moodValue <= 2f -> Color.parseColor("#87CEEB") // Sad
            moodValue <= 3f -> Color.parseColor("#D3D3D3") // Neutral
            moodValue <= 4f -> Color.parseColor("#90EE90") // Happy
            else -> Color.parseColor("#FFD700") // Ecstatic
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
