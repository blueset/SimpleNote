package com.studio1a23.simplenote.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.studio1a23.simplenote.R
import com.studio1a23.simplenote.presentation.MainActivity
import com.studio1a23.simplenote.utils.getNoteContent

class LongComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.LONG_TEXT) {
            return null
        }
        return createLongComplicationData(resources.getString(R.string.sample_note_long))
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val noteContent = getNoteContent(application).orEmpty().ifBlank { resources.getString(R.string.empty_note) }
        return createLongComplicationData(noteContent, createOpenNoteIntent())
    }

    private fun createLongComplicationData(
        text: String,
        tapAction: PendingIntent? = null
    ): LongTextComplicationData {
        val builder = LongTextComplicationData.Builder(
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