package com.studio1a23.simplenote.ui

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
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
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    AppScaffold {
        ScreenScaffold(columnState, modifier = Modifier.padding(bottom = 24.dp)) { contentPadding ->
            TransformingLazyColumn(
                state = columnState,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    ListHeader { Text(stringResource(R.string.title_edit_history)) }
                }
                items(history.count()) { idx ->
                    NoteCard(
                        history[idx],
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec)
                    )
                }
                if (history.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_history),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: NoteHistory,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        transformation = transformation,
        onClick = {},
        enabled = false,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = CardDefaults.Height - CardDefaults.ContentPadding.calculateTopPadding() - CardDefaults.ContentPadding.calculateBottomPadding())
        ) {
            Text(text = note.note, style = MaterialTheme.typography.bodySmall)
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
                    style = MaterialTheme.typography.bodyExtraSmall,
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
    HistoryList(
        listOf(
            NoteHistory(
                "This is a long long long note. Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                System.currentTimeMillis()
            ),
            NoteHistory("This is another note", System.currentTimeMillis() - 60 * 1000),
        )
    )
}
