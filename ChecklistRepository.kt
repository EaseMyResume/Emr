package com.minimal.dailychecklist.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.minimal.dailychecklist.model.ChecklistItem
import com.minimal.dailychecklist.model.ChecklistState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

private val Context.checklistDataStore by preferencesDataStore(name = "daily_checklist_prefs")

class ChecklistRepository(private val context: Context) {

    private object Keys {
        val ACTIVE_DATE = stringPreferencesKey("active_date")
        val ITEMS_JSON = stringPreferencesKey("items_json")
        val CLEAR_ON_COMPLETE = booleanPreferencesKey("clear_immediately_on_complete")
    }

    val stateFlow: Flow<ChecklistState> = context.checklistDataStore.data.map { prefs ->
        val today = LocalDate.now().toString()
        val savedDate = prefs[Keys.ACTIVE_DATE]
        val clearOnComplete = prefs[Keys.CLEAR_ON_COMPLETE] ?: false

        if (savedDate != null && savedDate != today) {
            ChecklistState(
                activeDate = today,
                items = emptyList(),
                clearImmediatelyOnComplete = clearOnComplete
            )
        } else {
            val raw = prefs[Keys.ITEMS_JSON]
            val items = if (raw == null) {
                defaultStarterItems()
            } else {
                decodeItems(raw)
            }
            ChecklistState(
                activeDate = today,
                items = items,
                clearImmediatelyOnComplete = clearOnComplete
            )
        }
    }

    suspend fun ensureCurrentDay(): Boolean {
        val today = LocalDate.now().toString()
        var rolledOver = false
        context.checklistDataStore.edit { prefs ->
            val savedDate = prefs[Keys.ACTIVE_DATE]
            if (savedDate == null) {
                prefs[Keys.ACTIVE_DATE] = today
                if (prefs[Keys.ITEMS_JSON] == null) {
                    prefs[Keys.ITEMS_JSON] = encodeItems(defaultStarterItems())
                }
            } else if (savedDate != today) {
                prefs[Keys.ACTIVE_DATE] = today
                prefs[Keys.ITEMS_JSON] = "[]"
                rolledOver = true
            }
        }
        return rolledOver
    }

    suspend fun addItem(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        val today = LocalDate.now().toString()
        context.checklistDataStore.edit { prefs ->
            val current = readEffectiveItems(prefs, today)
            val updated = current + ChecklistItem(title = trimmed, isDone = false)
            prefs[Keys.ACTIVE_DATE] = today
            prefs[Keys.ITEMS_JSON] = encodeItems(updated)
        }
    }

    suspend fun toggleItem(id: String) {
        val today = LocalDate.now().toString()
        context.checklistDataStore.edit { prefs ->
            val current = readEffectiveItems(prefs, today)
            val clearImmediately = prefs[Keys.CLEAR_ON_COMPLETE] ?: false
            val updated = current.map { item ->
                if (item.id == id) item.copy(isDone = !item.isDone) else item
            }
            prefs[Keys.ACTIVE_DATE] = today
            if (clearImmediately && updated.isNotEmpty() && updated.all { it.isDone }) {
                prefs[Keys.ITEMS_JSON] = "[]"
            } else {
                prefs[Keys.ITEMS_JSON] = encodeItems(updated)
            }
        }
    }

    suspend fun removeItem(id: String) {
        val today = LocalDate.now().toString()
        context.checklistDataStore.edit { prefs ->
            val current = readEffectiveItems(prefs, today)
            val updated = current.filterNot { it.id == id }
            prefs[Keys.ACTIVE_DATE] = today
            prefs[Keys.ITEMS_JSON] = encodeItems(updated)
        }
    }

    suspend fun clearChecklistForFreshStart() {
        val today = LocalDate.now().toString()
        context.checklistDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_DATE] = today
            prefs[Keys.ITEMS_JSON] = "[]"
        }
    }

    suspend fun setClearImmediatelyOnComplete(enabled: Boolean) {
        context.checklistDataStore.edit { prefs ->
            prefs[Keys.CLEAR_ON_COMPLETE] = enabled
        }
    }

    suspend fun getSnapshot(): ChecklistState {
        ensureCurrentDay()
        return stateFlow.first()
    }

    private fun readEffectiveItems(
        prefs: androidx.datastore.preferences.core.Preferences,
        today: String
    ): List<ChecklistItem> {
        val savedDate = prefs[Keys.ACTIVE_DATE]
        if (savedDate != null && savedDate != today) {
            return emptyList()
        }
        val raw = prefs[Keys.ITEMS_JSON] ?: return defaultStarterItems()
        return decodeItems(raw)
    }

    private fun encodeItems(items: List<ChecklistItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("isDone", item.isDone)
                }
            )
        }
        return array.toString()
    }

    private fun decodeItems(raw: String): List<ChecklistItem> {
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        ChecklistItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            isDone = obj.optBoolean("isDone", false)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun defaultStarterItems(): List<ChecklistItem> = listOf(
        ChecklistItem(title = "Wake up on time"),
        ChecklistItem(title = "Avoid unnecessary phone scrolling"),
        ChecklistItem(title = "Walk or exercise for 20 minutes"),
        ChecklistItem(title = "Learn something useful for 30 minutes")
    )
}
