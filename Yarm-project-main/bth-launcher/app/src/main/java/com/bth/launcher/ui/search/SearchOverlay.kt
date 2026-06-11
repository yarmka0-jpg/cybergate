package com.bth.launcher.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bth.launcher.data.AppInfo
import com.bth.launcher.ui.theme.LocalLauncherColors

/**
 * Smart search overlay: instant ranked results, a "most used" section before
 * any text is typed, and IME Go launches the top result.
 */
@Composable
fun SearchOverlay(
    onSearch: (String) -> List<AppInfo>,
    mostUsed: List<AppInfo>,
    allApps: List<AppInfo>,
    onLaunchApp: (AppInfo) -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalLauncherColors.current
    var query by remember { mutableStateOf("") }
    val results = remember(query) { onSearch(query) }
    val focusRequester = remember { FocusRequester() }

    val suggestions = remember(mostUsed, allApps) {
        if (mostUsed.isNotEmpty()) mostUsed else allApps.take(8)
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("ابحث عن تطبيق...", color = colors.textSecondary) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = colors.accent) },
                singleLine = true,
                shape = RoundedCornerShape(100.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = { results.firstOrNull()?.let(onLaunchApp) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.glassBorder,
                    cursorColor = colors.accent
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, "إغلاق", tint = colors.text)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (query.isBlank()) {
            Text(
                text = if (mostUsed.isNotEmpty()) "الأكثر استخداماً" else "اقتراحات",
                color = colors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(suggestions, key = { it.packageName }) { app ->
                    SearchResultRow(app = app, onClick = { onLaunchApp(app) })
                }
            }
        } else if (results.isEmpty()) {
            Text(
                text = "لا توجد نتائج لـ \"$query\"",
                color = colors.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(results, key = { it.packageName }) { app ->
                    SearchResultRow(app = app, onClick = { onLaunchApp(app) })
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(app: AppInfo, onClick: () -> Unit) {
    val colors = LocalLauncherColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            bitmap = app.icon,
            contentDescription = null,
            modifier = Modifier.size(44.dp)
        )
        Column {
            Text(text = app.name, color = colors.text, fontSize = 16.sp)
            Text(
                text = app.category.labelAr,
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }
    }
}
