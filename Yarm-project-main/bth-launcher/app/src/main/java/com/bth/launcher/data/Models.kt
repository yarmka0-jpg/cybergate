package com.bth.launcher.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

enum class AppCategory(val labelAr: String, val labelEn: String) {
    COMMUNICATION("تواصل", "Communication"),
    SOCIAL("اجتماعي", "Social"),
    MEDIA("وسائط", "Media"),
    GAMES("ألعاب", "Games"),
    TOOLS("أدوات", "Tools"),
    PRODUCTIVITY("إنتاجية", "Productivity"),
    SHOPPING("تسوق", "Shopping"),
    FINANCE("مالية", "Finance"),
    OTHER("أخرى", "Other")
}

/**
 * App model. The icon is pre-rendered ONCE to an ImageBitmap on a background
 * thread inside AppRepository, so composition never touches Drawable/Bitmap
 * conversion (this was the cause of the freeze when switching categories).
 */
@Immutable
data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: ImageBitmap,
    val category: AppCategory = AppCategory.OTHER,
    val isHidden: Boolean = false,
    val isLocked: Boolean = false,
    val launchCount: Int = 0
)

enum class LauncherTheme(val labelAr: String) {
    GLASS("زجاجي"),
    MIDNIGHT("ليلي"),
    DARK("داكن"),
    AMOLED("أسود نقي"),
    LIGHT("فاتح")
}

data class WeatherInfo(
    val temperature: Double,
    val weatherCode: Int,
    val isDay: Boolean
) {
    val description: String
        get() = when (weatherCode) {
            0 -> "صافي"
            1, 2 -> "غائم جزئياً"
            3 -> "غائم"
            45, 48 -> "ضباب"
            in 51..57 -> "رذاذ"
            in 61..67 -> "مطر"
            in 71..77 -> "ثلج"
            in 80..82 -> "زخات مطر"
            in 95..99 -> "عاصفة رعدية"
            else -> "غير معروف"
        }
}
