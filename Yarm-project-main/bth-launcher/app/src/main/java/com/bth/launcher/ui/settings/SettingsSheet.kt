package com.bth.launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bth.launcher.LauncherUiState
import com.bth.launcher.LauncherViewModel
import com.bth.launcher.data.LauncherTheme
import com.bth.launcher.ui.components.GlassCard
import com.bth.launcher.ui.lock.PinDialog
import com.bth.launcher.ui.theme.LocalLauncherColors
import com.bth.launcher.ui.theme.accentPalette

@Composable
fun SettingsSheet(
    state: LauncherUiState,
    viewModel: LauncherViewModel,
    onClose: () -> Unit
) {
    val colors = LocalLauncherColors.current
    var showPinSetup by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الإعدادات",
                color = colors.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, "إغلاق", tint = colors.text)
            }
        }

        // Theme selection
        SettingsSection(title = "المظهر") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                LauncherTheme.entries.forEach { theme ->
                    ThemeChip(
                        label = theme.labelAr,
                        selected = state.theme == theme,
                        onClick = { viewModel.setTheme(theme) }
                    )
                }
            }
        }

        // Accent color
        SettingsSection(title = "لون التمييز") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                accentPalette.forEach { (key, color) ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (state.accentColor == key) 3.dp else 0.dp,
                                color = if (state.accentColor == key) colors.text else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { viewModel.setAccent(key) }
                    )
                }
            }
        }

        // Grid columns
        SettingsSection(title = "عدد الأعمدة: ${state.gridColumns}") {
            Slider(
                value = state.gridColumns.toFloat(),
                onValueChange = { viewModel.setGridColumns(it.toInt()) },
                valueRange = 3f..6f,
                steps = 2,
                colors = SliderDefaults.colors(
                    thumbColor = colors.accent,
                    activeTrackColor = colors.accent
                )
            )
        }

        // Show labels
        SettingsSection(title = "أسماء التطبيقات") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("إظهار أسماء التطبيقات", color = colors.text, fontSize = 15.sp)
                Switch(
                    checked = state.showLabels,
                    onCheckedChange = { viewModel.setShowLabels(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = colors.accent)
                )
            }
        }

        // App lock PIN
        SettingsSection(title = "الحماية") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showPinSetup = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (state.hasPin) "تغيير رمز القفل" else "تعيين رمز قفل التطبيقات",
                        color = colors.text,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (state.hasPin) "مفعل" else "غير مفعل",
                        color = if (state.hasPin) colors.accent else colors.textSecondary,
                        fontSize = 14.sp
                    )
                }
                if (state.hasPin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.clearPin() }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "إزالة رمز القفل",
                            color = colors.textSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Hidden apps management
        val hiddenApps = state.allApps.filter { it.isHidden }
        if (hiddenApps.isNotEmpty()) {
            SettingsSection(title = "التطبيقات المخفية (${hiddenApps.size})") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    hiddenApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleHidden(app.packageName) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(app.name, color = colors.text, fontSize = 15.sp)
                            Text("إظهار", color = colors.accent, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Maintenance
        SettingsSection(title = "الصيانة") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.refreshApps() }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Text("إعادة فحص التطبيقات", color = colors.text, fontSize = 15.sp)
            }
        }

        Text(
            text = "BTH Launcher 1.0",
            color = colors.textSecondary,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }

    if (showPinSetup) {
        PinDialog(
            title = "أدخل رمز قفل جديد",
            isSettingPin = true,
            onVerify = { true },
            onPinSet = { viewModel.setPin(it) },
            onSuccess = { showPinSetup = false },
            onDismiss = { showPinSetup = false }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    val colors = LocalLauncherColors.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            content()
        }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalLauncherColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (selected) colors.accent else colors.glassSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) colors.onAccent else colors.text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
