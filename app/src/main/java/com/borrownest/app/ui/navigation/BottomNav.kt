package com.borrownest.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    Board(Routes.BOARD, "Board", Icons.Filled.Dashboard),
    History(Routes.HISTORY, "History", Icons.Filled.History),
    Archive(Routes.ARCHIVE, "Archive", Icons.Filled.Inventory2),
    Settings(Routes.SETTINGS, "Settings", Icons.Filled.Settings)
}
