package com.wellsync.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.widget.RemoteViews
import com.wellsync.app.MainActivity
import com.wellsync.app.R
import com.wellsync.app.utils.SharedPreferencesHelper
import android.util.Log

class WellSyncWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("WellSyncWidget", "onUpdate called for ${appWidgetIds.size} widgets")
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        Log.d("WellSyncWidget", "Widget enabled")
    }

    override fun onDisabled(context: Context) {
        Log.d("WellSyncWidget", "Widget disabled")
    }

    companion object {
        private fun createPieChartBitmap(
            context: Context,
            percentage: Int
        ): Bitmap {
            val size = 240
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val centerX = size / 2f
            val centerY = size / 2f
            val radius = size / 2f - 20f

            val rectF = RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            )

            // Background circle (incomplete habits)
            val bgPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                color = Color.parseColor("#E8ECF1")
            }
            canvas.drawCircle(centerX, centerY, radius, bgPaint)

            // Completed habits arc
            if (percentage > 0) {
                val completedPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    color = Color.parseColor("#FFFFFF")
                }

                val sweepAngle = (percentage * 360f) / 100f
                canvas.drawArc(rectF, -90f, sweepAngle, true, completedPaint)
            }

            // Inner circle to create donut effect
            val innerPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                color = Color.parseColor("#667EEA")
            }
            canvas.drawCircle(centerX, centerY, radius * 0.6f, innerPaint)

            return bitmap
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                Log.d("WellSyncWidget", "Updating widget $appWidgetId")

                val prefsHelper = SharedPreferencesHelper(context)
                val habits = prefsHelper.loadHabits() ?: emptyList()

                val completedCount = habits.count { it.completed }
                val totalCount = habits.size
                val percentage = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

                Log.d("WellSyncWidget", "Habits: $completedCount/$totalCount ($percentage%)")

                // Create an Intent to launch MainActivity
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Construct the RemoteViews object
                val views = RemoteViews(context.packageName, R.layout.widget_wellsync)

                // Create and set pie chart bitmap
                val pieChartBitmap = createPieChartBitmap(context, percentage)
                views.setImageViewBitmap(R.id.widget_pie_chart, pieChartBitmap)

                views.setTextViewText(R.id.widget_title, "WellSync")
                views.setTextViewText(R.id.widget_percentage, "$percentage%")
                views.setTextViewText(
                    R.id.widget_description,
                    "$completedCount of $totalCount habits completed"
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                // Tell the AppWidgetManager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d("WellSyncWidget", "Widget updated successfully")

            } catch (e: Exception) {
                Log.e("WellSyncWidget", "Error updating widget", e)
            }
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = android.content.ComponentName(
                    context,
                    WellSyncWidgetProvider::class.java
                )
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

                Log.d("WellSyncWidget", "Updating all widgets: ${allWidgetIds.size}")

                for (widgetId in allWidgetIds) {
                    updateAppWidget(context, appWidgetManager, widgetId)
                }
            } catch (e: Exception) {
                Log.e("WellSyncWidget", "Error updating all widgets", e)
            }
        }
    }
}