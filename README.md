# Weather Today App

## Overview
The Weather Today App is designed to provide users with current and forecasted weather information for selected locations. It features a user-friendly interface based on fragments, which adapts to different screen sizes and orientations. The app fetches weather data from the OpenWeatherMap API and supports offline access by storing data locally.

## Features
- **Current Weather Information**: Displays basic weather data such as city name, geographic coordinates, time, temperature, pressure, description, and graphical representation.
- **Additional Weather Data**: Provides details about wind speed and direction, humidity, visibility, and more.
- **Weather Forecast**: Shows a 5-day weather forecast.
- **Favorite Locations**: Allows users to save and manage a list of favorite locations.
- **Offline Access**: Caches weather data for offline access, ensuring that users can view previously fetched data even without an internet connection.
- **Customizable Units**: Users can select their preferred units of measurement (Celsius, Fahrenheit, or Kelvin).

## Setup Instructions

### Prerequisites
- Android Studio installed on your machine.
- An OpenWeatherMap API key. You can sign up for a free API key at [OpenWeatherMap](https://openweathermap.org/appid).

### API Key Configuration
To keep your API key secure and not expose it on GitHub, follow these steps:

1. **Add your API key to `local.properties`:**
   ```properties
   API_KEY=your_openweathermap_api_key
   ```

2. **Configure `build.gradle.kts` to read the API key:**

   ```kotlin
   import java.util.Properties

   val localProperties = Properties()
   val localPropertiesFile = rootProject.file("local.properties")
   if (localPropertiesFile.exists()) {
       localPropertiesFile.inputStream().use { stream ->
           localProperties.load(stream)
       }
   }

   val apiKey = localProperties.getProperty("API_KEY") ?: ""

   plugins {
       alias(libs.plugins.androidApplication)
       alias(libs.plugins.jetbrainsKotlinAndroid)
   }

   android {
       namespace = "com.example.weathertodayapp"
       compileSdk = 34

       defaultConfig {
           applicationId = "com.example.weathertodayapp"
           minSdk = 24
           targetSdk = 34
           versionCode = 1
           versionName = "1.0"
           
           buildConfigField("String", "API_KEY", "\"$apiKey\"")
           testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }

       buildFeatures {
           buildConfig = true
       }

       buildTypes {
           release {
               isMinifyEnabled = false
               proguardFiles(
                   getDefaultProguardFile("proguard-android-optimize.txt"),
                   "proguard-rules.pro"
               )
           }
       }
       compileOptions {
           sourceCompatibility = JavaVersion.VERSION_1_8
           targetCompatibility = JavaVersion.VERSION_1_8
       }
       kotlinOptions {
           jvmTarget = "1.8"
       }
   }

   dependencies {
       implementation(libs.androidx.core.ktx)
       implementation(libs.androidx.appcompat)
       implementation(libs.material)
       implementation(libs.androidx.activity)
       implementation(libs.androidx.constraintlayout)
       implementation("com.squareup.retrofit2:retrofit:2.9.0")
       implementation("com.squareup.retrofit2:converter-gson:2.9.0")
       implementation("com.squareup.okhttp3:okhttp:4.9.0")
       implementation("androidx.constraintlayout:constraintlayout:2.1.2")
       implementation("com.github.bumptech.glide:glide:4.13.2")
       annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")
       implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
       testImplementation(libs.junit)
       androidTestImplementation(libs.androidx.junit)
       androidTestImplementation(libs.androidx.espresso.core)
   }
   ```

### Building and Running the App
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/weather-today-app.git
   cd weather-today-app
   ```

2. Open the project in Android Studio.

3. Ensure your `local.properties` file contains your OpenWeatherMap API key:
   ```properties
   API_KEY=your_openweathermap_api_key
   ```

4. Build and run the app on your emulator or Android device.

### Main Components

#### MainActivity
The `MainActivity` initializes views, sets up listeners, and handles data fetching and saving. It communicates with fragments and handles user interactions, including setting the unit of measurement.

#### FragmentForecast
The `FragmentForecast` class handles displaying the weather forecast. It listens for changes in weather data and updates the view accordingly.

#### DataManager
The `DataManager` object holds shared data between activities and fragments, such as the current location and selected unit of measurement.

#### API Services
The `apiServices` class handles network requests to fetch weather data from OpenWeatherMap. It includes methods for checking network availability, making requests, and saving/loading data from the device's storage.

### User Interface
The user interface is based on fragments and adapts to different screen orientations and resolutions. It includes:
- A main fragment displaying basic weather information.
- Additional fragments for detailed weather data and forecasts.
- A menu for refreshing data, setting locations, and choosing units of measurement.

## Screenshots
Here are some screenshots of the app in action:

### Portrait Mode
![image](https://github.com/Joki004/WeatherTodayApp/assets/101185519/ee8ee27d-b92d-42a0-a3ec-472a4586b5f3)


### Landscape Mode
![image](https://github.com/Joki004/WeatherTodayApp/assets/101185519/48207677-7c56-491b-8d0d-2eba184b1424)

### Tablet Mode
![image](https://github.com/Joki004/WeatherTodayApp/assets/101185519/bb6dcb31-d25b-49c8-a4e9-a519bb8638b6)


