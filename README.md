# SkyCast 🌤️

**See the Sky Differently.**

SkyCast is an Android weather application that displays current weather conditions and a five-day forecast for searched cities or the user's current location. It combines animated weather visuals with a custom dark/light interface and locally cached weather data for offline access.

> **Project:** Android application  
> **Package:** `com.samarthshukla.skycast`  
> **Language:** Java  
> **UI:** XML layouts

---

## Features

### Current weather
- Displays current temperature, weather description, daily high and low, feels-like temperature, humidity, wind speed, and atmospheric pressure.
- Uses metric units in the current implementation.

### City search and autocomplete
- Search for cities by name.
- Displays geocoding suggestions while typing.
- Fetches weather using the selected city's latitude and longitude.

### Current location
- Uses Google's Fused Location Provider to retrieve the device location.
- Requests location permission at runtime.
- Supports approximate and precise location permission.
- Falls back to city search when location permission is denied.

### Five-day forecast
- Retrieves OpenWeatherMap's five-day / three-hour forecast data.
- Groups forecast entries by local date to display daily minimum and maximum temperatures.
- Displays a representative daily weather condition and precipitation probability.

### Animated weather visuals
- Uses Lottie animations for clear sky, clouds, rain, thunderstorms, and snow.
- Changes the main weather animation according to the returned weather icon code.
- Displays a radial colour glow behind the main animation, with its colour selected according to the weather condition and day/night icon variant.

### Dark and light themes
- Provides a custom animated sliding sun/moon switch.
- Saves the selected theme using `SharedPreferences`.
- Uses separate colour resources for light mode (`values`) and dark mode (`values-night`).
- Restores the selected theme when the app is reopened.

### Offline weather cache
- Caches current weather and forecast responses locally using `SharedPreferences` and Gson.
- Maintains separate cache entries for cities and device locations.
- Displays saved data when available, then attempts to refresh it from the API.
- Stores the last known device coordinates to support restoring the last location.
- Displays the last-updated timestamp.

---

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 11 |
| Android UI | XML layouts, AndroidX AppCompat, Material Components |
| Weather data | [OpenWeatherMap API](https://openweathermap.org/api) |
| Networking | Retrofit with Gson converter |
| Location | Google Play services — Fused Location Provider |
| Weather animations | [Lottie for Android](https://github.com/airbnb/lottie-android) |
| Local cache and preferences | SharedPreferences + Gson |
| Lists | RecyclerView |
| Build system | Gradle |

---

## Screens and UI

The main screen contains:

1. SkyCast header with the theme switch.
2. City search, autocomplete suggestions, and current-location control.
3. Current weather animation and temperature summary.
4. Weather details: feels-like temperature, humidity, wind, and pressure.
5. Horizontally scrolling five-day forecast.

The interface uses a dark navy/cyan palette and a light neutral/blue palette. Shared drawable resources provide consistent rounded cards and borders across both themes.

---

## Getting started

### Requirements

- Android Studio
- Android SDK with API level 36 installed
- A device or emulator running Android 7.0 (API 24) or newer
- An OpenWeatherMap API key

### 1. Clone the repository

```bash
git clone https://github.com/samarthshukla20/SkyCast.git
cd SkyCast
```

Open the project in Android Studio and allow Gradle to sync.

### 2. Configure the OpenWeatherMap API key

The app reads the API key from the root project's `local.properties` file and exposes it to the app as `BuildConfig.WEATHER_API_KEY`.

Add this line to the root `local.properties` file:

```properties
WEATHER_API_KEY=YOUR_OPENWEATHERMAP_API_KEY
```

Replace the placeholder with your own API key. Do not commit your real API key or share it in screenshots, logs, or public files. Keep `local.properties` out of version control.

### 3. Build and run

In Android Studio:

1. Sync the project with Gradle files.
2. Select the `app` run configuration.
3. Choose an Android device or emulator (API 24 or newer).
4. Click **Run**.

You can also build a debug APK from the project root:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is generated under:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 4. Grant location permission

On first launch, allow SkyCast to access device location to load local weather. If permission is denied, use city search instead. The app supports approximate or precise location permission.

---

## API usage

SkyCast uses the OpenWeatherMap endpoints below:

| Purpose | Endpoint |
|---|---|
| Current weather by city | `data/2.5/weather` |
| Current weather by coordinates | `data/2.5/weather` |
| Five-day forecast by city | `data/2.5/forecast` |
| Five-day forecast by coordinates | `data/2.5/forecast` |
| City geocoding / autocomplete | `geo/1.0/direct` |

Weather requests use `units=metric`. API responses are parsed into the application's weather and forecast model classes.

---

## Project structure

```text
SkyCast/
├── app/
│   └── src/main/
│       ├── assets/                 # Lottie weather animation JSON files
│       ├── java/com/samarthshukla/skycast/
│       │   ├── MainActivity.java  # Main screen, location, theme and weather flow
│       │   ├── WeatherCache.java  # Local weather and forecast cache
│       │   ├── data/api/          # Retrofit client and API service
│       │   ├── model/             # Weather, geocoding and forecast models
│       │   └── ui/forecast/       # Forecast and city suggestion adapters
│       └── res/
│           ├── drawable/          # Backgrounds and UI drawables
│           ├── layout/            # Main screen and RecyclerView item layouts
│           ├── values/            # Light theme colours and resources
│           └── values-night/      # Dark theme colours and resources
├── gradle/
│   └── libs.versions.toml         # Version catalog
├── build.gradle
├── settings.gradle
└── local.properties               # Local API key; do not commit
```

---

## Permissions

SkyCast requests these Android permissions:

- `INTERNET` — to retrieve weather and geocoding data.
- `ACCESS_FINE_LOCATION` — to request precise device location.
- `ACCESS_COARSE_LOCATION` — to support approximate device location.

---

## Notes and limitations

- Weather data depends on OpenWeatherMap availability, API limits, and the configured API key.
- The five-day forecast is built from the API's three-hour forecast entries; this project does not currently provide a separate 24-hour hourly forecast screen.
- Offline mode displays the last successfully cached data. It cannot provide new live weather information without a network connection.
- The application does not currently include severe-weather push alerts or a saved-favourites screen.

---

## Contributing

Suggestions, bug reports, and improvements are welcome. For a code change, please consider opening an issue first to describe the proposed change, then submit a pull request with a clear summary and testing notes.

---

## Repository

[github.com/samarthshukla20/SkyCast](https://github.com/samarthshukla20/SkyCast)

---

**SkyCast — See the Sky Differently.**
