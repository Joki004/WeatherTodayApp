package com.example.weathertodayapp.apiServices

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import org.json.JSONException
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject

class apiServices(private val context: Context) {

    var appKey = "3297082a2158c5da65aaef9d62970ded"
    val apiUrl = "https://api.openweathermap.org/"

    interface ResponseCallback {
        fun onSuccess(result: String)
        fun onFailure(e: IOException?)
    }

    fun getForecastData(lat: Double, lon: Double, unit: String, callback: ResponseCallback) {
        val url = "${apiUrl}data/2.5/forecast?lat=$lat&lon=$lon&appid=$appKey&units=$unit"
        makeRequest(url, callback)
    }

    fun getHourlyData(lat: Double, lon: Double, unit: String, callback: ResponseCallback) {
        val url = "${apiUrl}data/3.0/onecall?lat=$lat&lon=$lon&exclude=minutely,daily,alerts&appid=$appKey&units=$unit"
        makeRequest(url, callback)
    }

    fun getData(userInputs: String, unit: String, callback: ResponseCallback) {
        if (isNetworkAvailable() && isUserInputValid(userInputs)) {
            var url = createURL(userInputs)
            url = url?.let { determineUnit(unit, it) }
            if (url != null) {
                makeRequest(url, callback)
            } else {
                callback.onFailure(IOException("Invalid input or URL could not be created"))
            }
        } else {
            callback.onFailure(IOException("Network is not available"))
        }
    }
    private fun makeRequest(url: String, callback: ResponseCallback) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val apiCallsInstance = this

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        it.body?.string()?.let { responseBody ->
                            try {
                                val jsonObj = JSONObject(responseBody)
                                WeatherDataRepository.setWeatherDataJson(jsonObj)
                                callback.onSuccess(responseBody)
                            } catch (e: JSONException) {
                                Log.e("ApiCalls", "JSON parsing error", e)
                                callback.onFailure(IOException("Failed to parse JSON data"))
                            }
                        } ?: callback.onFailure(IOException("Response body is null"))
                    } else {
                        callback.onFailure(IOException("Unsuccessful response"))
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }
        })
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun createURL(userInputs: String): String? {
        if (userInputs.matches(Regex("^[a-zA-Z]+$"))) {
            return "${apiUrl}data/2.5/weather?q=$userInputs&appid=$appKey"
        }
        val parts = userInputs.split(",")
        return when {
            parts.size == 2 && parts[0].all { it.isDigit() } && parts[1].all { it.isLetter() } -> {
                "${apiUrl}data/2.5/weather?zip=${parts[0]},${parts[1]}&appid=$appKey"
            }
            parts.size == 2 && parts[0].all { it.isLetter() } && parts[1].all { it.isLetter() } -> {
                "${apiUrl}data/2.5/weather?q=${parts[0]},${parts[1]}&appid=$appKey"
            }
            parts.size == 2 && parts[0].all { it.isDigit() } && parts[1].all { it.isDigit() } -> {
                "${apiUrl}data/2.5/weather?lat=${parts[0]}&lon=${parts[1]}&appid=$appKey"
            }
            else -> null
        }
    }

    private fun isUserInputValid(userInputs: String): Boolean {
        if (userInputs.all { it.isLetter() }) return true
        val parts = userInputs.split(",")
        return parts.size == 2 && parts[0].matches(Regex("^[a-zA-Z0-9]+$")) && parts[1].matches(Regex("^[a-zA-Z]+$"))
    }

    fun saveWeatherDataToFile(filename: String, weatherData: String) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(weatherData.toByteArray())
        }
    }

    fun readWeatherDataFromFile(filename: String): String? {
        return try {
            context.openFileInput(filename).use {
                it.bufferedReader().readText()
            }
        } catch (e: IOException) {
            Log.e("ApiCalls", "Error reading from file", e)
            null
        }
    }

    fun deleteWeatherDataForLocation(location: String) {
        val filename = "${location}_weather_data.json"
        val file = File(context.filesDir, filename)
        if (file.exists()) {
            file.delete()
        }
    }

    fun saveFavorites(favorites: List<String>) {
        try {
            val file = File(context.filesDir, "favorites.dat")
            ObjectOutputStream(file.outputStream()).use { it.writeObject(favorites) }
        } catch (e: IOException) {
            Log.e("ApiCalls", "Error saving favorites to file", e)
        }
    }

    fun loadFavorites(): MutableList<String> {
        return try {
            val file = File(context.filesDir, "favorites.dat")
            if (file.exists()) {
                ObjectInputStream(file.inputStream()).use {
                    @Suppress("UNCHECKED_CAST")
                    it.readObject() as MutableList<String>
                }
            } else {
                mutableListOf()
            }
        } catch (e: IOException) {
            Log.e("ApiCalls", "Error loading favorites from file", e)
            mutableListOf()
        }
    }

    private fun determineUnit(unit: String, url: String): String {
        return when (unit) {
            "°C" -> "$url&units=metric"
            "°F" -> "$url&units=imperial"
            else -> "$url&units=standard"
        }
    }
}