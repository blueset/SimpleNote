package com.studio1a23.simplenote.ui

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.studio1a23.simplenote.R
import com.studio1a23.simplenote.data.NoteHistory
import com.studio1a23.simplenote.viewModel.MainViewModel

@Composable
fun NoteHistory(
    viewModel: MainViewModel,
) {
    val history = viewModel.noteHistoriesFlow.collectAsState()
    HistoryList(history = history.value.sortedByDescending { it.timestamp })
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun HistoryList(history: List<NoteHistory>) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Card
        ),
        rotaryMode = ScalingLazyColumnState.RotaryMode.Snap,
    )
    val listState = rememberScalingLazyListState()

    ScreenScaffold(
        scrollState = columnState,
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = stringResource(R.string.edit_history),
                    style = MaterialTheme.typography.title3,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            items(history.count()) { idx ->
                NoteCard(history[idx])
            }
            if (history.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_history),
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: NoteHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {},
        enabled = false,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = note.note, style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.weight(1f)) // Push timestamp to the right
                Text(
                    text = DateUtils.getRelativeTimeSpanString(
                        note.timestamp,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString(),
                    style = MaterialTheme.typography.caption2,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, showSystemUi = true)
@Preview(device = WearDevices.LARGE_ROUND, showBackground = true, showSystemUi = true)
@Composable
fun PreviewNoteHistory() {
    HistoryList(listOf(
        NoteHistory("This is a long long long note. Lorem ipsum dolor sit amet, consectetur adipiscing elit.", System.currentTimeMillis()),
        NoteHistory("This is another note", System.currentTimeMillis() - 60 * 1000),
    ))
}
