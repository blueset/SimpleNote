package com.studio1a23.simplenote.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.studio1a23.simplenote.R
import com.studio1a23.simplenote.presentation.MainActivity
import com.studio1a23.simplenote.utils.getNoteContent

const val MAX_TEXT_LENGTH = 7

class ShortComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) {
            return null
        }
        return createShortComplicationData(resources.getString(R.string.sample_note_short))
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val noteContent = getNoteContent(application).orEmpty().ifBlank { resources.getString(R.string.empty_note_short) }
        val shortNoteContent = if (noteContent.length <= MAX_TEXT_LENGTH) {
            noteContent
        } else {
            noteContent.take(MAX_TEXT_LENGTH - 1) + "…"
        }
        return createShortComplicationData(shortNoteContent, createOpenNoteIntent())
    }

    private fun createShortComplicationData(text: String, tapAction: PendingIntent? = null): ShortTextComplicationData {
        val builder = ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(text).build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(
                        application,
                        R.drawable.ic_note
                    )
                ).build()
            )

        if (tapAction != null) {
            builder.setTapAction(tapAction)
        }
        return builder.build()
    }

    private fun createOpenNoteIntent() = PendingIntent.getActivity(
        application,
        1,
        Intent(applicationContext, MainActivity::class.java).putExtra(
            "destination",
            "edit"
        ),
        PendingIntent.FLAG_IMMUTABLE
    )
}