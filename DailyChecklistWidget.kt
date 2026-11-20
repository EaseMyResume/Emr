package com.minimal.dailychecklist.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.minimal.dailychecklist.MainActivity
import com.minimal.dailychecklist.R
import com.minimal.dailychecklist.data.ChecklistRepository
import com.minimal.dailychecklist.model.ChecklistItem
import com.minimal.dailychecklist.model.ChecklistState

class DailyChecklistWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = ChecklistRepository(context.applicationContext)
        val snapshot = repo.getSnapshot()

        provideContent {
            WidgetContent(context = context, state = snapshot)
        }
    }

    @Composable
    private fun WidgetContent(context: Context, state: ChecklistState) {
        val addIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_OPEN_ADD, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.widget_card_bg))
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity(openAppIntent)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily checklist",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFF5F5F7)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "${state.completedCount}/${state.totalCount}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF22C55E)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            val filledSegments = if (state.totalCount == 0) {
                0
            } else {
                ((state.completedCount.toFloat() / state.totalCount.toFloat()) * 10f)
                    .toInt()
                    .coerceIn(0, 10)
            }

            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(ImageProvider(R.drawable.widget_progress_track))
            ) {
                for (i in 0 until 10) {
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .height(4.dp)
                            .then(
                                if (i < filledSegments) {
                                    GlanceModifier.background(ImageProvider(R.drawable.widget_progress_fill))
                                } else {
                                    GlanceModifier
                                }
                            )
                    ) {}
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            if (state.items.isEmpty()) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .clickable(actionStartActivity(addIntent)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+ Add task for today",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF22C55E)),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                ) {
                    items(state.items, itemId = { it.id.hashCode().toLong() }) { item ->
                        WidgetTaskRow(item)
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.items.isEmpty()) {
                        "Fresh checklist ready."
                    } else {
                        "Tick each task when you complete it."
                    },
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF7C7C82)),
                        fontSize = 12.sp
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                Text(
                    text = "+ Add",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF22C55E)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.clickable(actionStartActivity(addIntent))
                )
            }
        }
    }

    @Composable
    private fun WidgetTaskRow(item: ChecklistItem) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .clickable(
                    actionRunCallback<ToggleTaskAction>(
                        actionParametersOf(TASK_ID_KEY to item.id)
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(
                    if (item.isDone) R.drawable.widget_check_checked
                    else R.drawable.widget_check_unchecked
                ),
                contentDescription = if (item.isDone) "Completed" else "Not completed",
                modifier = GlanceModifier.size(20.dp)
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            Text(
                text = item.title,
                style = TextStyle(
                    color = ColorProvider(
                        if (item.isDone) Color(0xFF7C7C82) else Color(0xFFF5F5F7)
                    ),
                    fontSize = 15.sp,
                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                ),
                maxLines = 1
            )
        }
    }

    companion object {
        val TASK_ID_KEY = ActionParameters.Key<String>("task_id")

        suspend fun refreshAll(context: Context) {
            DailyChecklistWidget().updateAll(context.applicationContext)
        }
    }
}

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[DailyChecklistWidget.TASK_ID_KEY] ?: return
        ChecklistRepository(context.applicationContext).toggleItem(taskId)
        DailyChecklistWidget.refreshAll(context.applicationContext)
    }
}

class DailyChecklistWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyChecklistWidget()
}
