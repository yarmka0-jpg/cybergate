package com.bth.launcher.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fetches current weather from the free Open-Meteo API (no API key required).
 * Falls back to Riyadh coordinates when location permission is unavailable.
 */
class WeatherRepository(private val context: Context) {

    suspend fun fetchWeather(): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val (lat, lon) = getLocation()
            val url = URL(
                "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,weather_code,is_day"
            )
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(response).getJSONObject("current")

            WeatherInfo(
                temperature = current.getDouble("temperature_2m"),
                weatherCode = current.getInt("weather_code"),
                isDay = current.getInt("is_day") == 1
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getLocation(): Pair<Double, Double> {
        val fallback = 24.7136 to 46.6753 // Riyadh

        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return fallback

        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            location?.let { it.latitude to it.longitude } ?: fallback
        } catch (e: SecurityException) {
            fallback
        }
    }
}
