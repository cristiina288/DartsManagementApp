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

