# Class Diagram Documentation - CS427 Weather App

## Architecture Overview

This app uses **MVVM (Model-View-ViewModel) + Repository pattern** with **Single Activity + Fragments** architecture for clean separation of concerns and lifecycle safety.

**Total Classes: 26** (MainActivity, 5 Fragments, 4 ViewModels, 2 Helpers, 3 Repositories, 4 Entities, 3 DAOs, 2 API Clients, 1 Database, 2 Adapters)

### Layer Structure
```
MainActivity (Single Host Activity)
    ↓ hosts
Fragments (UI Screens)
    ↓ observes
ViewModels (Business Logic)
    ↓ requests data
Repositories (Data Coordination)
    ↓ accesses
DAOs (Database) / API Clients (Network) / Helper Classes
```

**Benefits**:
- Single Activity simplifies navigation and data sharing between screens
- Fragments provide modular UI components with independent lifecycles
- Shared ViewModels (scoped to MainActivity) enable seamless data sharing between Fragments
- ViewModel survives rotation without data loss
- Testable business logic (ViewModel has no Android dependencies)
- Flexible data sources (Repository abstracts DB vs API)

---

## Android Component Types

### Activity
**What**: Main entry point and container for the app. Hosts all Fragments.
**Example**: MainActivity (single host Activity)

### Fragment
**What**: Modular UI component representing a screen. Hosted by MainActivity.
**Lifecycle**: onCreateView → onViewCreated → onDestroyView
**Examples**: LoginFragment, CityListFragment, DetailsFragment, MapFragment, ThemeSettingsFragment

### Entity (Room)
**What**: Data class representing database table.
**Examples**: User, City, WeatherData, Theme

### DAO (Data Access Object)
**What**: Interface for database operations. Room generates implementation.
**Examples**: UserDao, CityDao, WeatherDao

### ViewModel
**What**: Holds UI data, survives rotation. Can be shared across Fragments.
**Examples**: LoginViewModel, CityListViewModel, WeatherViewModel, ThemeViewModel

### Repository
**What**: Mediates between ViewModel and data sources (DB/API).
**Examples**: UserRepository, CityRepository, WeatherRepository

### Helper Class
**What**: Plain Java class that organizes business logic (network calls, data processing).
**Note**: NOT Android Service components (android.app.Service).
**Examples**: WeatherHelper, LLMHelper

---

## Key Classes

### Activity

**MainActivity** (Single Host Activity)
- `fragmentManager: FragmentManager` - Manages Fragment transactions
- `cityListViewModel: CityListViewModel` - Shared ViewModel across Fragments
- `onCreate()` - Setup initial Fragment (LoginFragment)
- `navigateToFragment(Fragment)` - Replace current Fragment with new one
- `onBackPressed()` - Handle back navigation through Fragment backstack
- `applyUserTheme()` - Apply theme across all Fragments

---

### Fragments

**LoginFragment**
- `loginViewModel: LoginViewModel` - Manages auth logic
- `onCreateView()` - Inflate fragment_login.xml layout
- `onViewCreated()` - Setup UI, observe login result
- `onLoginButtonClick()` - Validate and trigger login
- `navigateToCityList()` - Navigate to CityListFragment on success

**CityListFragment**
- `cityListViewModel: CityListViewModel` - Shared ViewModel (scoped to MainActivity)
- `cityAdapter: CityAdapter` - RecyclerView adapter
- `observeCityList()` - Watch for city changes via LiveData
- `onCityItemClick(City)` - Navigate to DetailsFragment with Bundle

**DetailsFragment**
- `weatherViewModel: WeatherViewModel` - Manages weather/LLM data
- `cityId, cityName: Long, String` - Passed via Bundle arguments
- `displayWeatherData(WeatherData)` - Update UI with weather
- `onGetInsightsClick()` - Fetch AI insights

**MapFragment**
- `cityLatitude, cityLongitude: Double` - Passed via Bundle
- `onMapReady(GoogleMap)` - Setup map when loaded
- `addCityMarker()` - Place marker at city location

**ThemeSettingsFragment**
- `themeViewModel: ThemeViewModel` - Manages theme selection
- `themeAdapter: ThemeAdapter` - RecyclerView adapter for theme list
- `onThemeSelected(Theme)` - Handle user theme selection
- `displayAvailableThemes()` - Show list of available themes
- `observeCurrentTheme()` - Watch for current theme changes

---

### ViewModels

**LoginViewModel**
- `userRepository: UserRepository` - Data access
- `loginResult: LiveData<Result<User>>` - Observable auth result
- `login(username, password)` - Authenticate user

**CityListViewModel** (Shared across Fragments)
- `cityRepository: CityRepository`
- `citiesLiveData: LiveData<List<City>>` - Observable city list
- `addCity(cityName)` - Validate and add city
- `removeCity(cityId)` - Delete city

**WeatherViewModel**
- `weatherRepository: WeatherRepository`
- `weatherData: LiveData<WeatherData>` - Observable weather
- `llmInsights: LiveData<String>` - Observable AI insights
- `fetchWeather(cityId)` - Get weather (cache or network)

**ThemeViewModel**
- `userRepository: UserRepository` - Access user preferences
- `availableThemes: LiveData<List<Theme>>` - Observable theme list
- `currentTheme: LiveData<Theme>` - Observable current theme
- `loadAvailableThemes()` - Fetch all available themes
- `selectTheme(themeId)` - Apply selected theme
- `getCurrentTheme()` - Get user's current theme

---

### Helper Classes

**WeatherHelper** (Plain Java class, NOT Android Service)
- `weatherApiClient: WeatherApiClient` - HTTP client
- `database: AppDatabase` - For caching
- `fetchWeatherData(lat, lon): WeatherData` - API call
- `cacheWeatherData(WeatherData)` - Save to DB
- `isDataStale(WeatherData): Boolean` - Check if >30 min old

**Note**: This is called "WeatherHelper" (not WeatherService) to avoid confusion with Android Service components. It's a plain Java class for organizing network logic.

**LLMHelper** (Plain Java class, NOT Android Service)
- `geminiApiClient: GeminiApiClient`
- `generateInsights(WeatherData): String` - Call Gemini API
- `constructPrompt(WeatherData): String` - Build AI prompt

---

### Repositories

**UserRepository**
- `userDao: UserDao` - Database access
- `sharedPreferences: SharedPreferences` - Session storage
- `authenticateUser(username, password): User` - Validate credentials
- `getCurrentUser(): User` - Get logged-in user

**CityRepository**
- `cityDao: CityDao`
- `weatherApiClient: WeatherApiClient` - For validation
- `getCitiesForUser(userId): List<City>`
- `validateCity(cityName): Boolean` - Check if city exists via API
- `fetchCityCoordinates(cityName): Pair<Double>` - Get lat/lon

**WeatherRepository**
- `weatherHelper: WeatherHelper` - For network operations
- `weatherDao: WeatherDao`
- `getWeather(cityId): WeatherData` - Smart fetch (cache vs network)
- `refreshWeather(cityId): WeatherData` - Force network fetch

---

### Data Models (Entities)

**User**
- `id: Long` - Primary key
- `username: String` - Unique
- `passwordHash: String` - Bcrypt hash (never plain text)
- `themePreference: String` - "light", "dark", "blue"

**City**
- `id: Long` - Primary key
- `userId: Long` - Foreign key to User
- `name: String` - "Chicago"
- `latitude, longitude: Double` - GPS coordinates

**WeatherData**
- `id: Long` - Primary key
- `cityId: Long` - Foreign key to City
- `temperature, humidity, windSpeed` - Weather metrics
- `timestamp: Date` - When fetched
- `isStale(): Boolean` - True if >30 min old

**Theme** (Note: Could be simplified to User.themePreference field)
- `id: Long`
- `primaryColor, secondaryColor: String` - Hex colors
- `isDarkMode: Boolean`

---

### DAOs

**UserDao**
- `insert(User): Long`
- `getUserByUsername(String): User`
- `getUserById(Long): User`

**CityDao**
- `insert(City): Long`
- `delete(City): void`
- `getCitiesByUserId(Long): List~City~` - Filtered by user

**WeatherDao**
- `insert(WeatherData): Long` - REPLACE on conflict (keeps latest)
- `getWeatherByCityId(Long): WeatherData`
- `deleteOldWeather(Date): void` - Cleanup

---

### API Clients

**WeatherApiClient** (Retrofit)
- `getCurrentWeather(lat, lon): Call<WeatherResponse>`
- `searchCity(cityName): Call<CitySearchResponse>`

**GeminiApiClient** (Retrofit)
- `generateContent(prompt): Call<GeminiResponse>`

---

### Database

**AppDatabase** (RoomDatabase)
- `userDao, cityDao, weatherDao` - DAO accessors
- `getInstance(Context): AppDatabase` - Singleton pattern
- Manages 3 tables: User, City, WeatherData

---

### Utility

**CityAdapter** (RecyclerView.Adapter)
- `cities: List<City>` - Data source
- `onBindViewHolder()` - Bind city to view
- `onItemClickListener` - Click callback

---

## Relationships

**MainActivity-Fragment Relationships**:
- MainActivity `1` ↔ `*` Fragments (hosts multiple Fragments: Login, CityList, Details, Map, ThemeSettings)
- Uses FragmentManager for transactions (replace, add to backstack)

**Entity Relationships**:
- User `1` ↔ `*` City (one user, many cities)
- City `1` ↔ `1` WeatherData (one city, one weather snapshot)
- User `1` ↔ `1` Theme (one user, one theme)

**Component Dependencies**:
- Fragment → ViewModel (1:1 or shared)
- ViewModel → Repository (1:1)
- Repository → DAO/Helper (1:1)
- Helper → API Client (1:1)

---

## Data Flow Example

**User clicks "Chicago" to view weather (Fragment navigation)**:

1. CityListFragment → `onCityItemClick(city)` → Create Bundle with cityId
2. CityListFragment → `parentFragmentManager.beginTransaction().replace(R.id.fragment_container, DetailsFragment.newInstance(bundle)).addToBackStack(null).commit()`
3. DetailsFragment → `onCreate()` → Extract cityId from arguments Bundle
4. DetailsFragment → `onViewCreated()` → `weatherViewModel.fetchWeather(cityId)`
5. WeatherViewModel → `weatherRepository.getWeather(cityId)`
6. WeatherRepository → Check cache via `weatherDao.getWeatherByCityId()`
7. If stale → WeatherHelper → `weatherApiClient.getCurrentWeather(lat, lon)`
8. WeatherHelper → Parse JSON → Create WeatherData entity
9. WeatherHelper → `weatherDao.insert(data)` (cache for next time)
10. WeatherRepository → Return data to ViewModel
11. WeatherViewModel → Update `weatherData` LiveData
12. DetailsFragment → Observer triggered → `displayWeatherData()` updates UI

**Key**:
- Fragment navigation uses FragmentTransaction instead of Intents
- Data passed via Bundle arguments instead of Intent extras
- ViewModels can be shared across Fragments (scoped to MainActivity)

---

## Fragment Navigation Pattern

### Navigating Between Fragments:

```java
// From CityListFragment to DetailsFragment
Bundle bundle = new Bundle();
bundle.putLong("cityId", city.getId());
bundle.putString("cityName", city.getName());

DetailsFragment fragment = new DetailsFragment();
fragment.setArguments(bundle);

getParentFragmentManager()
    .beginTransaction()
    .replace(R.id.fragment_container, fragment)
    .addToBackStack(null)  // Add to back stack for back navigation
    .commit();
```

### Retrieving Data in Target Fragment:

```java
// In DetailsFragment
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
        cityId = getArguments().getLong("cityId");
        cityName = getArguments().getString("cityName");
    }
}
```

### Shared ViewModel Pattern:

```java
// In CityListFragment and DetailsFragment (both access same ViewModel)
CityListViewModel viewModel = new ViewModelProvider(requireActivity())
    .get(CityListViewModel.class);
// requireActivity() scopes ViewModel to MainActivity, shared across Fragments
```

---

## Technical Decisions

**Java + XML**: Traditional Android development for team familiarity and stability
**Single Activity + Fragments**: Modern navigation pattern, easier data sharing, consistent UI
**Room Database**: Type-safe, compile-time SQL validation, LiveData integration
**Retrofit**: Declarative HTTP API, automatic JSON parsing
**LiveData**: Lifecycle-aware, prevents memory leaks, auto UI updates
**Repository Pattern**: Abstracts data sources, enables caching strategy
**MVVM**: ViewModel survives rotation, testable business logic
**Helper Classes (not Services)**: Plain Java classes for business logic, avoids Android Service complexity

---

## Architecture Notes

**Why Single Activity + Fragments**:
- Simplified navigation (no Intent extras, use Bundle arguments)
- Shared ViewModels across screens (scoped to MainActivity)
- Consistent UI and theming
- Fragment backstack for intuitive back navigation
- Modern Android best practice

**Helper Classes vs Android Services**:
WeatherHelper and LLMHelper are plain Java classes, NOT Android Service components (android.app.Service). They organize network and business logic without the complexity of Service lifecycle management. All operations are user-initiated, so background Services are unnecessary.

**Theme Entity Simplification**:
For this app's requirements (3 predefined themes), the separate Theme table is unnecessary. Recommendation: Store `themePreference: String` in User table and define theme colors in `themes.xml`. Only use Theme entity if users can create custom themes.

**Future Enhancements**:
- Navigation Component for type-safe Fragment navigation
- Dependency Injection (Hilt/Dagger) for cleaner constructor injection
- Coroutines instead of Retrofit Call<T> for simpler async code
- Paging 3 for large city lists (100+ items)
