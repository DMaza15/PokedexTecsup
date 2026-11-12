package com.tecsup.pokedextecsup.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.pokedextecsup.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val message: String, val role: String = "USER") : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUserEmail = MutableStateFlow(repository.currentUser?.email ?: "")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _userRole = MutableStateFlow("USER")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        checkUserSession()
    }

    fun checkUserSession() {
        val user = repository.currentUser
        if (user != null) {
            _currentUserEmail.value = user.email ?: ""
            viewModelScope.launch {
                val role = repository.getUserRole(user.uid)
                val name = repository.getUserName(user.uid)
                _userRole.value = role
                _userName.value = name
            }
        } else {
            _currentUserEmail.value = ""
            _userRole.value = "USER"
            _userName.value = ""
        }
    }

    fun isUserLoggedIn(): Boolean = repository.isUserLoggedIn()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor completa todos los campos.")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("El formato del correo electrónico no es válido.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            repository.login(email.trim(), password)
                .onSuccess { user ->
                    val role = repository.getUserRole(user.uid)
                    val name = repository.getUserName(user.uid)
                    _userRole.value = role
                    _userName.value = name
                    _currentUserEmail.value = user.email ?: ""
                    _uiState.value = AuthUiState.Success("Sesión iniciada correctamente", role)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(
                        error.localizedMessage ?: "Credenciales incorrectas o error de inicio de sesión."
                    )
                }
        }
    }

    fun register(email: String, password: String, confirmPassword: String, name: String) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || name.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor completa todos los campos del registro.")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Ingresa un correo electrónico válido.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Las contraseñas no coinciden.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            repository.register(email.trim(), password, name.trim(), role = "USER")
                .onSuccess { user ->
                    _currentUserEmail.value = user.email ?: ""
                    _userRole.value = "USER"
                    _userName.value = name.trim()
                    _uiState.value = AuthUiState.Success("Cuenta registrada e iniciada correctamente", "USER")
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(
                        error.localizedMessage ?: "No se pudo registrar la cuenta."
                    )
                }
        }
    }

    fun logout() {
        repository.logout()
        _currentUserEmail.value = ""
        _userRole.value = "USER"
        _userName.value = ""
        _uiState.value = AuthUiState.Idle
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }

    class Factory(
        private val repository: AuthRepository = AuthRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
