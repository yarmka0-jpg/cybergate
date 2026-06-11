package com.bth.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads installed apps with their icons pre-rendered to ImageBitmap on
 * background threads. Icons are cached in memory by package name, so
 * subsequent refreshes (e.g. after install/uninstall) are nearly instant
 * and the UI thread NEVER does bitmap work.
 */
class AppRepository(private val context: Context) {

    private val iconCache = ConcurrentHashMap<String, ImageBitmap>()

    suspend fun loadApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        val resolved = pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }

        // Drop cache entries for uninstalled packages to avoid leaks
        val installed = resolved.map { it.activityInfo.packageName }.toHashSet()
        iconCache.keys.retainAll(installed)

        coroutineScope {
            resolved.map { resolveInfo ->
                async(Dispatchers.Default) {
                    val pkg = resolveInfo.activityInfo.packageName
                    try {
                        val icon = iconCache.getOrPut(pkg) {
                            resolveInfo.loadIcon(pm)
                                .toBitmap(ICON_SIZE_PX, ICON_SIZE_PX)
                                .asImageBitmap()
                        }
                        val appInfo = try {
                            pm.getApplicationInfo(pkg, 0)
                        } catch (e: Exception) {
                            null
                        }
                        AppInfo(
                            name = resolveInfo.loadLabel(pm).toString(),
                            packageName = pkg,
                            icon = icon,
                            category = categorize(pkg, appInfo)
                        )
                    } catch (e: Exception) {
                        // A single broken package must never break the whole list
                        null
                    }
                }
            }.awaitAll()
        }
            .filterNotNull()
            .sortedBy { it.name.lowercase() }
    }

    private fun categorize(pkg: String, info: ApplicationInfo?): AppCategory {
        heuristic(pkg)?.let { return it }

        return when (info?.category) {
            ApplicationInfo.CATEGORY_GAME -> AppCategory.GAMES
            ApplicationInfo.CATEGORY_AUDIO,
            ApplicationInfo.CATEGORY_VIDEO,
            ApplicationInfo.CATEGORY_IMAGE -> AppCategory.MEDIA
            ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
            ApplicationInfo.CATEGORY_NEWS -> AppCategory.PRODUCTIVITY
            ApplicationInfo.CATEGORY_MAPS -> AppCategory.TOOLS
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
            else -> AppCategory.OTHER
        }
    }

    private fun heuristic(pkg: String): AppCategory? {
        val p = pkg.lowercase()
        return when {
            listOf("whatsapp", "telegram", "messenger", "signal", "viber", "imo",
                "dialer", "contacts", "messaging", "mms", "discord", "skype", "zoom",
                "teams", "botim").any { p.contains(it) } -> AppCategory.COMMUNICATION

            listOf("instagram", "facebook", "twitter", "snapchat", "tiktok",
                "musically", "reddit", "linkedin", "pinterest", "threads",
                "com.zhiliaoapp").any { p.contains(it) } -> AppCategory.SOCIAL

            listOf("youtube", "netflix", "spotify", "music", "video", "player",
                "camera", "gallery", "photos", "anghami", "shahid", "soundcloud",
                "vlc", "podcast").any { p.contains(it) } -> AppCategory.MEDIA

            listOf("game", "pubg", "freefire", "supercell", "roblox", "minecraft",
                "garena", "tencent.ig", "activision", "ea.gp", "kiloo",
                "king.com").any { p.contains(it) } -> AppCategory.GAMES

            listOf("amazon", "aliexpress", "noon", "shein", "ebay", "souq", "temu",
                "alibaba", "shop", "store", "talabat", "careem", "uber",
                "hungerstation").any { p.contains(it) } -> AppCategory.SHOPPING

            listOf("bank", "pay", "wallet", "binance", "paypal", "stc", "money",
                "tamara", "tabby", "cash").any { p.contains(it) } -> AppCategory.FINANCE

            listOf("docs", "sheets", "slides", "office", "word", "excel", "notion",
                "evernote", "keep", "calendar", "gmail", "outlook", "mail", "drive",
                "dropbox", "pdf").any { p.contains(it) } -> AppCategory.PRODUCTIVITY

            listOf("settings", "calculator", "clock", "files", "filemanager",
                "weather", "maps", "translate", "chrome", "browser", "firefox",
                "scanner", "flashlight", "compass", "vpn").any { p.contains(it) } -> AppCategory.TOOLS

            else -> null
        }
    }

    companion object {
        private const val ICON_SIZE_PX = 144
    }
}
