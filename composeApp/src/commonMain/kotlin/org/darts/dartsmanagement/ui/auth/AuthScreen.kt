package org.darts.dartsmanagement.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dartsmanagement.composeapp.generated.resources.Res
import dartsmanagement.composeapp.generated.resources.ico_dartboard
import dartsmanagement.composeapp.generated.resources.ico_hide_visibility
import dartsmanagement.composeapp.generated.resources.ico_show_visibility
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.ui.home.HomeScreen
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI


// Colores temáticos para la aplicación
object AuthColors {
    val Background = Color(0xFF0B0F13)
    val Surface = Color(0xFF111417)
    val Primary = Color(0xFF00BFA6)
    val TextPrimary = Color(0xFFE6EEF3)
    val TextSecondary = Color(0xFF9AA6AD)
    val Error = Color(0xFFFF6B6B)
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ico_dartboard),
            contentDescription = null,
            tint = AuthColors.Primary,
            modifier = Modifier.size(80.dp)
        )
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
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = AuthColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de submit
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuthColors.Primary,
                    contentColor = Color(0xFF0B0F13)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF0B0F13),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isLoginMode) "Iniciar Sesión" else "Crear Cuenta",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
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
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuthColors.Primary.copy(alpha = 0.8f)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AuthColors.Primary.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            focusedLabelColor = AuthColors.Primary,
            unfocusedLabelColor = AuthColors.TextSecondary,
            focusedTextColor = AuthColors.TextPrimary,
            unfocusedTextColor = AuthColors.TextPrimary,
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            cursorColor = AuthColors.Primary
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
                tint = AuthColors.Primary.copy(alpha = 0.8f)
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    painter = if (isPasswordVisible) {
                        painterResource(Res.drawable.ico_hide_visibility)
                    } else {
                        painterResource(Res.drawable.ico_show_visibility)
                    },
                    contentDescription = if (isPasswordVisible)
                        "Ocultar contraseña"
                    else
                        "Mostrar contraseña",
                    tint = AuthColors.TextSecondary,
                    modifier = Modifier.size(24.dp)
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
            focusedBorderColor = AuthColors.Primary.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            focusedLabelColor = AuthColors.Primary,
            unfocusedLabelColor = AuthColors.TextSecondary,
            focusedTextColor = AuthColors.TextPrimary,
            unfocusedTextColor = AuthColors.TextPrimary,
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            cursorColor = AuthColors.Primary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}