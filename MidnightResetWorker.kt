package com.minimal.dailychecklist.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.minimal.dailychecklist.data.ChecklistRepository
import com.minimal.dailychecklist.widget.DailyChecklistWidget
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class MidnightResetWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repo = ChecklistRepository(applicationContext)
        repo.ensureCurrentDay()
        DailyChecklistWidget.refreshAll(applicationContext)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "daily_checklist_midnight_reset"

        fun schedule(context: Context) {
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay().plusMinutes(1)
            val initialDelayMinutes = Duration.between(now, nextMidnight).toMinutes().coerceAtLeast(1)

            val request = PeriodicWorkRequestBuilder<MidnightResetWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
