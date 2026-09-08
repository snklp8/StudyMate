package com.studymate.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.data.model.UserRole
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.StudyMateButton
import com.studymate.app.ui.components.StudyMateLogoBadge
import com.studymate.app.ui.components.StudyMateTextField
import com.studymate.app.ui.theme.BorderSubtle

@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var localError by remember { mutableStateOf<String?>(null) }

    val registerState by authViewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Success) {
            val user = (registerState as RegisterState.Success).user
            val route = if (user.role == UserRole.TEACHER) Screen.TeacherDashboard.route else Screen.StudentHome.route
            navController.navigate(route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            StudyMateLogoBadge(
                iconSize = 48.dp,
                showTagline = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Join your university learning ecosystem",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Role selection tabs
            Text(
                text = "Select your role",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Pair(UserRole.STUDENT, "Student" to Icons.Default.School),
                    Pair(UserRole.TEACHER, "Teacher" to Icons.Default.SupervisorAccount)
                ).forEach { (role, data) ->
                    val isSelected = selectedRole == role
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { selectedRole = role },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = data.second,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = data.first,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            StudyMateTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                placeholder = if (selectedRole == UserRole.STUDENT) "Alex Mercer" else "Dr. Rajesh Kumar",
                leadingIcon = {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            StudyMateTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                placeholder = "name@university.edu",
                leadingIcon = {
                    Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            StudyMateTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Create strong password",
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            StudyMateTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                placeholder = "Re-enter password",
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )

            if (localError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = localError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (registerState is RegisterState.Error) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = (registerState as RegisterState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            StudyMateButton(
                text = "Register as ${if (selectedRole == UserRole.STUDENT) "Student" else "Teacher"}",
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        localError = "Please fill in all required fields"
                    } else if (password != confirmPassword) {
                        localError = "Passwords do not match"
                    } else {
                        localError = null
                        authViewModel.register(name, email, password, selectedRole)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isLoading = registerState is RegisterState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
