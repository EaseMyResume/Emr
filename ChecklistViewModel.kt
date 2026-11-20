package com.minimal.dailychecklist.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.minimal.dailychecklist.data.ChecklistRepository
import com.minimal.dailychecklist.model.ChecklistState
import com.minimal.dailychecklist.widget.DailyChecklistWidget
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class ChecklistViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ChecklistRepository(application.applicationContext)

    val uiState: StateFlow<ChecklistState> = repo.stateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChecklistState(
            activeDate = LocalDate.now().toString(),
            items = emptyList()
        )
    )

    init {
        refreshDayIfNeeded()
    }

    fun refreshDayIfNeeded() {
        viewModelScope.launch {
            val rolledOver = repo.ensureCurrentDay()
            if (rolledOver) {
                DailyChecklistWidget.refreshAll(getApplication())
            }
        }
    }

    fun addTask(title: String) {
        viewModelScope.launch {
            repo.addItem(title)
            DailyChecklistWidget.refreshAll(getApplication())
        }
    }

    fun toggleTask(id: String) {
        viewModelScope.launch {
            repo.toggleItem(id)
            DailyChecklistWidget.refreshAll(getApplication())
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repo.removeItem(id)
            DailyChecklistWidget.refreshAll(getApplication())
        }
    }

    fun startFreshChecklist() {
        viewModelScope.launch {
            repo.clearChecklistForFreshStart()
            DailyChecklistWidget.refreshAll(getApplication())
        }
    }

    fun setClearImmediatelyOnComplete(enabled: Boolean) {
        viewModelScope.launch {
            repo.setClearImmediatelyOnComplete(enabled)
        }
    }
}
