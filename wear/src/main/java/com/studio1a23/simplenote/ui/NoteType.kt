package com.studio1a23.simplenote.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.studio1a23.simplenote.viewModel.MainViewModel

val numberFormats = listOf(
    "Number",
    "Text",
    "ASCII",
    "Phone",
    "Email",
    "URI",
    "Password",
    "NumberPassword",
    "Decimal",
)

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun EditNoteType(
    viewModel: MainViewModel,
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Unspecified,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScalingLazyColumn(
        columnState = columnState
    ) {
        item {
            ListHeader {
                Text("Note type")
            }
        }
        for (format in numberFormats) {
            val checked = viewModel.noteType.value == format
            item {
                ToggleChip(
                    checked = checked,
                    toggleControl = {
                        RadioButton(
                            selected = checked,
                            modifier = Modifier.semantics {
                                this.contentDescription = if (checked) "On" else "Off"
                            }
                        )
                    },
                    onCheckedChange = { viewModel.saveNoteType(format) },
                    // Override the default toggle control color to show the user the current
                    // primary selected color.
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedToggleControlColor = MaterialTheme.colors.primary,
                    ),
                    label = {
                        Text(format)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}