package org.darts.dartsmanagement.ui.auth


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.ui.home.HomeScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

// Colores temáticos para viaje/aventura
object AuthColors {
    val AdventureGreen = Color(0xFF2E7D32)
    val EarthBrown = Color(0xFF6D4C41)
    val SkyBlue = Color(0xFF1976D2)
    val LightGreen = Color(0xFF4CAF50)
    val Background = Color(0xFFF1F8E9)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1B5E20)
}


object AuthScreen : Screen {
    @Composable
    override fun Content() {
        AuthScreenContent()
    }
}



@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun AuthScreenContent(

) {
    val navigator = LocalNavigator.currentOrThrow

    val onNavigateToHome: () -> Unit = { navigator.push(HomeScreen) }
    val authViewModel = koinViewModel<AuthViewModel>()
    val uiState by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Mostrar mensajes de error usando Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Short
                )
            }
            authViewModel.clearError()
        }
    }

    // Navegar a Home cuando la autenticación sea exitosa
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AuthColors.Background,
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo/Título de la app
            AppHeader()

            Spacer(modifier = Modifier.height(40.dp))

            // Formulario de autenticación
            AuthCard(
                isLoginMode = uiState.isLoginMode,
                name = uiState.name,
                email = uiState.email,
                password = uiState.password,
                confirmPassword = uiState.confirmPassword,
                isLoading = uiState.isLoading,
                onNameChange = authViewModel::updateName,
                onEmailChange = authViewModel::updateEmail,
                onPasswordChange = authViewModel::updatePassword,
                onConfirmPasswordChange = authViewModel::updateConfirmPassword,
                onSubmit = authViewModel::authenticate,
                onToggleMode = authViewModel::toggleAuthMode
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Snackbar para mostrar errores
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun AppHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🗺️",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Marca tu camino",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = AuthColors.AdventureGreen
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Guarda y gestiona tus lugares favoritos",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AuthColors.OnSurface.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthCard(
    isLoginMode: Boolean,
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = AuthColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título del formulario
            Text(
                text = if (isLoginMode) "Iniciar Sesión" else "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AuthColors.AdventureGreen
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de nombre (solo en modo registro)
            AnimatedVisibility(visible = !isLoginMode) {
                Column {
                    AuthTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = "Nombre completo",
                        icon = Icons.Default.Person,
                        keyboardType = KeyboardType.Text
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Campo de email
            AuthTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de contraseña
            AuthPasswordField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Contraseña"
            )

            // Campo de confirmar contraseña (solo en modo registro)
            AnimatedVisibility(visible = !isLoginMode) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthPasswordField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = "Confirmar contraseña"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de submit
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuthColors.AdventureGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isLoginMode) "Iniciar Sesión" else "Crear Cuenta",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enlace para cambiar de modo
            TextButton(
                onClick = onToggleMode,
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLoginMode)
                        "¿No tienes cuenta? Crear una nueva"
                    else
                        "¿Ya tienes cuenta? Iniciar sesión",
                    color = AuthColors.SkyBlue
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuthColors.AdventureGreen
            )
        },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AuthColors.AdventureGreen,
            focusedLabelColor = AuthColors.AdventureGreen,
            cursorColor = AuthColors.AdventureGreen
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = AuthColors.AdventureGreen
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible }
            ) {
                Icon(
                    imageVector = if (isPasswordVisible)
                        Icons.Default.Close
                    else
                        Icons.Default.Create,
                    contentDescription = if (isPasswordVisible)
                        "Ocultar contraseña"
                    else
                        "Mostrar contraseña",
                    tint = AuthColors.AdventureGreen.copy(alpha = 0.7f)
                )
            }
        },
        visualTransformation = if (isPasswordVisible)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AuthColors.AdventureGreen,
            focusedLabelColor = AuthColors.AdventureGreen,
            cursorColor = AuthColors.AdventureGreen
        ),
        shape = RoundedCornerShape(12.dp)
    )
}