package com.voiclog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.voiclog.ui.screens.log.LogScreen
import com.voiclog.ui.screens.record.RecordScreen
import com.voiclog.ui.screens.settings.SettingsScreen

@Composable
fun RootNav(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(Route.Record)

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Route.Record> {
                RecordScreen()
            }
            entry<Route.Log> {
                LogScreen()
            }
            entry<Route.Settings> {
                SettingsScreen()
            }
        }
    )
}