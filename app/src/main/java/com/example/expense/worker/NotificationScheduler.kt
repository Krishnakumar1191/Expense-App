package com.example.expense.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val weeklyWorkRequest = PeriodicWorkRequestBuilder<WeeklyReminderWorker>(
            7, TimeUnit.DAYS
        ).build()

        val monthlyWorkRequest = PeriodicWorkRequestBuilder<MonthlyReportWorker>(
            30, TimeUnit.DAYS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "WeeklyReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyWorkRequest
        )

        workManager.enqueueUniquePeriodicWork(
            "MonthlyReportWork",
            ExistingPeriodicWorkPolicy.KEEP,
            monthlyWorkRequest
        )
    }
}
