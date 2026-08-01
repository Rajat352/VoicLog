package com.voiclog.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route: NavKey {
    @Serializable
    data object Record: Route, NavKey
    @Serializable
    data object Log: Route, NavKey

    @Serializable
    data object Settings: Route, NavKey
}