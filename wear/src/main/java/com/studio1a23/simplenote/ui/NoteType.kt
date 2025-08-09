package com.studio1a23.simplenote.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.google.android.horologist.annotations.ExperimentalHorologistApi
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
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    val noteType = if (viewModel.noteType.value in numberFormats) {
        viewModel.noteType.value
    } else {
        "Text"
    }

    AppScaffold {
        ScreenScaffold(columnState) { contentPadding ->
            TransformingLazyColumn(
                state = columnState,
                contentPadding = contentPadding,
            ) {
                item {
                    ListHeader {
                        Text(stringResource(R.string.note_type))
                    }
                }
                for ((format, formatNameRes) in numberFormats) {
                    val checked = noteType == format
                    item {
                        RadioButton(
                            selected = checked,
                            onSelect = { viewModel.saveNoteType(format) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                            label = {
                                Text(stringResource(formatNameRes))
                            }
                        )
                    }
                }
            }
        }
    }
}