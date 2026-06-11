package com.bth.launcher.ui.lock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bth.launcher.ui.components.GlassCard
import com.bth.launcher.ui.theme.LocalLauncherColors
import kotlinx.coroutines.launch

/**
 * PIN pad dialog with shake animation on wrong entry.
 * Used both for unlocking locked apps and for setting a new PIN.
 */
@Composable
fun PinDialog(
    title: String,
    onVerify: suspend (String) -> Boolean,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    isSettingPin: Boolean = false,
    onPinSet: (String) -> Unit = {}
) {
    val colors = LocalLauncherColors.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    suspend fun shake() {
        for (offset in listOf(16f, -14f, 10f, -6f, 3f, 0f)) {
            shakeOffset.animateTo(offset, tween(45))
        }
    }

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            if (isSettingPin) {
                onPinSet(pin)
                onSuccess()
            } else if (onVerify(pin)) {
                onSuccess()
            } else {
                error = true
                shake()
                pin = ""
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.graphicsLayer { translationX = shakeOffset.value }
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = title,
                    color = colors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (error) {
                    Text(
                        text = "رمز خاطئ، حاول مرة أخرى",
                        color = colors.accent,
                        fontSize = 13.sp
                    )
                }

                // PIN dots
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index < pin.length) colors.accent
                                    else colors.glassBorder
                                )
                        )
                    }
                }

                // Number pad
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "⌫")
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { key ->
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (key.isNotEmpty()) colors.glassSurface
                                            else androidx.compose.ui.graphics.Color.Transparent
                                        )
                                        .clickable(enabled = key.isNotEmpty()) {
                                            error = false
                                            when (key) {
                                                "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                else -> if (pin.length < 4) pin += key
                                            }
                                        }
                                ) {
                                    if (key == "⌫") {
                                        Icon(
                                            Icons.Filled.Backspace,
                                            contentDescription = "حذف",
                                            tint = colors.text,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            color = colors.text,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
