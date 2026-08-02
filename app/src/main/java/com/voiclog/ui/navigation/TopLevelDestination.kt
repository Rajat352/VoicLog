package com.voiclog.ui.navigation

import com.voiclog.R

enum class TopLevelDestination(
    val route: Route.TopLevel,
    val icon: Int,
    val label: String
) {
    RECORD(Route.TopLevel.Record, R.drawable.mic_icon, "Record"),
    LOG(Route.TopLevel.Log, R.drawable.log_icon, "Log"),
    SETTINGS(Route.TopLevel.Settings, R.drawable.settings_icon, "Settings")
}