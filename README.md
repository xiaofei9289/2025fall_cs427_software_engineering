# 2025 Fall CS427 Software Engineering

A personal Android weather app. After registering and signing in locally, a user keeps their own city list, checks live weather and a city map, and uses a large language model for interface themes, city scenes, and weather Q&A.

Author: Xiaofei Feng ([xiaofei9289](https://github.com/xiaofei9289))

## Features

- **Accounts.** Local sign-up, sign-in, and sign-out. Passwords are stored as BCrypt hashes, never in plain text. Usernames are case-insensitive, and the session survives an app restart.
- **Cities.** Each user has a separate city list. Adding a city resolves latitude and longitude through OpenWeatherMap Geocoding, rejects duplicates, and supports removal.
- **Weather details.** Shows temperature in Fahrenheit, conditions, humidity, wind speed, and the local time in that city's timezone. Weather is read from a local cache and refreshed from OpenWeatherMap after 30 minutes.
- **Map.** Shows the saved city on Google Maps with a marker. The user can zoom and pan.
- **Themes.** Includes preset themes such as Light, Dark, and Blue. Gemini can also generate a color scheme from a description and apply it immediately. Theme preference is stored per user.
- **Weather-aware city scene.** Asks Gemini for a city landscape image based on the city name, current weather, and time of day.
- **Weather Q&A.** Generates at least two questions from the current weather. Tapping a question requests the matching answer from Gemini. Questions and answers are not hardcoded.

Screen flow:

```text
Sign in / Sign up
    -> City list
        -> City details (weather, scene image)
            -> Map
            -> Weather Q&A
        -> Theme settings
        -> Sign out
```

## Architecture

One Activity hosts the screens as Fragments. Business logic follows MVVM and the Repository pattern.

```text
MainActivity
  -> Fragment (UI)
    -> ViewModel (UI state and actions; can be shared across Fragments)
      -> Repository (local database and network)
        -> Room DAO
        -> Retrofit (OpenWeatherMap, Gemini)
```

`MainActivity` is the only host Activity. `CityListViewModel` is scoped to that Activity, so the city list and the details screen share one data source. ViewModels survive rotation. Database and network work run on background threads, and the UI updates through LiveData.

The local Room database `weather_app_database` (schema version 2) has four tables:

| Entity | Role |
| --- | --- |
| `User` | Username, password hash, theme preference, creation time |
| `City` | City name and coordinates, tied to a user |
| `WeatherData` | Latest weather for a city, with a timestamp for the 30-minute cache |
| `Theme` | Preset themes and LLM-generated themes |

## Stack

- Language: Java 8
- Android: `minSdk 29`, `compileSdk` / `targetSdk 34`
- Build: Gradle 7.3.3, Android Gradle Plugin 7.2.1, View Binding
- UI: AppCompat, Material, ConstraintLayout, Navigation, RecyclerView
- Architecture: AndroidX ViewModel, LiveData
- Persistence: Room 2.4.3
- Network: Retrofit 2.9.0, OkHttp, Gson
- Maps: Google Play Services Maps 18.1.0, Location 21.0.1
- Passwords: jBCrypt
- External services: OpenWeatherMap Current Weather and Geocoding; Google Gemini for themes, weather Q&A, and image generation

The app does not request the user's location. The map only shows coordinates of cities the user saved.

## Layout

```text
app/src/main/java/edu/uiuc/cs427app/
  MainActivity.java              Host Activity and screen navigation
  ui/fragment/                   Sign-in, city list, details, map, theme, weather Q&A
  ui/adapter/                    City list adapter
  viewmodel/                     Login, CityList, Weather, Theme
  repository/                    User, City, Weather
  data/                          Room database, entities, DAOs
  network/                       Retrofit clients and response models
  helper/                        Weather requests, LLM, theme generation, city images
app/src/test/                    JVM unit tests
app/src/androidTest/             Instrumented tests (emulator or device)
docs/                            Class diagram notes
```

## Setup

- JDK 17
- Android SDK 34 and Android SDK Build-Tools
- An API 34 emulator or a physical device. Instrumented tests were verified on a Pixel AVD (`google_apis` / `arm64-v8a` / Android 14)

`local.properties` is listed in `.gitignore` and must not be committed. Create it in the project root:

```properties
sdk.dir=/Users/your-name/Library/Android/sdk
WEATHER_API_KEY=your_openweathermap_key
GEMINI_API_KEY=your_gemini_key
MAPS_API_KEY=your_google_maps_key
```

The three keys are used for weather and geocoding, Gemini text and images, and maps. Without a key, that feature fails at runtime. Sign-in, local city-list logic, and most database tests still run.

## Build and run

From the project root:

```bash
./gradlew :app:assembleDebug
```

Open the project in Android Studio, select a running emulator, and run `app`. To install on a connected device from the command line:

```bash
./gradlew :app:installDebug
```

## Tests

Unit tests run on the local JVM and do not need a device:

```bash
./gradlew :app:testDebugUnitTest
```

Instrumented tests need a running emulator or a connected device (`adb devices`):

```bash
./gradlew :app:connectedDebugAndroidTest
```

To run a single instrumented test class:

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=edu.uiuc.cs427app.data.dao.UserDaoTest
```

Coverage:

| Type | Location | What it covers |
| --- | --- | --- |
| Unit tests | `app/src/test` | User entity, password hashing, sign-in validation (`AuthenticationTest`) |
| Instrumented tests | `app/src/androidTest` | User, City, and Weather DAOs; sign-up and sign-in; sign-out; add and remove cities; weather display; map; mocked location; weather Q&A |

One full run on the API 34 Pixel emulator: 19 unit tests passed, 47 instrumented tests passed.

Reports:

- `app/build/reports/tests/testDebugUnitTest/index.html`
- `app/build/reports/androidTests/connected/index.html`
