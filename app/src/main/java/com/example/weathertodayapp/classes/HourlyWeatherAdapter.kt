package com.example.weathertodayapp.classes

import com.example.weathertodayapp.DataManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weathertodayapp.R

data class HourlyWeather(
    val hour: String,
    val iconUrl: String,
    val temperature: Double,
    val humidity: Int
)

class HourlyWeatherAdapter(private val hourlyWeatherList: List<HourlyWeather>) :
    RecyclerView.Adapter<HourlyWeatherAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hourTextView: TextView = view.findViewById(R.id.hourTextView)
        val iconImageView: ImageView = view.findViewById(R.id.iconImageView)
        val temperatureTextView: TextView = view.findViewById(R.id.temperatureTextView)
        val humidityTextView: TextView = view.findViewById(R.id.humidityTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_weather, parent, false)
        return ViewHolder(view)
    }
    private fun kelvinToCelsius(kelvin: Double): Double = kelvin - 273.15

    private fun kelvinToFahrenheit(kelvin: Double): Double = (kelvin - 273.15) * 9/5 + 32

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val unit = DataManager.unit


        val hourlyWeather = hourlyWeatherList[position]
        val temperatureText = when (unit) {
            "°C" -> String.format("%.1f°C", kelvinToCelsius(hourlyWeather.temperature))
            "K" -> String.format("%.1fK", hourlyWeather.temperature)
            "°F" -> String.format("%.1f°F", kelvinToFahrenheit(hourlyWeather.temperature))
            else -> "${hourlyWeather.temperature}K"
        }
        holder.hourTextView.text = hourlyWeather.hour
        Glide.with(holder.iconImageView.context)
            .load(hourlyWeather.iconUrl)
            .into(holder.iconImageView)
        holder.temperatureTextView.text = temperatureText
        holder.humidityTextView.text = "Humidity: ${hourlyWeather.humidity}%"
    }

    override fun getItemCount() = hourlyWeatherList.size
}
