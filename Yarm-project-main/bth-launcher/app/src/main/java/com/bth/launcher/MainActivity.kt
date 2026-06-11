package com.bth.launcher

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.bth.launcher.ui.LauncherRoot
import com.bth.launcher.ui.theme.LauncherThemeProvider

class MainActivity : ComponentActivity() {

    val viewModel: LauncherViewModel by viewModels()

    /**
     * Package add/remove broadcasts CANNOT be received via manifest-declared
     * receivers on Android 8+. They must be registered dynamically — this is
     * why newly installed apps never appeared before.
     */
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.refreshApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ContextCompat.registerReceiver(
            this,
            packageReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            },
            ContextCompat.RECEIVER_EXPORTED
        )

        setContent {
            // PERF: collect only theme/accent here — collecting the whole uiState
            // recomposed the entire app root on every state change (e.g. each app launch).
            val theme by viewModel.themeState.collectAsState()
            val accent by viewModel.accentState.collectAsState()
            LauncherThemeProvider(theme = theme, accentKey = accent) {
                LauncherRoot(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Home pressed while launcher is open: collapse all overlays
        viewModel.signalGoHome()
    }

    fun launchApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent)
                viewModel.onAppLaunched(packageName)
            } else {
                Toast.makeText(this, "تعذر فتح التطبيق", Toast.LENGTH_SHORT).show()
                viewModel.refreshApps()
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "التطبيق غير موجود", Toast.LENGTH_SHORT).show()
            viewModel.refreshApps()
        } catch (e: Exception) {
            Toast.makeText(this, "تعذر فتح التطبيق", Toast.LENGTH_SHORT).show()
        }
    }

    fun uninstallApp(packageName: String) {
        try {
            startActivity(
                Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName"))
            )
        } catch (e: Exception) {
            Toast.makeText(this, "تعذر إلغاء التثبيت", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAppInfo(packageName: String) {
        try {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
            )
        } catch (e: Exception) {
            Toast.makeText(this, "تعذر فتح معلومات التطبيق", Toast.LENGTH_SHORT).show()
        }
    }

    fun expandNotifications() {
        try {
            @Suppress("WrongConstant", "PrivateApi")
            val service = getSystemService("statusbar")
            Class.forName("android.app.StatusBarManager")
                .getMethod("expandNotificationsPanel")
                .invoke(service)
        } catch (e: Exception) {
            // Not supported on this device/OS version
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(packageReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }
}
