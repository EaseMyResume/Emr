package com.minimal.dailychecklist

import android.app.Application
import com.minimal.dailychecklist.worker.MidnightResetWorker

class ChecklistApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MidnightResetWorker.schedule(this)
    }
}
