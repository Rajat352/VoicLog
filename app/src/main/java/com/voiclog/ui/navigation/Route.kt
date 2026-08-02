package com.voiclog.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route: NavKey {

    sealed interface TopLevel: Route {
        @Serializable data object Record: TopLevel
        @Serializable data object Log: TopLevel
        @Serializable data object Settings: TopLevel
    }

}