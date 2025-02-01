package com.studio1a23.simplenote.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.CompactChip
import com.studio1a23.simplenote.R
import com.studio1a23.simplenote.viewModel.MainViewModel

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Edit(viewModel: MainViewModel, onSave: () -> Unit, onChangeNoteType: () -> Unit) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Chip,
            last = ScalingLazyColumnDefaults.ItemType.SingleButton
        )
    )
    val savedValue = remember {
        mutableStateOf(
            TextFieldValue(
                viewModel.noteContentFlow.value,
                selection = TextRange(viewModel.noteContentFlow.value.length)
            )
        )
    }

    LaunchedEffect(viewModel.noteContentFlow) {
        viewModel.noteContentFlow.collect {
            Log.d("Edit", "Edit update savedValue to: $it")
            savedValue.value = savedValue.value.copy(text = it, selection = TextRange(it.length))
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
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.surface)
                                .focusRequester(focusRequester),
                        )
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                            savedValue.value = savedValue.value.copy(selection = TextRange(savedValue.value.text.length))
                        }
                    },
                    onClick = {
                        focusRequester.requestFocus()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.secondaryChipColors(),
                    contentPadding = ChipDefaults.ContentPadding,
                )
            }
            item {
                CompactChip(
                    "Note type: ${stringResource(numberFormats.getOrDefault(viewModel.noteType.value, R.string.note_type_text))}",
                    colors = ChipDefaults.secondaryChipColors(),
                    onClick = {
                        viewModel.saveNote(savedValue.value.text)
                        onChangeNoteType()
                    }
                )
            }
            item {
                Button(
                    onClick = {
                        viewModel.saveNote(savedValue.value.text)
                        onSave()
                    },
                    enabled = true,
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(R.string.button_save),
                    )
                }
            }
        }
    }
}

@Composable
@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true, showBackground = true)
fun EditPreview() {
    Edit(MainViewModel(Application()), {}, {})
}
