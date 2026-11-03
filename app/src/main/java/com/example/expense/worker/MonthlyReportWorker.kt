package com.example.expense.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expense.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MonthlyReportWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        NotificationHelper.showNotification(
            context = context,
            title = "Monthly Excel Report Ready 📄",
            message = "Your monthly expense report is ready! Tap to download the Excel spreadsheet.",
            notificationId = 2002
        )
        return Result.success()
    }
}
