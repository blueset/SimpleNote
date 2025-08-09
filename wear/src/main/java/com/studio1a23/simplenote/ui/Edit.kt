package com.studio1a23.simplenote.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.EdgeButton
import com.studio1a23.simplenote.R
import com.studio1a23.simplenote.viewModel.MainViewModel

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Edit(viewModel: MainViewModel, onSave: () -> Unit, onChangeNoteType: () -> Unit) {
    val columnState = rememberScalingLazyListState()
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

    ScreenScaffold(
        scrollState = columnState,
        contentPadding = PaddingValues(16.dp, 48.dp),
        edgeButtonSpacing = 0.dp,
        edgeButton = {
        EdgeButton(
            onClick = {
                viewModel.saveNote(savedValue.value.text)
                onSave()
            },
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = stringResource(R.string.button_save),
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
        }
    }) {
        contentPadding ->
        ScalingLazyColumn(
            state = columnState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            autoCentering = null,
        ) {
            item {
                val keyboardController = LocalSoftwareKeyboardController.current
                val focusRequester = remember { FocusRequester() }
                Button (
                    onClick = {
                        focusRequester.requestFocus()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    contentPadding = ButtonDefaults.ContentPadding,
                ) {
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
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .focusRequester(focusRequester),
                    )
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                        savedValue.value = savedValue.value.copy(selection = TextRange(savedValue.value.text.length))
                    }
                }
            }
            item {
                CompactButton(
                    colors = ButtonDefaults.outlinedButtonColors(),
                    border = ButtonDefaults.outlinedButtonBorder(true),
                    onClick = {
                        viewModel.saveNote(savedValue.value.text)
                        onChangeNoteType()
                    }
                ) {
                    Text("Note type: ${stringResource(numberFormats.getOrDefault(viewModel.noteType.value, R.string.note_type_text))}")
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
