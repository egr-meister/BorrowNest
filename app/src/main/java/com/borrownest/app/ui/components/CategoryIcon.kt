package com.borrownest.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.ui.graphics.vector.ImageVector
import com.borrownest.app.model.ItemCategory

/** Simple Material icon per category. No photos, no avatars. */
fun categoryIcon(category: ItemCategory): ImageVector = when (category) {
    ItemCategory.Book -> Icons.Outlined.Book
    ItemCategory.Tool -> Icons.Outlined.Build
    ItemCategory.Clothing -> Icons.Outlined.Checkroom
    ItemCategory.Electronics -> Icons.Outlined.Devices
    ItemCategory.Household -> Icons.Outlined.Home
    ItemCategory.Sports -> Icons.Outlined.SportsBasketball
    ItemCategory.Document -> Icons.Outlined.Description
    ItemCategory.Accessory -> Icons.Outlined.Watch
    ItemCategory.Other -> Icons.Outlined.Category
}
