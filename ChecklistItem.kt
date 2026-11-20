package com.minimal.dailychecklist.model

import java.util.UUID

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isDone: Boolean = false
)

data class ChecklistState(
    val activeDate: String,
    val items: List<ChecklistItem>,
    val clearImmediatelyOnComplete: Boolean = false
) {
    val completedCount: Int get() = items.count { it.isDone }
    val totalCount: Int get() = items.size
    val allCompleted: Boolean get() = items.isNotEmpty() && completedCount == totalCount
}
