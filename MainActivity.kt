package com.minimal.dailychecklist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minimal.dailychecklist.ui.ChecklistScreen
import com.minimal.dailychecklist.ui.ChecklistViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ChecklistViewModel by viewModels()
    private var openAddOnLaunch by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openAddOnLaunch = intent?.getBooleanExtra(EXTRA_OPEN_ADD, false) == true

        setContent {
            val state by viewModel.uiState.collectAsState()
            ChecklistScreen(
                state = state,
                openAddImmediately = openAddOnLaunch,
                onAddTask = viewModel::addTask,
                onToggleTask = viewModel::toggleTask,
                onDeleteTask = viewModel::deleteTask,
                onStartFresh = viewModel::startFreshChecklist,
                onToggleClearOnComplete = viewModel::setClearImmediatelyOnComplete
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDayIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra(EXTRA_OPEN_ADD, false)) {
            openAddOnLaunch = true
        }
    }

    companion object {
        const val EXTRA_OPEN_ADD = "extra_open_add"
    }
}
