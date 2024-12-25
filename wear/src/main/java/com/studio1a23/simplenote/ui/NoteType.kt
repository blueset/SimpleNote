package com.studio1a23.simplenote.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.studio1a23.simplenote.R
import com.studio1a23.simplenote.viewModel.MainViewModel

val numberFormats = mapOf(
    "Number" to R.string.note_type_number,
    "Text" to R.string.note_type_text,
    "ASCII" to R.string.note_type_ascii,
    "Phone" to R.string.note_type_phone,
    "Email" to R.string.note_type_email,
    "URI" to R.string.note_type_uri,
    "Password" to R.string.note_type_password,
    "NumberPassword" to R.string.note_type_number_password,
    "Decimal" to R.string.note_type_decimal,
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

    val noteType = if (viewModel.noteType.value in numberFormats) {
        viewModel.noteType.value
    } else {
        "Text"
    }

    ScalingLazyColumn(
        columnState = columnState
    ) {
        item {
            ListHeader {
                Text(stringResource(R.string.note_type))
            }
        }
        for ((format, formatNameRes) in numberFormats) {
            val checked = noteType == format
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
                        Text(stringResource(formatNameRes))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}