package com.studio1a23.simplenote.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme

@Composable
fun SimpleNoteTheme(
    content: @Composable () -> Unit
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    val dynamicColorScheme = dynamicColorScheme(LocalContext.current)
    MaterialTheme(
        content = content,
        colorScheme = dynamicColorScheme ?: darkScheme,
    )
}