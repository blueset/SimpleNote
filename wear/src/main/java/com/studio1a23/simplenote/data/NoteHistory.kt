package com.studio1a23.simplenote.data

import com.google.android.gms.wearable.DataMap

data class NoteHistory(
    val note: String,
    val timestamp: Long
) {
    companion object {
        fun fromArrayListDataMap(data: ArrayList<DataMap>?): MutableList<NoteHistory> {
            return data?.map {
                NoteHistory(
                    it.getString("note") ?: "",
                    it.getLong("timestamp")
                )
            }?.toMutableList() ?: mutableListOf()
        }
    }

    fun toDataMap() : DataMap {
        val dataMap = DataMap()
        dataMap.putString("note", note)
        dataMap.putLong("timestamp", timestamp)
        return dataMap
    }
}