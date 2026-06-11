package com.bth.launcher.ui.home

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bth.launcher.LauncherUiState
import com.bth.launcher.data.AppInfo
import com.bth.launcher.data.WeatherInfo
import com.bth.launcher.ui.components.AppIconItem
import com.bth.launcher.ui.components.GlassCard
import com.bth.launcher.ui.components.GlassPill
import com.bth.launcher.ui.theme.LocalLauncherColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home screen:
 * - Hero clock + date + weather pill at the top
 * - Most-used quick row in the middle
 * - Search pill + dock + drawer handle at the bottom
 * Gestures (fixed): swipe UP opens the drawer, swipe DOWN expands notifications.
 */
@Composable
fun HomeScreen(
    state: LauncherUiState,
    weather: WeatherInfo?,
    onLaunchApp: (AppInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwipeDown: () -> Unit
) {
    val colors = LocalLauncherColors.current
    var dragTotal by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onDragEnd = {
                        when {
                            dragTotal < -120f -> onOpenDrawer()   // swipe up
                            dragTotal > 120f -> onSwipeDown()     // swipe down
                        }
                        dragTotal = 0f
                    },
                    onVerticalDrag = { _, delta -> dragTotal += delta }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ===== Top: hero clock =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onOpenSearch) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "بحث",
                            tint = colors.text,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "إعدادات",
                            tint = colors.text,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
                HeroClock(weather = weather)
            }

            // ===== Middle: most used =====
            if (state.mostUsed.isNotEmpty()) {
                MostUsedRow(
                    apps = state.mostUsed.take(4),
                    showLabels = state.showLabels,
                    onLaunchApp = onLaunchApp
                )
            }

            // ===== Bottom: dock + drawer handle =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DockRow(
                    favorites = state.favorites.take(5),
                    showLabels = false,
                    onLaunchApp = onLaunchApp,
                    onOpenDrawer = onOpenDrawer
                )
                DrawerHandle(onClick = onOpenDrawer)
            }
        }
    }
}

@Composable
private fun HeroClock(weather: WeatherInfo?) {
    val colors = LocalLauncherColors.current
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE، dd MMMM", Locale("ar")) }
    var now by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000L * (60 - (System.currentTimeMillis() / 1000 % 60)))
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = timeFormat.format(now),
            color = colors.text,
            fontSize = 84.sp,
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = (-2).sp
        )
        Text(
            text = dateFormat.format(now),
            color = colors.textSecondary,
            fontSize = 16.sp
        )
        weather?.let {
            Spacer(modifier = Modifier.height(14.dp))
            GlassPill {
                Text(
                    text = "${it.temperature.toInt()}° • ${it.description}",
                    color = colors.accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MostUsedRow(
    apps: List<AppInfo>,
    showLabels: Boolean,
    onLaunchApp: (AppInfo) -> Unit
) {
    val colors = LocalLauncherColors.current
    Column {
        Text(
            text = "الأكثر استخداماً",
            color = colors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                apps.forEach { app ->
                    AppIconItem(
                        app = app,
                        iconSize = 52.dp,
                        showLabel = showLabels,
                        onClick = { onLaunchApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DockRow(
    favorites: List<AppInfo>,
    showLabels: Boolean,
    onLaunchApp: (AppInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalLauncherColors.current
    GlassCard(modifier = modifier.fillMaxWidth(), cornerRadius = 28.dp) {
        if (favorites.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenDrawer)
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Apps,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "اضغط مطولاً على تطبيق لإضافته هنا",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                favorites.forEach { app ->
                    AppIconItem(
                        app = app,
                        iconSize = 56.dp,
                        showLabel = showLabels,
                        onClick = { onLaunchApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerHandle(onClick: () -> Unit) {
    val colors = LocalLauncherColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = "فتح درج التطبيقات",
            tint = colors.textSecondary,
            modifier = Modifier.size(22.dp)
        )
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(colors.glassBorder)
        )
    }
}
