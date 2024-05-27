package com.example.weathertodayapp.classes

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.weathertodayapp.DataManager
import com.example.weathertodayapp.R
import com.example.weathertodayapp.apiServices.WeatherDataRepository
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FragmentForecast : Fragment(), WeatherDataRepository.WeatherDataObserver,
    DataManager.UnitObserver {
    private var day1Date: TextView? = null
    private var day1Icon: ImageView? = null
    private var day1Prevision: TextView? = null
    private var day1Description: TextView? = null

    private var day2Date: TextView? = null
    private var day2Icon: ImageView? = null
    private var day2Prevision: TextView? = null
    private var day2Description: TextView? = null

    private var day3Date: TextView? = null
    private var day3Icon: ImageView? = null
    private var day3Prevision: TextView? = null
    private var day3Description: TextView? = null

    private var day4Date: TextView? = null
    private var day4Icon: ImageView? = null
    private var day4Prevision: TextView? = null
    private var day4Description: TextView? = null

    private var day5Date: TextView? = null
    private var day5Icon: ImageView? = null
    private var day5Prevision: TextView? = null
    private var day5Description: TextView? = null

    private var day6Date: TextView? = null
    private var day6Icon: ImageView? = null
    private var day6Prevision: TextView? = null
    private var day6Description: TextView? = null

    private var day7Date: TextView? = null
    private var day7Icon: ImageView? = null
    private var day7Prevision: TextView? = null
    private var day7Description: TextView? = null
    var unit: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forecast, container, false)
        initializeViews(view)
        WeatherDataRepository.registerForecastObserver(this)
        DataManager.registerObserver(this)
        unit = DataManager.unit
        updateForecastViews()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        WeatherDataRepository.unregisterForecastObserver(this)
        DataManager.unregisterObserver(this)
    }

    private fun initializeViews(view: View) {
        day1Date = view.findViewById(R.id.Day1Date)
        day1Icon = view.findViewById(R.id.Day1Icon)
        day1Prevision = view.findViewById(R.id.Day1Prevision)
        day1Description = view.findViewById(R.id.Day1Description)

        day2Date = view.findViewById(R.id.Day2Date)
        day2Icon = view.findViewById(R.id.Day2Icon)
        day2Prevision = view.findViewById(R.id.Day2Prevision)
        day2Description = view.findViewById(R.id.Day2Description)

        day3Date = view.findViewById(R.id.Day3Date)
        day3Icon = view.findViewById(R.id.Day3Icon)
        day3Prevision = view.findViewById(R.id.Day3Prevision)
        day3Description = view.findViewById(R.id.Day3Description)

        day4Date = view.findViewById(R.id.Day4Date)
        day4Icon = view.findViewById(R.id.Day4Icon)
        day4Prevision = view.findViewById(R.id.Day4Prevision)
        day4Description = view.findViewById(R.id.Day4Description)

        day5Date = view.findViewById(R.id.Day5Date)
        day5Icon = view.findViewById(R.id.Day5Icon)
        day5Prevision = view.findViewById(R.id.Day5Prevision)
        day5Description = view.findViewById(R.id.Day5Description)
    }

    private fun getReadableDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(timestamp * 1000L))
    }

    private fun updateForecastViews() {
        val jsonObject = WeatherDataRepository.getForecastDataJson()
        jsonObject?.let { forecastJson ->
            try {
                val list = forecastJson.getJSONArray("list")

                val dailyForecasts = (0 until list.length() step 8).map { index ->
                    list.getJSONObject(index)
                }.take(5)

                dailyForecasts.forEachIndexed { index, dailyForecast ->
                    val dt = dailyForecast.getLong("dt")
                    val weatherArray = dailyForecast.getJSONArray("weather")
                    val weather = weatherArray.getJSONObject(0)
                    val main = dailyForecast.getJSONObject("main")
                    val tempMin = main.getDouble("temp_min")
                    val tempMax = main.getDouble("temp_max")
                    val description = weather.getString("description")
                    val icon = weather.getString("icon")

                    when (index) {
                        0 -> updateDayForecast(
                            day1Date,
                            day1Icon,
                            day1Prevision,
                            day1Description,
                            dt,
                            icon,
                            tempMin,
                            tempMax,
                            description
                        )

                        1 -> updateDayForecast(
                            day2Date,
                            day2Icon,
                            day2Prevision,
                            day2Description,
                            dt,
                            icon,
                            tempMin,
                            tempMax,
                            description
                        )

                        2 -> updateDayForecast(
                            day3Date,
                            day3Icon,
                            day3Prevision,
                            day3Description,
                            dt,
                            icon,
                            tempMin,
                            tempMax,
                            description
                        )

                        3 -> updateDayForecast(
                            day4Date,
                            day4Icon,
                            day4Prevision,
                            day4Description,
                            dt,
                            icon,
                            tempMin,
                            tempMax,
                            description
                        )

                        4 -> updateDayForecast(
                            day5Date,
                            day5Icon,
                            day5Prevision,
                            day5Description,
                            dt,
                            icon,
                            tempMin,
                            tempMax,
                            description
                        )
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } ?: Log.e("FragmentThree", "Forecast data is null")
    }

    @SuppressLint("SetTextI18n")
    fun updateDayForecast(
        dateView: TextView?,
        iconView: ImageView?,
        previsionView: TextView?,
        descriptionView: TextView?,
        dt: Long,
        icon: String,
        tempMin: Double,
        tempMax: Double,
        description: String
    ) {
        dateView?.text = getReadableDate(dt)
        val iconUrl = "https://openweathermap.org/img/wn/$icon@2x.png"
        iconView?.let { imageView ->
            Glide.with(this).load(iconUrl).into(imageView)
        }
        val tempMinConverted = unit?.let { convertTemperature(tempMin, it) }
        val tempMaxConverted = unit?.let { convertTemperature(tempMax, it) }

        previsionView?.text =
            getString(R.string.temp_format, tempMinConverted, tempMaxConverted) + unit
        descriptionView?.text = description
    }

    private fun convertTemperature(tempKelvin: Double, unit: String): Double {
        return when (unit) {
            "°C" -> tempKelvin - 273.15
            "°F" -> (tempKelvin - 273.15) * 9 / 5 + 32
            else -> tempKelvin
        }
    }

    override fun onWeatherDataUpdated() {
        // Not yet implemented
    }

    override fun onForecastDataUpdated() {
        if (isAdded) {
            requireActivity().runOnUiThread {
                updateForecastViews()
            }
        }
    }

    override fun onHourlyDataUpdated() {
        // Not yet implemented
    }
    override fun onUnitChanged(newUnit: String) {
        unit = newUnit
        updateForecastViews()
    }
}