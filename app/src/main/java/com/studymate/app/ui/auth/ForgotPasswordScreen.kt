package com.studymate.app.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.StudyMateButton
import com.studymate.app.ui.components.StudyMateTextField
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val forgotPasswordState by authViewModel.forgotPasswordState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(forgotPasswordState) {
        if (forgotPasswordState is ForgotPasswordState.Success) {
            snackbarHostState.showSnackbar("Password reset instructions dispatched to $email")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enter the email associated with your StudyMate university account to receive reset instructions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            StudyMateTextField(
                value = email,
                onValueChange = {
                    email = it
                    localError = null
                },
                label = "Email Address",
                placeholder = "name@university.edu",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (forgotPasswordState is ForgotPasswordState.Success) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Reset link dispatched! Please check your university inbox.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = SuccessGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (localError != null) {
                Text(
                    text = localError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else if (forgotPasswordState is ForgotPasswordState.Error) {
                Text(
                    text = (forgotPasswordState as ForgotPasswordState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            StudyMateButton(
                text = if (forgotPasswordState is ForgotPasswordState.Success) "Resend Instructions" else "Reset Password",
                onClick = {
                    if (email.isBlank()) {
                        localError = "Please enter your email address"
                    } else {
                        localError = null
                        authViewModel.sendPasswordReset(email)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isLoading = forgotPasswordState is ForgotPasswordState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remember password? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Return to Login",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
