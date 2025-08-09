/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.studio1a23.simplenote.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.createGraph
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.studio1a23.simplenote.R
import com.studio1a23.simplenote.presentation.theme.SimpleNoteTheme
import com.studio1a23.simplenote.ui.Edit
import com.studio1a23.simplenote.ui.EditNoteType
import com.studio1a23.simplenote.ui.NoteHistory
import com.studio1a23.simplenote.viewModel.MainViewModel

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            navController = rememberSwipeDismissableNavController()
            WearApp(swipeDismissableNavController = navController, viewModel = viewModel)
            LaunchedEffect(Unit) {
                viewModel.loadData()
                intent?.getStringExtra("destination")?.let { destination ->
                    navController.navigate(
                        destination,
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.init()
    }

    override fun onPause() {
        super.onPause()
        viewModel.unload()
    }
}

@Composable
fun WearApp(
    modifier: Modifier = Modifier,
    swipeDismissableNavController: NavHostController = rememberSwipeDismissableNavController(),
    viewModel: MainViewModel,
) {
    SimpleNoteTheme {
        AppScaffold() {
            swipeDismissableNavController.setLifecycleOwner(LocalLifecycleOwner.current)
            swipeDismissableNavController.setViewModelStore(LocalViewModelStoreOwner.current!!.viewModelStore)
            swipeDismissableNavController.graph = remember {
                swipeDismissableNavController.createGraph(startDestination = "home") {
                    composable(route = "home") {
                        Greeting(
                            noteContent = viewModel.noteContentFlow.collectAsState().value,
                            onNavEdit = { swipeDismissableNavController.navigate("edit") },
                            onNavHistory = { swipeDismissableNavController.navigate("history") },
                        )
                    }
                    composable(route = "edit") {
                        Edit(
                            viewModel,
                            onSave = { swipeDismissableNavController.popBackStack() },
                            onChangeNoteType = { swipeDismissableNavController.navigate("editNoteType") }
                        )
                    }
                    composable(route = "editNoteType") {
                        EditNoteType(viewModel)
                    }
                    composable(route = "history") {
                        NoteHistory(viewModel)
                    }
                }
            }
            SwipeDismissableNavHost(
                modifier = modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                navController = swipeDismissableNavController,
                graph = swipeDismissableNavController.graph,
            )
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Greeting(noteContent: String, onNavEdit: () -> Unit = {}, onNavHistory: () -> Unit = {}) {
    val columnState = rememberScalingLazyListState()
    ScreenScaffold(
        columnState,
        contentPadding = PaddingValues(16.dp, 16.dp),
        edgeButtonSpacing = 8.dp,
        edgeButton = {
            EdgeButton(onClick = onNavEdit) { Text(stringResource(R.string.button_edit)) }
        }
    ) {
        contentPadding ->
        ScalingLazyColumn(
            state = columnState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = contentPadding,
            autoCentering = null,
        ) {
            item {
                ListHeader {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            item {
                Card(onClick = {}, enabled = false) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .defaultMinSize(minHeight = CardDefaults.Height - CardDefaults.ContentPadding.calculateTopPadding() - CardDefaults.ContentPadding.calculateBottomPadding())
                            .padding(0.dp),
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = if (noteContent.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryDim,
                            fontStyle = if (noteContent.isNotBlank()) FontStyle.Normal else FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            text = noteContent.ifBlank { stringResource(R.string.empty_note) },
                        )
                    }
                }
            }
            item {
                CompactButton(
                    onClick = onNavHistory,
                    colors = ButtonDefaults.outlinedButtonColors(),
                    border = ButtonDefaults.outlinedButtonBorder(true),
                    modifier = Modifier.padding(0.dp)
                ) { Text(stringResource(R.string.button_history)) }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun GreetingPreview() {
    SimpleNoteTheme {
        Greeting(noteContent = "")
    }
}

