package com.studymate.app.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.studymate.app.data.model.User
import com.studymate.app.data.model.UserRole
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty."))
        }

        return try {
            val authResult = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("Authentication failed: Missing UID"))

            val docSnapshot = firestore.collection("users").document(uid).get().await()
            if (!docSnapshot.exists()) {
                // User authenticated in Firebase Auth but has no Firestore profile document
                auth.signOut()
                return Result.failure(Exception("User profile not found. Please contact support or register again."))
            }

            val roleString = docSnapshot.getString("role")?.lowercase()
            if (roleString == null || (roleString != "student" && roleString != "teacher")) {
                auth.signOut()
                return Result.failure(Exception("Invalid or missing account role. Please contact support."))
            }

            val user = User(
                id = docSnapshot.getString("uid") ?: uid,
                name = docSnapshot.getString("name") ?: (authResult.user?.displayName ?: ""),
                email = docSnapshot.getString("email") ?: trimmedEmail,
                role = if (roleString == "teacher") UserRole.TEACHER else UserRole.STUDENT,
                course = docSnapshot.getString("course"),
                semester = docSnapshot.getLong("semester")?.toInt(),
                department = docSnapshot.getString("department"),
                profileImageUrl = docSnapshot.getString("profileImageUrl"),
                createdAt = docSnapshot.getLong("createdAt") ?: System.currentTimeMillis()
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User> {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.isBlank() || trimmedEmail.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("All fields are required."))
        }

        return try {
            val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("Registration failed: Missing UID"))

            val roleString = if (role == UserRole.TEACHER) "teacher" else "student"
            val currentTime = System.currentTimeMillis()

            val userProfileMap = hashMapOf<String, Any>(
                "uid" to uid,
                "name" to trimmedName,
                "email" to trimmedEmail,
                "role" to roleString,
                "createdAt" to currentTime
            )

            // Save user profile in Firestore at users/{uid}
            firestore.collection("users").document(uid).set(userProfileMap).await()

            val createdUser = User(
                id = uid,
                name = trimmedName,
                email = trimmedEmail,
                role = role,
                createdAt = currentTime
            )

            Result.success(createdUser)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return try {
            val docSnapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
            if (!docSnapshot.exists()) {
                // If authenticated session exists but Firestore profile is missing, sign out
                auth.signOut()
                return null
            }

            val roleString = docSnapshot.getString("role")?.lowercase()
            if (roleString == null || (roleString != "student" && roleString != "teacher")) {
                auth.signOut()
                return null
            }

            User(
                id = docSnapshot.getString("uid") ?: firebaseUser.uid,
                name = docSnapshot.getString("name") ?: (firebaseUser.displayName ?: ""),
                email = docSnapshot.getString("email") ?: (firebaseUser.email ?: ""),
                role = if (roleString == "teacher") UserRole.TEACHER else UserRole.STUDENT,
                course = docSnapshot.getString("course"),
                semester = docSnapshot.getLong("semester")?.toInt(),
                department = docSnapshot.getString("department"),
                profileImageUrl = docSnapshot.getString("profileImageUrl"),
                createdAt = docSnapshot.getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }

        return try {
            auth.sendPasswordResetEmail(trimmedEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun logout() {
        try {
            auth.signOut()
        } catch (_: Exception) {
        }
    }

    private fun mapAuthException(exception: Exception): Exception {
        val message = when (exception) {
            is FirebaseAuthInvalidUserException -> "No account found with this email."
            is FirebaseAuthInvalidCredentialsException -> "Invalid email address or incorrect password."
            is FirebaseAuthUserCollisionException -> "An account with this email already exists."
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters."
            is FirebaseNetworkException -> "Network error. Please check your internet connection."
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
                    "ERROR_WRONG_PASSWORD" -> "The password entered is incorrect."
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email."
                    "ERROR_USER_DISABLED" -> "This account has been disabled."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Email/Password sign-in is not enabled in Firebase Console."
                    else -> exception.localizedMessage ?: "Authentication failed. Please try again."
                }
            }
            else -> exception.localizedMessage ?: "An unexpected error occurred. Please try again."
        }
        return Exception(message)
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(
                    task.exception ?: RuntimeException("Firebase operation completed without a result or exception.")
                )
            }
        }
    }
}
