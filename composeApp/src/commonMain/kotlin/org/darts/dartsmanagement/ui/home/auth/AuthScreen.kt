package org.darts.dartsmanagement.ui.home.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.ui.home.HomeScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI


// Colores temáticos para viaje/aventura
object AuthColors {
    val Background = Color(0xFF1E2832)
    val Surface = Color(0xFF2C3A47)
    val Primary = Color(0xFF0ED2F7)
    val TextPrimary = Color.White
    val TextSecondary = Color.Gray
    val Error = Color(0xFFCF6679)
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
            .background(AuthColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            text = "Darts Management",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = AuthColors.TextPrimary
            )
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
                    color = AuthColors.TextPrimary
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
                    .height(56.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFB2FEFA), Color(0xFF0ED2F7))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
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
        label = { Text(label, color = AuthColors.TextSecondary) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuthColors.Primary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AuthColors.Primary,
            unfocusedBorderColor = AuthColors.TextSecondary,
            focusedLabelColor = AuthColors.Primary,
            cursorColor = AuthColors.Primary,
            focusedTextColor = AuthColors.TextPrimary,
            unfocusedTextColor = AuthColors.TextPrimary
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
        label = { Text(label, color = AuthColors.TextSecondary) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = AuthColors.Primary
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible }
            ) {
                Icon(
                    imageVector = if (isPasswordVisible)
                        Icons.Default.Clear
                    else
                        Icons.Default.ArrowDropDown,
                    contentDescription = if (isPasswordVisible)
                        "Ocultar contraseña"
                    else
                        "Mostrar contraseña",
                    tint = AuthColors.Primary.copy(alpha = 0.7f)
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
            focusedBorderColor = AuthColors.Primary,
            unfocusedBorderColor = AuthColors.TextSecondary,
            focusedLabelColor = AuthColors.Primary,
            cursorColor = AuthColors.Primary,
            focusedTextColor = AuthColors.TextPrimary,
            unfocusedTextColor = AuthColors.TextPrimary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}