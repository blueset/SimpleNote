package com.studio1a23.simplenote.tile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.TaskStackBuilder
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.StateBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.expression.AppDataKey
import androidx.wear.protolayout.expression.DynamicBuilders
import androidx.wear.protolayout.expression.DynamicDataBuilders
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.studio1a23.simplenote.R
import com.studio1a23.simplenote.presentation.MainActivity
import com.studio1a23.simplenote.utils.getNoteContent
import kotlinx.coroutines.tasks.await

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

        val noteContent = getNoteContent(application) ?: resources.getString(R.string.no_note_found)

        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder().setRoot(tileLayout(this, requestParams.deviceConfiguration, noteContent)).build()
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

private fun tileLayout(context: Context, deviceParameters: DeviceParametersBuilders.DeviceParameters, noteContent: String = ""): LayoutElementBuilders.LayoutElement {
    val chipColors = ChipColors.primaryChipColors(Colors.DEFAULT)
    val resources = context.resources
    return PrimaryLayout.Builder(deviceParameters)
        .setResponsiveContentInsetEnabled(true)
        .setPrimaryLabelTextContent(
            Text.Builder(context, resources.getString(R.string.quick_note))
                .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                .setColor(argb(Colors.DEFAULT.primary))
                .build()
        )
        .setContent(
            Text.Builder(
                context,
//                TypeBuilders.StringProp.Builder(noteContent).setDynamicValue(
//                    DynamicBuilders.DynamicString.from(KEY_NOTE)
//                ).build(), TypeBuilders.StringLayoutConstraint.Builder("1234567890\n1234567890").build()
                noteContent
            )
                .setColor(argb(Colors.DEFAULT.onSurface))
                .setTypography(
                    when (deviceParameters.screenWidthDp) {
                        in 0..210 ->
                            when (noteContent.length) {
                                in 0..4 -> Typography.TYPOGRAPHY_DISPLAY1
                                in 5..8 -> Typography.TYPOGRAPHY_DISPLAY2
                                in 9..10 -> Typography.TYPOGRAPHY_DISPLAY3
                                in 11..12 -> Typography.TYPOGRAPHY_TITLE1
                                in 13..24 -> Typography.TYPOGRAPHY_TITLE2
                                in 25..40 -> Typography.TYPOGRAPHY_TITLE3
                                in 41..44 -> Typography.TYPOGRAPHY_BODY2
                                in 45..52 -> Typography.TYPOGRAPHY_CAPTION2
                                else -> Typography.TYPOGRAPHY_CAPTION3
                            }
                        else ->
                            when (noteContent.length) {
                                in 0..8 -> Typography.TYPOGRAPHY_DISPLAY1
                                in 9..10 -> Typography.TYPOGRAPHY_DISPLAY2
                                in 11..12 -> Typography.TYPOGRAPHY_DISPLAY3
                                in 13..24 -> Typography.TYPOGRAPHY_TITLE1
                                in 25..36 -> Typography.TYPOGRAPHY_TITLE2
                                in 37..48 -> Typography.TYPOGRAPHY_TITLE3
                                in 49..52 -> Typography.TYPOGRAPHY_BODY2
                                in 53..64 -> Typography.TYPOGRAPHY_CAPTION2
                                else -> Typography.TYPOGRAPHY_CAPTION3
                            }
                    }
                )
                .setMaxLines(4)
                .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_TRUNCATE)
                .build()
        )
        .setPrimaryChipContent(
            CompactChip.Builder(
                context, resources.getString(R.string.edit), ModifiersBuilders.Clickable.Builder()
                    .setId("editNote")
                    .setOnClick(ActionBuilders.LoadAction.Builder().build())
                    .build(),
                deviceParameters
            )
                .setChipColors(chipColors)
                .build()

        ).build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(
    onTileRequest = { request ->
        TilePreviewHelper.singleTimelineEntryTileBuilder(
            tileLayout(context, request.deviceConfiguration, "Test note lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."),
        ).build()
    }
)