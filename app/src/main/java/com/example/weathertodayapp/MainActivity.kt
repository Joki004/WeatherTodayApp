package com.example.weathertodayapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weathertodayapp.apiServices.WeatherDataRepository
import com.example.weathertodayapp.apiServices.apiServices
import com.google.android.material.navigation.NavigationView
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.Locale
import com.example.weathertodayapp.classes.FavoritesAdapter
import com.example.weathertodayapp.classes.FragementBasicWeatherFragment
import com.example.weathertodayapp.classes.FragmentForecast
import com.example.weathertodayapp.classes.FragmentMoreInfo

class MainActivity : AppCompatActivity() {

    private lateinit var mainLayout: ConstraintLayout
    private lateinit var navigationContainer: FrameLayout
    private lateinit var dimBackground: View
    private lateinit var navigationView: NavigationView
    private lateinit var menuLeft: Button
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private val apiCalls = apiServices(this)
    private val favorites = mutableListOf<String>()
    private val maxFavoriteList = 5
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval: Long = 10800000
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (isInternetAvailable()) {
                refreshAllFavoriteCities()
            }
            handler.postDelayed(this, refreshInterval)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initializeViews()
        initializeSpinner()
        loadFavorites()
        setupListeners()

        if (savedInstanceState == null) {
            loadInitialFragments()
        }


        handler.post(refreshRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun initializeViews() {
        mainLayout = findViewById(R.id.main)
        navigationContainer = findViewById(R.id.navigation_container)
        dimBackground = findViewById(R.id.dim_background)
        navigationView = findViewById(R.id.navigation_view)
        menuLeft = findViewById(R.id.MenuLeft)

        val headerView = navigationView.getHeaderView(0)
        favoritesRecyclerView = headerView.findViewById(R.id.favourites_list)

        favoritesAdapter = FavoritesAdapter(this, favorites, ::loadFavorite, ::removeFavorite)
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesRecyclerView.adapter = favoritesAdapter

        navigationContainer.visibility = View.GONE
        dimBackground.visibility = View.GONE
    }

    private fun setupListeners() {
        if (!isTablet()) {
            findViewById<Button>(R.id.btnNext).setOnClickListener {
                Log.d("FragmentMoreInfo", "Button More info CLicked")
                loadFragment(FragmentMoreInfo(), R.id.fragmentContainer2)
            }

            findViewById<Button>(R.id.btnPrevious).setOnClickListener {
                loadFragment(FragmentForecast(), R.id.fragmentContainer2)
            }
        }

        findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val spinner: Spinner = findViewById(R.id.spinner)
            DataManager.editTextData = findViewById<EditText>(R.id.searchBar).text.toString()
            DataManager.unit = spinner.selectedItem.toString()
            Log.d(
                "MainActivity",
                "Confirm button clicked. Location: ${DataManager.editTextData}, Unit: ${DataManager.unit}"
            )
            executeApiCall(DataManager.editTextData)
        }

        findViewById<TextView>(R.id.FavoriteIcon).setOnClickListener {
            addCurrentLocationToFavorites()
        }

        menuLeft.setOnClickListener {
            toggleNavigationView()
        }

        dimBackground.setOnClickListener {
            hideNavigationView()
        }

        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.CloseNavBar -> hideNavigationView()
            }
            true
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    private fun toggleNavigationView() {
        if (navigationContainer.visibility == View.GONE) showNavigationView() else hideNavigationView()
    }

    private fun showNavigationView() {
        navigationContainer.visibility = View.VISIBLE
        dimBackground.visibility = View.VISIBLE
    }

    private fun hideNavigationView() {
        navigationContainer.visibility = View.GONE
        dimBackground.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addCurrentLocationToFavorites() {
        val location = findViewById<EditText>(R.id.searchBar).text.toString()
        if (location.isNotEmpty() && !favorites.contains(location)) {
            if (favorites.size >= maxFavoriteList) {
                Toast.makeText(
                    this,
                    "Maximum of $maxFavoriteList favorites allowed",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            favorites.add(location)
            favoritesAdapter.notifyDataSetChanged()
            apiCalls.saveFavorites(favorites)
            saveWeatherDataForLocation(location)
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Location is empty or already in favorites", Toast.LENGTH_SHORT)
                .show()
        }
        updateFavoriteIcon()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun removeFavorite(position: Int) {
        val removedLocation = favorites.removeAt(position)
        favoritesAdapter.notifyDataSetChanged()
        apiCalls.saveFavorites(favorites)
        apiCalls.deleteWeatherDataForLocation(removedLocation)
        Toast.makeText(this, "Removed $removedLocation from favorites", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadFavorites() {
        val loadedFavorites = apiCalls.loadFavorites()
        favorites.clear()
        favorites.addAll(loadedFavorites)
        favoritesAdapter.notifyDataSetChanged()
    }

    private fun saveWeatherDataForLocation(location: String) {
        apiCalls.getData(location, DataManager.unit, object : apiServices.ResponseCallback {
            override fun onSuccess(result: String) {
                apiCalls.saveWeatherDataToFile("${location}_weather_data.json", result)
            }

            override fun onFailure(e: IOException?) {
                Log.e("MainActivity", "Failed to get weather data for $location", e)
            }
        })
    }

    private fun loadFavorite(location: String) {
        if (isInternetAvailable()) {
            findViewById<EditText>(R.id.searchBar).setText(location)
            executeApiCall(location)
        } else {
            // Load data from file
            val fileName = "${location}_weather_data.json"
            val file = File(filesDir, fileName)
            if (file.exists()) {
                val fileContent = file.readText()
                val jsonObj = JSONObject(fileContent)
                WeatherDataRepository.setWeatherDataJson(jsonObj)
                setBackground()
                (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? FragementBasicWeatherFragment)?.updateDataInFragment()
            } else {
                Toast.makeText(this, "No offline data available for $location", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        updateFavoriteIcon()
    }

    private fun executeApiCall(userInputs: String) {
        val spinner: Spinner = findViewById(R.id.spinner)
        val currentUnit = spinner.selectedItem.toString()
        DataManager.unit = currentUnit
        apiCalls.getData(userInputs, currentUnit, object : apiServices.ResponseCallback {
            override fun onSuccess(result: String) {
                Log.d("ApiCalls", "Success: $result")
                WeatherDataRepository.setWeatherDataJson(JSONObject(result))
                setBackground()

                val jsonObj = WeatherDataRepository.getWeatherDataJson()
                jsonObj?.let {
                    try {
                        val coord = it.getJSONObject("coord")
                        val lat = coord.getDouble("lat")
                        val lon = coord.getDouble("lon")
                        apiCalls.getForecastData(
                            lat,
                            lon,
                            currentUnit,
                            object : apiServices.ResponseCallback {
                                override fun onSuccess(result: String) {
                                    Log.d("ApiCalls2", "Success: $result")
                                    WeatherDataRepository.setForecastDataJson(JSONObject(result))
                                }

                                override fun onFailure(e: IOException?) {
                                    Log.e("ApiCalls2", "Failed to get forecast data", e)
                                }
                            })

                        apiCalls.getHourlyData(
                            lat,
                            lon,
                            currentUnit,
                            object : apiServices.ResponseCallback {
                                override fun onSuccess(result: String) {
                                    Log.d("ApiCalls2", "Success hourly: $result")
                                    WeatherDataRepository.setHourlyDataJson(JSONObject(result))
                                    updateFavoriteIconOnConfirm(userInputs)
                                }

                                override fun onFailure(e: IOException?) {
                                    Log.e("ApiCalls2", "Failed to get hourly data", e)
                                }
                            })
                    } catch (e: JSONException) {
                        Log.e(
                            "MainActivity",
                            "JSON parsing error during coordinate extraction: ${e.message}"
                        )
                    }
                } ?: run {
                    Log.e("MainActivity", "JSON object from WeatherDataRepository is null")
                }
            }

            override fun onFailure(e: IOException?) {
                Log.e("ApiCalls", "Failed to get data", e)
            }
        })
    }

    private fun initializeSpinner() {
        val spinner: Spinner = findViewById(R.id.spinner)
        val options = arrayOf("°C", "K", "°F")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE)
        val savedUnit = prefs.getString("unit", "°C")
        val position = adapter.getPosition(savedUnit)
        spinner.setSelection(position)

        DataManager.unit = savedUnit ?: "°C"

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem: String = parent.getItemAtPosition(position) as String
                prefs.edit().putString("unit", selectedItem).apply()
                DataManager.unit= selectedItem
            }


            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setBackground() {
        val jsonObj = WeatherDataRepository.getWeatherDataJson()
        if (jsonObj == null) {
            Log.e("MainActivity", "JSON object is null")
            return
        }
        jsonObj.let {
            try {
                Log.d("MainActivity", "JSON Object: $it")
                if (!it.has("weather")) {
                    Log.e("MainActivity", "No 'weather' key in JSON response")
                    return
                }
                val weatherArray = it.getJSONArray("weather")
                if (weatherArray.length() > 0) {
                    val weatherObject = weatherArray.getJSONObject(0)
                    val description = weatherObject.getString("description")
                    val colorRes = when (description.lowercase(Locale.ROOT)) {
                        "clear sky" -> R.color.clear_sky_day
                        "few clouds" -> R.color.clouds_few
                        "scattered clouds" -> R.color.clouds_scattered
                        "broken clouds" -> R.color.clouds_broken
                        "shower rain" -> R.color.rain_heavy
                        "rain" -> R.color.rain_moderate
                        "thunderstorm" -> R.color.thunderstorm
                        "snow" -> R.color.snow_light
                        "mist" -> R.color.mist_fog
                        else -> R.color.background_default
                    }
                    Log.d("MainActivity", "Setting background for description: $description")

                    runOnUiThread {
                        val color = ContextCompat.getColor(this@MainActivity, colorRes)
                        mainLayout.setBackgroundColor(color)
                        menuLeft.setBackgroundColor(color)
                        Log.d("MainActivity", "Background color set")
                    }
                } else {
                    Log.d("MainActivity", "No weather data available")
                }
            } catch (e: JSONException) {
                Log.e("MainActivity", "JSON parsing error: ${e.message}")
            }
        }
    }

    private fun loadInitialFragments() {
        loadFragment(FragementBasicWeatherFragment.newInstance(), R.id.fragmentContainer)
        loadFragment(FragmentMoreInfo(), R.id.fragmentContainer2)
        if (isTablet()) {
            loadFragment(FragmentForecast(), R.id.fragmentContainer3)
        }
    }

    private fun loadFragment(fragment: Fragment, containerId: Int) {
        supportFragmentManager.beginTransaction().replace(containerId, fragment).commit()
    }

    private fun isTablet(): Boolean {
        val screenLayout = resources.configuration.screenLayout
        val screenSize = screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        return screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_main)
        loadInitialFragments()
        setupListeners()
        if (isTablet()) {
            Log.d("MainActivity", "Tablet mode")
        } else {
            Log.d("MainActivity", "Phone mode")

        }
    }

    private fun refreshAllFavoriteCities() {
        for (location in favorites) {
            executeApiCall(location)
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun updateFavoriteIcon() {
        val currentLocation = findViewById<EditText>(R.id.searchBar).text.toString()
        val favoriteIcon = findViewById<TextView>(R.id.FavoriteIcon)
        val drawableRes = if (favorites.contains(currentLocation)) {
            R.drawable.bxs_star_filled
        } else {
            R.drawable.bx_star
        }
        favoriteIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
    }

    private fun updateFavoriteIconOnConfirm(location: String) {

        runOnUiThread {
            val favoriteIcon = findViewById<TextView>(R.id.FavoriteIcon)
            val drawableRes = if (favorites.contains(location)) {
                R.drawable.bxs_star_filled
            } else {
                R.drawable.bx_star
            }
            favoriteIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
        }
    }

}
