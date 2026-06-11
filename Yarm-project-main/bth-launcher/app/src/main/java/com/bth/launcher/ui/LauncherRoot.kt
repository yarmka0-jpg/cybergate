package com.bth.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.bth.launcher.LauncherViewModel
import com.bth.launcher.MainActivity
import com.bth.launcher.data.AppInfo
import com.bth.launcher.ui.drawer.AppDrawer
import com.bth.launcher.ui.home.HomeScreen
import com.bth.launcher.ui.lock.PinDialog
import com.bth.launcher.ui.search.SearchOverlay
import com.bth.launcher.ui.settings.SettingsSheet
import com.bth.launcher.ui.theme.LocalLauncherColors

enum class LauncherScreen { HOME, DRAWER, SEARCH, SETTINGS }

@Composable
fun LauncherRoot(viewModel: LauncherViewModel) {
    val state by viewModel.uiState.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val colors = LocalLauncherColors.current
    val activity = LocalContext.current as MainActivity

    var screen by remember { mutableStateOf(LauncherScreen.HOME) }
    var pendingLockedApp by remember { mutableStateOf<AppInfo?>(null) }

    // Home button pressed while launcher open: collapse everything
    LaunchedEffect(Unit) {
        viewModel.goHomeEvents.collect {
            screen = LauncherScreen.HOME
            pendingLockedApp = null
        }
    }

    BackHandler(enabled = screen != LauncherScreen.HOME) {
        screen = LauncherScreen.HOME
    }

    fun launch(app: AppInfo) {
        if (app.isLocked && state.hasPin) {
            pendingLockedApp = app
        } else {
            activity.launchApp(app.packageName)
            screen = LauncherScreen.HOME
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.backgroundTop, colors.backgroundBottom)
                )
            )
    ) {
        HomeScreen(
            state = state,
            weather = weather,
            onLaunchApp = ::launch,
            onOpenDrawer = { screen = LauncherScreen.DRAWER },
            onOpenSearch = { screen = LauncherScreen.SEARCH },
            onOpenSettings = { screen = LauncherScreen.SETTINGS },
            onSwipeDown = { activity.expandNotifications() }
        )

        // PERF: the drawer stays composed at all times (icons pre-warmed) and is
        // animated purely on the draw layer. Previously AnimatedVisibility built
        // the whole grid DURING the slide animation, causing visible jank on the
        // first open. When fully closed it is translated off-screen, so it can
        // neither be seen nor receive touches.
        val drawerProgress by animateFloatAsState(
            targetValue = if (screen == LauncherScreen.DRAWER) 1f else 0f,
            animationSpec = tween(durationMillis = 260),
            label = "drawerProgress"
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = drawerProgress
                    translationY = if (drawerProgress == 0f) size.height
                    else size.height / 3f * (1f - drawerProgress)
                }
        ) {
            AppDrawer(
                state = state,
                onLaunchApp = ::launch,
                onToggleHidden = viewModel::toggleHidden,
                onToggleLocked = viewModel::toggleLocked,
                onToggleFavorite = viewModel::toggleFavorite,
                onUninstall = activity::uninstallApp,
                onAppInfo = activity::openAppInfo,
                onClose = { screen = LauncherScreen.HOME },
                onOpenSearch = { screen = LauncherScreen.SEARCH }
            )
        }

        // Smart search: drops down from the top
        AnimatedVisibility(
            visible = screen == LauncherScreen.SEARCH,
            enter = slideInVertically(initialOffsetY = { -it / 3 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it / 3 }) + fadeOut()
        ) {
            SearchOverlay(
                onSearch = viewModel::search,
                mostUsed = state.mostUsed,
                allApps = state.visibleApps,
                onLaunchApp = ::launch,
                onClose = { screen = LauncherScreen.HOME }
            )
        }

        AnimatedVisibility(
            visible = screen == LauncherScreen.SETTINGS,
            enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 3 }) + fadeOut()
        ) {
            SettingsSheet(
                state = state,
                viewModel = viewModel,
                onClose = { screen = LauncherScreen.HOME }
            )
        }

        // First-load indicator (apps list still being built)
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.accent)
            }
        }

        // PIN entry for locked apps
        pendingLockedApp?.let { app ->
            PinDialog(
                title = "افتح ${app.name}",
                onVerify = { pin -> viewModel.verifyPin(pin) },
                onSuccess = {
                    activity.launchApp(app.packageName)
                    pendingLockedApp = null
                    screen = LauncherScreen.HOME
                },
                onDismiss = { pendingLockedApp = null }
            )
        }
    }
}
