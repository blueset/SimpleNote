package com.studio1a23.simplenote.tile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.toColor
import androidx.core.net.toUri
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.StateBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.TypeBuilders
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
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.SuspendingTileService
import com.studio1a23.simplenote.presentation.MainActivity
import com.studio1a23.simplenote.tile.MainTileService.Companion.KEY_NOTE
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

    @SuppressLint("VisibleForTests")
    private suspend fun getNoteContent(): String {
        val dataClient = Wearable.getDataClient(application)
        val dataItems = dataClient.getDataItems(Uri.parse("wear://*/note")).await()
        dataItems.forEach { item ->
            val dataMap = DataMapItem.fromDataItem(item).dataMap
            if (dataMap.containsKey("note")) {
                return dataMap.getString("note") ?: "No note found."
            }
        }
        return "No note found."
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val lastClickableId = requestParams.currentState.lastClickableId
        if (lastClickableId == "foo") {
            TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(
                    Intent(applicationContext, MainActivity::class.java).putExtra(
                        "destination",
                        "edit"
                    )
                )
                .startActivities()
        }

        val noteContent = getNoteContent()

        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder().setRoot(tileLayout(this, noteContent)).build()
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

private fun tileLayout(context: Context, noteContent: String = ""): LayoutElementBuilders.LayoutElement {
    val chipColors = ChipColors.primaryChipColors(Colors.DEFAULT)
    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setPrimaryLabelTextContent(
            Text.Builder(context, "Quick note")
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
                    .setTypography(when (noteContent.length) {
                        in 0..4 -> Typography.TYPOGRAPHY_DISPLAY1
                        in 5..8 -> Typography.TYPOGRAPHY_DISPLAY2
                        in 9..10 -> Typography.TYPOGRAPHY_DISPLAY3
                        in 11..12 -> Typography.TYPOGRAPHY_TITLE1
                        in 13..24 -> Typography.TYPOGRAPHY_TITLE2
                        in 25..40 -> Typography.TYPOGRAPHY_TITLE3
                        in 41..44 -> Typography.TYPOGRAPHY_BODY2
                        in 45..52 -> Typography.TYPOGRAPHY_CAPTION2
                    else -> Typography.TYPOGRAPHY_CAPTION3
                })
                .setMaxLines(4)
                .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE)
                .build()
        )
        .setPrimaryChipContent(
            CompactChip.Builder(
                context, "Edit", ModifiersBuilders.Clickable.Builder()
                    .setId("foo")
                    .setOnClick(ActionBuilders.LoadAction.Builder().build())
                    .build(),
                buildDeviceParameters(context.resources)
            )
                .setChipColors(chipColors)
                .build()

        ).build()
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun TilePreview() {
    LayoutRootPreview(root = tileLayout(LocalContext.current, "#124"))
}