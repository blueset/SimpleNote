package com.studio1a23.simplenote.utils

import android.content.Context
import android.net.Uri
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

suspend fun getNoteContent(context: Context): String? {
    val dataClient = Wearable.getDataClient(context)
    val dataItems = dataClient.getDataItems(Uri.parse("wear://*/note")).await()
    var noteContent: String? = null
    dataItems.forEach { item ->
        val dataMap = DataMapItem.fromDataItem(item).dataMap
        if (dataMap.containsKey("note")) {
            val asset = dataMap.getAsset("note")
            val fd = asset?.let { dataClient.getFdForAsset(it).await() }
            noteContent = fd?.inputStream?.bufferedReader()?.readText()
        }
    }
    dataItems.release()
    return noteContent
}