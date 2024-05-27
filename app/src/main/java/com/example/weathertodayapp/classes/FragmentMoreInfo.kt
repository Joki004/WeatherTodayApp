package com.example.weathertodayapp.classes

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weathertodayapp.R
import com.example.weathertodayapp.apiServices.WeatherDataRepository
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentMoreInfo : Fragment(), WeatherDataRepository.WeatherDataObserver {
    private var windForce: TextView? = null
    private var windDirection: TextView? = null
    private var humidity: TextView? = null
    private var visibility: TextView? = null
    private lateinit var hourlyWeatherRecyclerView: RecyclerView
    private var lastUpdateTimestamp: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more_info, container, false)
        initializeViews(view)
        WeatherDataRepository.registerWeatherObserver(this)
        WeatherDataRepository.registerHourlyObserver(this)
        updateUIWithWeatherData()
        updateHourlyWeatherData()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        WeatherDataRepository.unregisterWeatherObserver(this)
        WeatherDataRepository.unregisterHourlyObserver(this)
    }

    private fun initializeViews(view: View) {
        windForce = view.findViewById(R.id.WindForce)
        windDirection = view.findViewById(R.id.WindDirection)
        humidity = view.findViewById(R.id.Humidity)
        visibility = view.findViewById(R.id.visibility)
        hourlyWeatherRecyclerView = view.findViewById(R.id.hourlyWeatherRecyclerView)
        hourlyWeatherRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Log to ensure views are initialized
        Log.d("FragmentMoreInfo", "Views initialized: windForce=$windForce, windDirection=$windDirection, humidity=$humidity, visibility=$visibility")
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("WindForce", windForce?.text.toString())
        outState.putString("WindDirection", windDirection?.text.toString())
        outState.putString("Humidity", humidity?.text.toString())
        outState.putString("Visibility", visibility?.text.toString())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            windForce?.text = it.getString("WindForce")
            windDirection?.text = it.getString("WindDirection")
            humidity?.text = it.getString("Humidity")
            visibility?.text = it.getString("Visibility")
        }
    }

    override fun onWeatherDataUpdated() {
        if (isAdded) {
            requireActivity().runOnUiThread {
                updateUIWithWeatherData()
            }
        }
    }

    override fun onHourlyDataUpdated() {
        if (isAdded) {
            requireActivity().runOnUiThread {
                updateHourlyWeatherData()
            }
        }
    }

    override fun onForecastDataUpdated() {
        // Implement this method if needed
    }

    @SuppressLint("SetTextI18n")
    private fun updateUIWithWeatherData() {
        val jsonObj = WeatherDataRepository.getWeatherDataJson()
        if (jsonObj != null) {
            try {
                Log.d("FragmentMoreInfo", "Full JSON: $jsonObj")

                // Access the 'list' array
                val listArray = jsonObj.optJSONArray("list")
                if (listArray != null && listArray.length() > 0) {
                    // Get the first object in the list array
                    val firstEntry = listArray.getJSONObject(0)

                    // Now access 'wind', 'main', and 'visibility' from the first object in the list array
                    val wind = firstEntry.optJSONObject("wind")
                    val main = firstEntry.optJSONObject("main")
                    val visibilityValue = firstEntry.optInt("visibility", -1)

                    Log.d("FragmentMoreInfo", "Parsed JSON - Wind: $wind, Main: $main, Visibility: $visibilityValue")

                    if (wind != null && main != null) {
                        val windSpeed = wind.optDouble("speed", 0.0)
                        val windDeg = wind.optInt("deg", 0)
                        val humidityValue = main.optInt("humidity", 0)

                        Log.d("FragmentMoreInfo", "Wind Speed: $windSpeed, Wind Direction: $windDeg, Humidity: $humidityValue, Visibility: $visibilityValue")

                        windForce?.text = "Wind Speed: $windSpeed m/s"
                        windDirection?.text = "Wind Direction: $windDeg°"
                        humidity?.text = "Humidity: $humidityValue%"
                        visibility?.text = "Visibility: ${if (visibilityValue != -1) visibilityValue / 1000.0 else 0.0} km"
                    } else {
                        Log.e("FragmentMoreInfo", "Wind or Main JSON object is null")
                    }
                } else {
                    Log.e("FragmentMoreInfo", "List array is null or empty")
                }
            } catch (e: Exception) {
                Log.e("FragmentMoreInfo", "Exception in if block: ${e.message}", e)
            }
        } else {
            Log.e("FragmentMoreInfo", "Weather data JSON is null")
        }
    }


    fun updateHourlyWeatherData() {
        val jsonObjHours = WeatherDataRepository.getHourlyDataJson()
        jsonObjHours?.let {
            Log.d("FragmentMoreInfo", "Hourly data is available")
            updateHourlyWeatherRecyclerView(it)
        } ?: run {
            Log.d("FragmentMoreInfo", "Hourly data is NOT available")
        }
    }

    private fun updateHourlyWeatherRecyclerView(json: JSONObject) {
        val hourlyWeatherList = parseHourlyWeatherData(json)
        val adapter = HourlyWeatherAdapter(hourlyWeatherList)
        hourlyWeatherRecyclerView.adapter = adapter
    }

    private fun parseHourlyWeatherData(json: JSONObject): List<HourlyWeather> {
        val hourlyArray = json.getJSONArray("hourly")
        return (0 until hourlyArray.length()).map { index ->
            val hourlyJson = hourlyArray.getJSONObject(index)
            val hour = getReadableHour(hourlyJson.getLong("dt"))
            val iconCode = hourlyJson.getJSONArray("weather").getJSONObject(0).getString("icon")
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
            val temperature = hourlyJson.getDouble("temp")
            val humidity = hourlyJson.getInt("humidity")
            HourlyWeather(hour, iconUrl, temperature, humidity)
        }
    }

    private fun getReadableHour(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("ha", Locale.getDefault())
        return format.format(date)
    }

    private fun updateUI(forecastData: JSONObject?) {
        // Implement your UI update logic here if you need to handle forecast data
    }
}
