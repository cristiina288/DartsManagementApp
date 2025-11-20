# Darts Management KMP Application - Development Guide

## Project Overview
This is a production-ready Kotlin Multiplatform (KMP) application for Darts Management. It targets both Android and iOS from a single shared codebase, leveraging modern development practices including Jetpack Compose for Multiplatform, a clean, ViewModel-centric architecture, and SOLID principles.

## Enhanced AI Development Prompt

### Kotlin Multiplatform Application Development Specialist Prompt

You are an expert Kotlin Multiplatform developer tasked with creating and maintaining a professional, production-ready application. Your role is to architect, implement, and deliver a complete app for both Android and iOS following modern best practices, industry standards, and the specific technical requirements outlined below.

#### Core Technical Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose for Multiplatform
- **Architecture**: Clean Architecture with MVVM (ViewModel-centric)
- **Dependency Injection**: Koin
- **Navigation**: Voyager
- **Networking**: Ktor (with Firebase for BaaS)
- **Backend Services**: Firebase (Authentication, Firestore, Storage, Analytics)

##### Firebase Firestore Integration Strategy
This section outlines the strategy and current state of integrating Firebase Firestore as the backend for data persistence.

**1. Firestore Data Model:**

*   **`licenses` Collection:**
    *   Document ID: `license_123`
    *   Fields: `company_name` (String), `expires_at` (Timestamp), `status` (String)

*   **`users` Collection:**
    *   Document ID: Firebase Authentication UID
    *   Fields: `email` (String), `name` (String), `license_id` (String, links to `licenses` collection)

*   **`machines` Collection:**
    *   Document ID: Machine ID (e.g., `101`)
    *   Fields: `name` (String), `type` (String), `last_collection` (Timestamp), `counter` (Number), `barId` (String, links to `bars`), `status` (Map with `id` (Number))

*   **`locations` Collection:**
    *   Document ID: Location ID (e.g., `loc_001`)
    *   Fields: `address` (String), `city` (String), `latitude` (Double), `longitude` (Double), `name` (String), `postalCode` (String), `locationBarUrl` (String)

*   **`bars` Collection:**
    *   Document ID: Bar ID (e.g., `bar_abc`)
    *   Fields: `license_id` (String, links to `licenses`), `id` (Number), `name` (String), `description` (String), `machine_ids` (Array of Numbers, links to `machines`), `location_id` (String, links to `locations`), `status_id` (Number)

**2. `getBars` Implementation Approach:**

*   The `getBars` function in `BarsApiService` now fetches data from Firestore.
*   It performs a multi-step query:
    1.  Get current user's UID.
    2.  Fetch user's `license_id` from the `users` collection.
    3.  Query `bars` collection for documents matching the `license_id`.
    4.  For each bar, fetch its associated `locations` and `machines` documents using their respective IDs.
    5.  Map the aggregated Firestore data to the existing `BarResponse` DTOs.

**3. `saveBar` Implementation Approach (Basic):**

*   The `saveBar` function in `BarsApiService` currently implements a basic save operation.
*   It takes a `SaveBarRequest` and directly adds a document to the `bars` collection.
*   Location details (`address`, `latitude`, `longitude`) from `SaveBarRequest` are embedded directly into the `bars` document, rather than creating a separate `locations` document and linking to it. This is a temporary, simplified approach.

**4. Known Issues and Next Steps:**

*   **iOS `ExpectedFirestore` Implementation:** The `actual` implementation for `ExpectedFirestore` (specifically for `getDocument`, `getDocuments`, `getCurrentUserUID`) is currently only provided for Android. The iOS platform will require its own `actual` implementation to compile and function correctly.
*   **Data Mapping Discrepancies:** There are mismatches between the fields available in the Firestore data models (e.g., `LocationFirestore`, `MachineFirestore`) and the fields expected by the existing `...Response` DTOs (e.g., `LocationResponse`, `MachineResponse`). This may lead to incomplete data in the UI or require further reconciliation.
*   **Inefficient Machine Fetching (N+1 Problem):** In `getBars`, machines are currently fetched one by one for each bar. This results in an N+1 query problem, which can be inefficient for bars with many machines. Optimizing this to fetch all machines in a single query (e.g., using a `whereIn` clause if supported by the `ExpectedFirestore` abstraction) is a recommended next step.

- **Minimum Android SDK**: API 24 (Android 7.0)
- **Target Android SDK**: Latest stable (35)

#### Key Implementation Guidelines

##### 1. Project Structure (Kotlin Multiplatform)
The project follows a standard KMP structure. All business logic, UI, and application state should be placed in `commonMain`. Platform-specific implementations or initializations reside in `androidMain` and `iosMain`.
- **`composeApp/src/commonMain`**: The core of the application. Contains shared UI (Composables), ViewModels, navigation, repositories, use cases, and data models. **95% of the code should live here.**
- **`composeApp/src/androidMain`**: Android-specific entry points (`MainActivity`), platform services, and Koin module initializations.
- **`composeApp/src/iosMain`**: iOS-specific entry points, platform services, and UIViewController setup.
- **`composeApp/src/commonMain/composeResources`**: Shared resources like fonts, images, and strings.

##### 2. Design System Implementation
- **Colors**: Use Material 3 color system (`MaterialTheme.colorScheme`) with support for light/dark themes defined in `ui.theme`. Avoid hardcoded colors.
- **Typography**: Define typography scales in the Material 3 theme.
- **Components**: Build small, reusable, and stateless UI components in the `ui.components` package.
- **Spacing**: Use a consistent spacing system (e.g., multiples of 8.dp) defined in a `Dimens.kt` object.
- **Edge-to-Edge**: Implement edge-to-edge rendering for a modern look and feel, properly handling window insets.

##### 3. State Management Pattern (ViewModel with StateFlow)
All screens must use a ViewModel to manage state and business logic. The UI observes a single `StateFlow<UiState>`.

```kotlin
// ViewModel with StateFlow, injected with Koin
class MyScreenViewModel(
    private val myUseCase: MyUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyScreenUiState())
    val uiState: StateFlow<MyScreenUiState> = _uiState.asStateFlow()
    
    fun onEvent(event: MyScreenEvent) {
        // Handle UI events and trigger business logic
    }
}

// UI State data class, containing all screen state
data class MyScreenUiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null
)

// Sealed interface for UI events
sealed interface MyScreenEvent {
    data class OnItemClick(val id: String) : MyScreenEvent
}
```

##### 4. Navigation Pattern (Voyager)
Navigation is managed by Voyager. Screens are represented as `Screen` objects. Use sealed classes to define navigation routes for type safety.

```kotlin
// Sealed class for type-safe navigation routes
sealed class AppScreen(val title: String) : Screen {
    data object Login : AppScreen("Login") {
        @Composable
        override fun Content() { /* LoginScreen Composable */ }
    }
    
    data object Home : AppScreen("Home") {
        @Composable
        override fun Content() { /* HomeScreen Composable */ }
    }
    
    data class GameDetails(val gameId: String) : AppScreen("Game Details") {
        @Composable
        override fun Content() { /* GameDetailsScreen Composable */ }
    }
}

// Navigating between screens
val navigator = LocalNavigator.currentOrThrow
navigator.push(AppScreen.GameDetails(gameId = "123"))
```

##### 5. Dependency Injection (Koin)
Koin is used for dependency injection across the multiplatform project. Define modules in `commonMain` and initialize them in the platform-specific entry points.

```kotlin
// commonMain/di/AppModule.kt
val appModule = module {
    // Repositories
    single<AuthRepository> { FirebaseAuthRepository(get()) }
    
    // Use Cases
    factory { LoginUseCase(get()) }
    
    // ViewModels
    viewModel { LoginViewModel(get()) }
}

// androidMain/DartsManagementApp.kt
class DartsManagementApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@DartsManagementApp)
            modules(appModule)
        }
    }
}
```

#### Best Practices
1.  **Composable Responsibilities**: Keep Composables small, stateless, and focused on UI. Hoist state and logic to ViewModels.
2.  **State Hoisting**: Always separate stateful (screen-level) and stateless (component-level) composables. The stateless composable should be easily previewable and reusable.
3.  **Side Effects**: Use `LaunchedEffect` for suspend functions and side effects tied to the Composable's lifecycle. Avoid launching coroutines directly from Composables without proper lifecycle management.
4.  **Performance**: Use `remember` for expensive calculations and `derivedStateOf` for state that changes frequently. Use `key()` to help Compose optimize recompositions in lists.
5.  **Testing**: Write unit tests for ViewModels and use cases in `commonTest`.
6.  **Resource Management (`composeResources`)**:
    - **Strings, Images, Fonts**: Place all shared assets in `composeApp/src/commonMain/composeResources/drawable` and `composeApp/src/commonMain/composeResources/font`. Access them using `painterResource()` and `fontResource()`.
    - **NEVER** use Android's `R.drawable` or `stringResource(R.string.xxx)` in `commonMain`. All resources must be accessed via the Compose Resources library to ensure they are available on iOS.
7.  **ViewModel Co-location**: Place each ViewModel in the same package as its corresponding screen to improve discoverability and feature cohesion (e.g., `ui.screens.login` contains `LoginScreen.kt` and `LoginViewModel.kt`).
8.  **Use Case Invocation**: Use Kotlin's `operator fun invoke()` pattern for use cases to make them more idiomatic (e.g., `loginUseCase(email, password)` instead of `loginUseCase.execute(email, password)`).
9.  **Previews**: Create multiple `@Preview` functions for each stateless Composable to visualize different states (e.g., loading, error, populated data).

# UI/UX Design Prompt – Dark-Mode-Only Multiplatform App

## Global Design Principles
*   The app is always in Dark Mode.
*   Visual identity: modern, clean, professional, suitable for an internal company tool.
*   Color direction: deep greys with teal/cyan accents.
*   Typography: Inter or Roboto, with clear hierarchy.
*   Rounded corners: 8–12dp.
*   Components must be visually rich without images.
*   UI must feel consistent across Android and iOS.

## Color Palette (Suggested)
*   Background: #0B0F13
*   Surface: #111417
*   Elevated Surface: #15181B
*   Primary Accent: #00BFA6
*   Secondary Accent: #8BE9FD
*   Warm Accent: #FFB86B
*   Text Primary: #E6EEF3
*   Text Secondary: #9AA6AD
*   Error: #FF6B6B