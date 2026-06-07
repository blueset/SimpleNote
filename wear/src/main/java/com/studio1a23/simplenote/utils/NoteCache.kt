package com.studio1a23.simplenote.utils

import android.content.Context

private const val PREFS_NAME = "note_cache"
private const val KEY_NOTE = "note"

/**
 * Small, synchronous local cache for the current note text.
 *
 * Complication and tile services are short-lived and can be destroyed before a
 * data-layer asset read completes (which surfaces as a JobCancellationException).
 * Reading the note from here is instant and never blocks on the data layer.
 */
object NoteCache {
    fun get(context: Context): String? =
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NOTE, null)

    fun set(context: Context, note: String) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NOTE, note)
            .apply()
    }
}
