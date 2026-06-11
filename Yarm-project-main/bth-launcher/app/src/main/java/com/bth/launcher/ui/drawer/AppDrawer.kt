package com.bth.launcher.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bth.launcher.LauncherUiState
import com.bth.launcher.data.AppCategory
import com.bth.launcher.data.AppInfo
import com.bth.launcher.ui.components.GlassCard
import com.bth.launcher.ui.components.AppIconItem
import com.bth.launcher.ui.theme.LocalLauncherColors

/**
 * App drawer:
 * - Built-in instant filter field
 * - Category tabs with live app counts
 * - Long-press context menu: favorite / hide / lock / app info / uninstall
 * - Scrolls back to top when switching category (no jank: icons are pre-rendered)
 */
@Composable
fun AppDrawer(
    state: LauncherUiState,
    onLaunchApp: (AppInfo) -> Unit,
    onToggleHidden: (String) -> Unit,
    onToggleLocked: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onUninstall: (String) -> Unit,
    onAppInfo: (String) -> Unit,
    onClose: () -> Unit,
    onOpenSearch: () -> Unit
) {
    val colors = LocalLauncherColors.current
    var selectedCategory by remember { mutableStateOf<AppCategory?>(null) }
    var filter by remember { mutableStateOf("") }
    var contextMenuApp by remember { mutableStateOf<AppInfo?>(null) }
    val gridState = rememberLazyGridState()

    val categoryCounts = remember(state.visibleApps) {
        state.visibleApps.groupingBy { it.category }.eachCount()
    }

    val categories = remember(categoryCounts) {
        categoryCounts.keys.sortedBy { it.ordinal }
    }

    val displayedApps = remember(state.visibleApps, selectedCategory, filter) {
        val base = if (selectedCategory == null) state.visibleApps
        else state.visibleApps.filter { it.category == selectedCategory }
        if (filter.isBlank()) base
        else {
            val q = filter.trim().lowercase()
            base.filter { it.name.lowercase().contains(q) }
        }
    }

    // Jump back to the top whenever the category or filter changes
    LaunchedEffect(selectedCategory, filter) {
        gridState.scrollToItem(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .systemBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "التطبيقات",
                    color = colors.text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.visibleApps.size} تطبيق",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
            Row {
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Filled.Search, "بحث شامل", tint = colors.text)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, "إغلاق", tint = colors.text)
                }
            }
        }

        // Instant filter field
        OutlinedTextField(
            value = filter,
            onValueChange = { filter = it },
            placeholder = { Text("تصفية سريعة...", color = colors.textSecondary, fontSize = 14.sp) },
            singleLine = true,
            shape = RoundedCornerShape(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.text,
                unfocusedTextColor = colors.text,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.glassBorder,
                cursorColor = colors.accent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Category tabs with counts
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            item(key = "all") {
                CategoryChip(
                    label = "الكل",
                    count = state.visibleApps.size,
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null }
                )
            }
            items(categories.size, key = { categories[it].name }) { index ->
                val category = categories[index]
                CategoryChip(
                    label = category.labelAr,
                    count = categoryCounts[category] ?: 0,
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }

        if (displayedApps.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.SearchOff,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "لا توجد نتائج",
                    color = colors.textSecondary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(state.gridColumns),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedApps, key = { it.packageName }) { app ->
                    AppIconItem(
                        app = app,
                        showLabel = state.showLabels,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { contextMenuApp = app }
                    )
                }
            }
        }
    }

    // Long-press context menu
    contextMenuApp?.let { app ->
        AppContextMenu(
            app = app,
            isFavorite = state.favorites.any { it.packageName == app.packageName },
            onToggleFavorite = { onToggleFavorite(app.packageName); contextMenuApp = null },
            onToggleHidden = { onToggleHidden(app.packageName); contextMenuApp = null },
            onToggleLocked = { onToggleLocked(app.packageName); contextMenuApp = null },
            onAppInfo = { onAppInfo(app.packageName); contextMenuApp = null },
            onUninstall = { onUninstall(app.packageName); contextMenuApp = null },
            onDismiss = { contextMenuApp = null }
        )
    }
}

@Composable
private fun CategoryChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalLauncherColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (selected) colors.accent else colors.glassSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) colors.onAccent else colors.text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = count.toString(),
            color = if (selected) colors.onAccent.copy(alpha = 0.7f) else colors.textSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun AppContextMenu(
    app: AppInfo,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleHidden: () -> Unit,
    onToggleLocked: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalLauncherColors.current

    Dialog(onDismissRequest = onDismiss) {
        GlassCard {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    AppIconItem(
                        app = app,
                        iconSize = 40.dp,
                        showLabel = false,
                        onClick = {}
                    )
                    Text(
                        text = app.name,
                        color = colors.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                MenuRow(
                    icon = { Icon(Icons.Filled.Favorite, null, tint = colors.accent, modifier = Modifier.size(20.dp)) },
                    label = if (isFavorite) "إزالة من الدوك" else "إضافة إلى الدوك",
                    onClick = onToggleFavorite
                )
                MenuRow(
                    icon = { Icon(Icons.Filled.VisibilityOff, null, tint = colors.accent, modifier = Modifier.size(20.dp)) },
                    label = if (app.isHidden) "إظهار التطبيق" else "إخفاء التطبيق",
                    onClick = onToggleHidden
                )
                MenuRow(
                    icon = { Icon(Icons.Filled.Lock, null, tint = colors.accent, modifier = Modifier.size(20.dp)) },
                    label = if (app.isLocked) "إلغاء قفل التطبيق" else "قفل التطبيق برمز",
                    onClick = onToggleLocked
                )
                MenuRow(
                    icon = { Icon(Icons.Filled.Info, null, tint = colors.accent, modifier = Modifier.size(20.dp)) },
                    label = "معلومات التطبيق",
                    onClick = onAppInfo
                )
                MenuRow(
                    icon = { Icon(Icons.Filled.Delete, null, tint = colors.accent, modifier = Modifier.size(20.dp)) },
                    label = "إلغاء التثبيت",
                    onClick = onUninstall
                )
            }
        }
    }
}

@Composable
private fun MenuRow(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    val colors = LocalLauncherColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Text(text = label, color = colors.text, fontSize = 15.sp)
    }
}
