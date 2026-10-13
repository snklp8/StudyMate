package com.studymate.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.studymate.app.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Singleton Supabase client scoped to the application lifetime.
 *
 * Configured for Supabase Third-Party Authentication with Firebase Auth:
 * - [accessToken] lambda dynamically supplies the current Firebase ID token.
 *   Supabase Storage automatically includes this token in the `Authorization: Bearer`
 *   header for all authenticated storage requests.
 * - [Storage] plugin is installed for operations on the private `study-materials` bucket.
 *
 * The URL and publishable (anon) key are injected at compile-time from BuildConfig,
 * which reads them from local.properties. No secret or service-role keys are exposed.
 */
object SupabaseClientProvider {

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        accessToken = {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                null
            } else {
                suspendCancellableCoroutine { continuation ->
                    user.getIdToken(false).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(task.result?.token)
                        } else {
                            continuation.resume(null)
                        }
                    }
                }
            }
        }
        install(Storage)
    }
}
