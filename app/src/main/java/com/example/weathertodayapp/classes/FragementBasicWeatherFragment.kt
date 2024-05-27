package com.example.weathertodayapp.classes

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlin.math.roundToInt
import com.bumptech.glide.Glide
import com.example.weathertodayapp.R
import com.example.weathertodayapp.apiServices.WeatherDataRepository
import org.json.JSONException

class FragementBasicWeatherFragment : Fragment(), WeatherDataRepository.WeatherDataObserver {
    private var textViewCity: TextView? = null
    private var textViewTemperature: TextView? = null
    private var imageViewWeatherIcon: ImageView? = null
    private var textViewPressure: TextView? = null
    private var textViewDescription: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_basic_weather, container, false)

        textViewCity = view.findViewById(R.id.textViewCity)
        textViewTemperature = view.findViewById(R.id.textViewTemperature)
        imageViewWeatherIcon = view.findViewById(R.id.imageViewWeatherIcon)
        textViewPressure = view.findViewById(R.id.textViewPressure)
        textViewDescription = view.findViewById(R.id.textViewDescription)
        WeatherDataRepository.registerWeatherObserver(this)
        updateDataInFragment()
        return view
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
    override fun onDestroyView() {
        super.onDestroyView()
        WeatherDataRepository.unregisterWeatherObserver(this)
        textViewCity = null
        textViewTemperature = null
        imageViewWeatherIcon = null
        textViewPressure = null
        textViewDescription = null
    }

    override fun onWeatherDataUpdated() {
        if (isAdded) {
            requireActivity().runOnUiThread {
                updateDataInFragment()
            }
        }
    }

    override fun onForecastDataUpdated() {
        // Not yet implemented
    }

    override fun onHourlyDataUpdated() {
        // Not yet implemented
    }

    @SuppressLint("SetTextI18n")
    fun updateDataInFragment() {
        val jsonObj = WeatherDataRepository.getWeatherDataJson()

        jsonObj?.let {
            try {

                if (it.has("sys")) {
                    val sys = it.getJSONObject("sys")
                    textViewCity?.text = "${it.getString("name")}, ${sys.getString("country")}"
                } else {
                    textViewCity?.text = it.getString("name")
                }

                val main = it.getJSONObject("main")
                val prefs = requireActivity().getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
                val unit = prefs.getString("unit", "metric")

                textViewTemperature?.text = "${main.getDouble("temp").roundToInt()} $unit"
                textViewPressure?.text = "Pressure: ${main.getDouble("pressure").roundToInt()} mb"

                val weatherArray = it.getJSONArray("weather")
                if (weatherArray.length() > 0) {
                    val weatherObject = weatherArray.getJSONObject(0)
                    val icon = weatherObject.getString("icon")
                    val iconUrl = "https://openweathermap.org/img/wn/$icon.png"
                    textViewDescription?.text = weatherObject.getString("description")
                    imageViewWeatherIcon?.let { it1 ->
                        Glide.with(this)
                            .load(iconUrl)
                            .into(it1)
                    }
                } else {

                }
            } catch (e: JSONException) {
                Log.e("BasicWeatherFragment", "JSON parsing error: ${e.message}")
            }
        }
    }

    companion object {
        fun newInstance(): FragementBasicWeatherFragment {
            return FragementBasicWeatherFragment()
        }
    }
}
