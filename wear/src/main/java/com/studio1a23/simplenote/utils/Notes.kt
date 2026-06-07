package com.studio1a23.simplenote.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

suspend fun getNoteContent(context: Context): String? {
    // Fast path: serve the locally cached note. The data-layer asset read below
    // is slow and can be cancelled when a short-lived complication/tile service
    // is destroyed mid-request, so we avoid it whenever a cached value exists.
    NoteCache.get(context)?.let { return it }

    // No cache yet (e.g. first run): fall back to a guarded live read and seed
    // the cache so subsequent requests resolve instantly.
    val dataClient = Wearable.getDataClient(context)
    return try {
        val dataItems = dataClient.getDataItems(Uri.parse("wear://*/note")).await()
        var noteContent: String? = null
        try {
            dataItems.forEach { item ->
                val dataMap = DataMapItem.fromDataItem(item).dataMap
                if (dataMap.containsKey("note")) {
                    val asset = dataMap.getAsset("note") ?: return@forEach
                    val response = dataClient.getFdForAsset(asset).await()
                    response.inputStream.use { stream ->
                        noteContent = stream.bufferedReader().readText()
                    }
                }
            }
        } finally {
            // Always release the buffer, even if reading an asset failed,
            // to avoid leaking the underlying DataHolder.
            dataItems.release()
        }
        noteContent?.also { NoteCache.set(context, it) }
    } catch (e: CancellationException) {
        // Never swallow cancellation: let structured concurrency unwind correctly.
        throw e
    } catch (e: Exception) {
        Log.e("getNoteContent", "Failed to read note content", e)
        null
    }
}