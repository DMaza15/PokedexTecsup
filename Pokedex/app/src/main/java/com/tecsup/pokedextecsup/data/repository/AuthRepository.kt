package com.tecsup.pokedextecsup.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.pokedextecsup.domain.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: String = "USER"
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("No se pudo crear el usuario.")

            val userData = mapOf(
                "uid" to user.uid,
                "email" to email,
                "nombre" to name,
                "rol" to role
            )
            firestore.collection("users").document(user.uid).set(userData).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Credenciales incorrectas.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getUserRole(uid: String): String = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.getString("rol") ?: "USER"
        } catch (e: Exception) {
            "USER"
        }
    }

    suspend fun getUserName(uid: String): String = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.getString("nombre") ?: auth.currentUser?.email?.substringBefore("@") ?: "Usuario"
        } catch (e: Exception) {
            "Usuario"
        }
    }

    suspend fun getAllUsers(): Result<List<UserModel>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("users").get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                UserModel(
                    uid = doc.getString("uid") ?: doc.id,
                    email = doc.getString("email") ?: "",
                    nombre = doc.getString("nombre") ?: "",
                    rol = doc.getString("rol") ?: "USER"
                )
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserRole(uid: String, newRole: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(uid).update("rol", newRole).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
