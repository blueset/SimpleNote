package com.studio1a23.simplenote.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.AndroidViewModel
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.studio1a23.simplenote.complication.LongComplicationService
import com.studio1a23.simplenote.complication.ShortComplicationService
import com.studio1a23.simplenote.data.NoteHistory
import com.studio1a23.simplenote.tile.MainTileService
import kotlinx.coroutines.flow.MutableStateFlow


const val MAX_HISTORY_SIZE = 10

class MainViewModel(private val application: Application) : AndroidViewModel(application),
    DataClient.OnDataChangedListener {

    private val dataClient by lazy { Wearable.getDataClient(application) }

    var noteContentFlow = MutableStateFlow("")
    var noteType = mutableStateOf("")
    var noteTypeEnum = derivedStateOf { when (noteType.value) {
        "Number" -> KeyboardType.Number
        "Text" -> KeyboardType.Text
        "ASCII" -> KeyboardType.Ascii
        "Phone" -> KeyboardType.Phone
        "Email" -> KeyboardType.Email
        "URI" -> KeyboardType.Uri
        "Password" -> KeyboardType.Password
        "NumberPassword" -> KeyboardType.NumberPassword
        "Decimal" -> KeyboardType.Decimal
        else -> KeyboardType.Number
    } }

    var noteHistoriesFlow = MutableStateFlow(mutableListOf<NoteHistory>())

    fun init() {
        Log.d("dataMap", "On init.")
        dataClient.addListener(this)
    }

    @SuppressLint("VisibleForTests")
    fun loadData() {
        Log.d("dataMap", "Loading data.")
        val dataItems = dataClient.getDataItems(Uri.parse("wear://*/note"))
        dataItems.addOnSuccessListener { items ->
            items.forEach { item ->
                val dataMap = DataMapItem.fromDataItem(item).dataMap
                Log.d("dataMap", "Loading data map: $dataMap")
                if (dataMap.containsKey("note")) {
                    noteContentFlow.value = dataMap.getString("note") ?: ""
                }
                if (dataMap.containsKey("type")) {
                    noteType.value = dataMap.getString("type") ?: ""
                }
                if (dataMap.containsKey("history")) {
                    noteHistoriesFlow.value = NoteHistory.fromArrayListDataMap(dataMap.getDataMapArrayList("history"))
                }
            }
            items.release()
        }.addOnFailureListener { e ->
            Log.e("dataMap", "Failed to get data item: $e\n${e.message}\n${e.stackTraceToString()}")
        }
    }

    fun unload() {
        dataClient.removeListener(this)
    }

    @SuppressLint("VisibleForTests")
    fun saveNote(note: String) {
        Log.d("dataMap", "Saving note: $note")
        noteContentFlow.value = note
        if (note.isNotBlank() && noteHistoriesFlow.value.lastOrNull()?.note != note) {
            noteHistoriesFlow.value.add(NoteHistory(note, System.currentTimeMillis()))
        }
        saveToDataClient()
        MainTileService.forceTileUpdate(application)
    }

    fun saveNoteType(type: String) {
        Log.d("dataMap", "Saving note type: $type")
        noteType.value = type
        saveToDataClient()
    }

    @SuppressLint("VisibleForTests")
    fun saveToDataClient() {
        val request = PutDataMapRequest.create("/note").apply {
            dataMap.putString("type", noteType.value)
            dataMap.putString("note", noteContentFlow.value)
            dataMap.putDataMapArrayList("history", ArrayList(
                noteHistoriesFlow.value
                    .sortedBy { it.timestamp }
                    .takeLast(MAX_HISTORY_SIZE)
                    .map { it.toDataMap() }
            ))
        }.asPutDataRequest()
        dataClient.putDataItem(request).addOnSuccessListener {
            Log.d("dataMap", "Data item set: $it")
            val dataMap = DataMapItem.fromDataItem(it).dataMap
            Log.d("dataMap", "Data item set: $dataMap")
        }

        ComplicationDataSourceUpdateRequester.create(
            application, ComponentName(
                application, ShortComplicationService::class.java
            )
        ).requestUpdateAll()
        ComplicationDataSourceUpdateRequester.create(
            application, ComponentName(
                application, LongComplicationService::class.java
            )
        ).requestUpdateAll()
    }

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(events: DataEventBuffer) {
        Log.d("dataMap", "On data changed: $events")
        events.forEach { event ->
            if (event.dataItem.uri.path == "/note") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                dataMap.keySet().forEach {
                    if (it == "note") {
                        noteContentFlow.value = dataMap.getString(it) ?: ""
                    }
                    if (it == "type") {
                        noteType.value = dataMap.getString(it) ?: ""
                    }
                    if (it == "history") {
                        noteHistoriesFlow.value = NoteHistory.fromArrayListDataMap(dataMap.getDataMapArrayList(it))
                    }
                }
            }
        }
    }

}