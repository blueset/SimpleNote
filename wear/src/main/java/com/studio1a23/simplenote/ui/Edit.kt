package com.studio1a23.simplenote.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.CompactChip
import com.studio1a23.simplenote.viewModel.MainViewModel

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Edit(viewModel: MainViewModel, onSave: () -> Unit, onChangeNoteType: () -> Unit) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Chip,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )
    val savedValue = remember { mutableStateOf(viewModel.noteContentFlow.value) }

    LaunchedEffect(viewModel.noteContentFlow) {
        viewModel.noteContentFlow.collect {
            Log.d("Edit", "Edit update savedValue to: $it")
            savedValue.value = it
        }
    }

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                val keyboardController = LocalSoftwareKeyboardController.current
                val focusRequester = remember { FocusRequester() }
                androidx.wear.compose.material.Chip(
                    label = {
                        BasicTextField(
                            value = savedValue.value,
                            onValueChange = {
                                savedValue.value = it
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = viewModel.noteTypeEnum.value,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                }
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = MaterialTheme.colors.onSurface,
                            ),
                            modifier = Modifier
                                .background(MaterialTheme.colors.surface)
                                .focusRequester(focusRequester),

                        )
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    },
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.secondaryChipColors(),
                    contentPadding = ChipDefaults.ContentPadding,
                )
            }
            item {
                CompactChip(
                    "Note type: ${viewModel.noteType.value}",
                    colors = ChipDefaults.secondaryChipColors(),
                    onClick = {
                        viewModel.saveNote(savedValue.value)
                        onChangeNoteType()
                    }
                )
            }
            item {
                Chip(
                    "Save",
                    onClick = {
                        viewModel.saveNote(savedValue.value)
                        onSave()
                    }
                )
            }
        }
    }
}

//@Composable
//@Preview(device = "spec:width=384px,height=884px,dpi=320,isRound=true")
//fun EditPreview() {
//    Edit()
//}
