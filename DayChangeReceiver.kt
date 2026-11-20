package com.minimal.dailychecklist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.minimal.dailychecklist.data.ChecklistRepository
import com.minimal.dailychecklist.widget.DailyChecklistWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DayChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ChecklistRepository(context.applicationContext).ensureCurrentDay()
                DailyChecklistWidget.refreshAll(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
