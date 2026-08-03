package com.voiclog.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.voiclog.ui.screens.log.LogScreen
import com.voiclog.ui.screens.record.RecordScreen
import com.voiclog.ui.screens.settings.SettingsScreen
import com.voiclog.ui.theme.VoicLogTypography

@Composable
fun RootNav(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(Route.TopLevel.Record)
    val currentKey = backStack.lastOrNull()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentKey is Route.TopLevel) {
                NavigationBar() {
                    TopLevelDestination.entries.forEach {
                        NavigationBarItem(
                            selected = currentKey == it.route,
                            icon = {
                                Icon(
                                    painter = painterResource(it.icon),
                                    contentDescription = it.label
                                )
                            },
                            label = {
                                Text(
                                    text = it.label,
                                    style = VoicLogTypography.labelMedium
                                )
                            },
                            onClick = {
                                backStack.switchTab(it.route)
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavDisplay(
            modifier = modifier.padding(paddingValues),
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<Route.TopLevel.Record> {
                    RecordScreen()
                }
                entry<Route.TopLevel.Log> {
                    LogScreen()
                }
                entry<Route.TopLevel.Settings> {
                    SettingsScreen()
                }
            }
        )
    }

}

// Treats Record as the navigation stack root
fun NavBackStack<NavKey>.switchTab(target: Route.TopLevel) {
    if (lastOrNull() == target) return
    clear()
    add(Route.TopLevel.Record)
    if (target != Route.TopLevel.Record) add(target)
}