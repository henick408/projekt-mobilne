package pl.edu.przedmioty.data

import com.google.firebase.auth.FirebaseAuth

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun isSignedIn(): Boolean = auth.currentUser != null

    fun register(
        email: String,
        password: String,
        onResult: (Result<Unit>) -> Unit,
    ) {
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Result<Unit>) -> Unit,
    ) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun logout() {
        auth.signOut()
    }
}