package com.minimal.dailychecklist.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimal.dailychecklist.model.ChecklistItem
import com.minimal.dailychecklist.model.ChecklistState

private val AppBg = Color(0xFF000000)
private val CardBg = Color(0xFF111113)
private val CardBorder = Color(0xFF232327)
private val GreenAccent = Color(0xFF22C55E)
private val TrackBg = Color(0xFF232327)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextMuted = Color(0xFF7C7C82)
private val CircleOutline = Color(0xFF6E6E73)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    state: ChecklistState,
    openAddImmediately: Boolean,
    onAddTask: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onStartFresh: () -> Unit,
    onToggleClearOnComplete: (Boolean) -> Unit
) {
    var isAdding by remember { mutableStateOf(openAddImmediately) }
    var newTaskText by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(openAddImmediately) {
        if (openAddImmediately) {
            isAdding = true
        }
    }

    LaunchedEffect(isAdding) {
        if (isAdding) {
            focusRequester.requestFocus()
        }
    }

    val progressTarget = if (state.totalCount == 0) {
        0f
    } else {
        state.completedCount.toFloat() / state.totalCount.toFloat()
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 260),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = CardBg,
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily checklist",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${state.completedCount}/${state.totalCount}",
                        color = GreenAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Checklist options",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TrackBg)
                ) {
                    if (animatedProgress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(RoundedCornerShape(4.dp))
                                .background(GreenAccent)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = state.items.isEmpty(),
                    transitionSpec = {
                        fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                    },
                    label = "checklist_body"
                ) { isEmpty ->
                    if (isEmpty) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Fresh checklist ready for today.",
                                color = TextMuted,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightInMax(380.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.items, key = { it.id }) { item ->
                                ChecklistRow(
                                    item = item,
                                    onToggle = { onToggleTask(item.id) },
                                    onDelete = { onDeleteTask(item.id) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isAdding) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1A1A1E))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = newTaskText,
                            onValueChange = { newTaskText = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 15.sp
                            ),
                            cursorBrush = SolidColor(GreenAccent),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newTaskText.isNotBlank()) {
                                        onAddTask(newTaskText)
                                        newTaskText = ""
                                        isAdding = false
                                    }
                                }
                            ),
                            decorationBox = { innerTextField ->
                                if (newTaskText.isEmpty()) {
                                    Text(
                                        text = "Add a task for today...",
                                        color = TextMuted,
                                        fontSize = 15.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Add",
                            color = if (newTaskText.isNotBlank()) GreenAccent else TextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = newTaskText.isNotBlank()) {
                                    onAddTask(newTaskText)
                                    newTaskText = ""
                                    isAdding = false
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        IconButton(
                            onClick = {
                                newTaskText = ""
                                isAdding = false
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Cancel",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isAdding = true }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add task",
                                tint = GreenAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add task",
                                color = GreenAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (state.allCompleted) {
                            Text(
                                text = "Start fresh",
                                color = TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onStartFresh() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (state.items.isEmpty()) {
                        "Tap Add task to build today's checklist."
                    } else {
                        "Tick each task when you complete it."
                    },
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CardBg,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Checklist behavior",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clear immediately when all done",
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Off: clears automatically at midnight. On: clears right after ticking the last task.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = state.clearImmediatelyOnComplete,
                        onCheckedChange = onToggleClearOnComplete,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = GreenAccent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        onStartFresh()
                        showSettings = false
                    }
                ) {
                    Text(
                        text = "Clear today's checklist now",
                        color = GreenAccent
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    item: ChecklistItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .then(
                    if (item.isDone) {
                        Modifier.background(Color.White)
                    } else {
                        Modifier.border(1.5.dp, CircleOutline, CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item.isDone) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Completed",
                    tint = CardBg,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = item.title,
            color = if (item.isDone) TextMuted else TextPrimary,
            fontSize = 16.sp,
            textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Remove task",
                tint = Color(0xFF48484D),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

private fun Modifier.heightInMax(max: androidx.compose.ui.unit.Dp): Modifier =
    this.then(androidx.compose.foundation.layout.heightIn(max = max))
