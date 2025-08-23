package com.studio1a23.simplenote.tile

import android.content.Context
import android.content.Intent
import androidx.core.app.TaskStackBuilder
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.StateBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.expression.AppDataKey
import androidx.wear.protolayout.expression.DynamicBuilders
import androidx.wear.protolayout.expression.DynamicDataBuilders
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.studio1a23.simplenote.R
import com.studio1a23.simplenote.presentation.MainActivity
import com.studio1a23.simplenote.utils.getNoteContent

private const val RESOURCES_VERSION = "0"

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val lastClickableId = requestParams.currentState.lastClickableId
        if (lastClickableId == "editNote") {
            TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(
                    Intent(applicationContext, MainActivity::class.java).putExtra(
                        "destination",
                        "edit"
                    )
                )
                .startActivities()
        }

        val noteContent = getNoteContent(application) ?: resources.getString(R.string.empty_note)

        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder()
                    .setRoot(tileLayout(this, requestParams.deviceConfiguration, noteContent))
                    .build()
            ).build()
        ).build()

        val state = StateBuilders.State.Builder()
            .addKeyToValueMapping(
                KEY_NOTE,
                DynamicDataBuilders.DynamicDataValue.fromString(noteContent)
            )
            .build()

        return TileBuilders.Tile.Builder().setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline).setState(state).build()
    }

    companion object {
        val KEY_NOTE = AppDataKey<DynamicBuilders.DynamicString>("note")

        fun forceTileUpdate(applicationContext: Context) {
            getUpdater(applicationContext).requestUpdate(MainTileService::class.java)
        }
    }
}

private fun tileLayout(
    context: Context,
    deviceParameters: DeviceParametersBuilders.DeviceParameters,
    noteContent: String = ""
): LayoutElementBuilders.LayoutElement {
    return materialScope(
        context = context,
        deviceConfiguration = deviceParameters,
        allowDynamicTheme = true,
        defaultColorScheme = darkScheme
    ) {
        val resources = context.resources

        primaryLayout(
            titleSlot = {
                text(resources.getString(R.string.app_name).layoutString)
            },
            mainSlot = {
                text(
                    noteContent.ifEmpty { resources.getString(R.string.empty_note) }.layoutString,
                    maxLines = 8,
                    typography =
                    if (noteContent.isBlank())
                        Typography.TITLE_LARGE
                    else
                        when (deviceParameters.screenWidthDp) {
                            in 0..224 ->
                                when (noteContent.length) {
                                    in 0..2   -> Typography.NUMERAL_EXTRA_LARGE
                                    in 3..3   -> Typography.NUMERAL_LARGE
                                    in 4..4   -> Typography.DISPLAY_LARGE
                                    in 5..10  -> Typography.DISPLAY_MEDIUM
                                    in 11..12 -> Typography.DISPLAY_SMALL
                                    in 13..24 -> Typography.LABEL_LARGE
                                    in 25..27 -> Typography.TITLE_LARGE
                                    in 28..36 -> Typography.BODY_LARGE
                                    in 37..40 -> Typography.LABEL_MEDIUM
                                    in 41..44 -> Typography.BODY_MEDIUM
                                    in 45..48 -> Typography.LABEL_SMALL
                                    in 49..65 -> Typography.BODY_SMALL
                                    else ->            Typography.BODY_EXTRA_SMALL
                                }

                            else ->
                                when (noteContent.length) {
                                    in 0..3    -> Typography.NUMERAL_EXTRA_LARGE
                                    in 4..6    -> Typography.NUMERAL_LARGE
                                    in 7..8    -> Typography.DISPLAY_LARGE
                                    in 9..18   -> Typography.DISPLAY_MEDIUM
                                    in 19..21  -> Typography.DISPLAY_SMALL
                                    in 22..36  -> Typography.LABEL_LARGE
                                    in 37..50  -> Typography.TITLE_LARGE
                                    in 51..55  -> Typography.BODY_LARGE
                                    in 56..72  -> Typography.LABEL_MEDIUM
                                    in 73..78  -> Typography.BODY_MEDIUM
                                    in 79..84  -> Typography.LABEL_SMALL
                                    in 85..105 -> Typography.BODY_SMALL
                                    else ->             Typography.BODY_EXTRA_SMALL
                                }
                        },
                    italic = noteContent.isBlank(),
                    color = if (noteContent.isBlank()) colorScheme.primaryDim else colorScheme.primary,
                )
            },
            bottomSlot = {
                textEdgeButton(
                    labelContent = { text(resources.getString(R.string.button_edit).layoutString) },
                    onClick = clickable(id = "editNote")
                )
            }
        )
    }
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(
    onTileRequest = { request ->
        TilePreviewHelper.singleTimelineEntryTileBuilder(
            tileLayout(
                context,
                request.deviceConfiguration,
                "Hello, World!"
            ),
        ).build()
    }
)