package com.tecsup.pokedextecsup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.pokedextecsup.data.repository.AuthRepository
import com.tecsup.pokedextecsup.domain.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminUiState {
    object Loading : AdminUiState
    data class Success(val users: List<UserModel>) : AdminUiState
    data class Error(val message: String) : AdminUiState
}

class AdminViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            repository.getAllUsers()
                .onSuccess { users ->
                    _uiState.value = AdminUiState.Success(users)
                }
                .onFailure { error ->
                    _uiState.value = AdminUiState.Error(
                        error.localizedMessage ?: "No se pudieron obtener los usuarios desde Firestore."
                    )
                }
        }
    }

    fun toggleUserRole(user: UserModel) {
        val newRole = if (user.rol == "ADMIN") "USER" else "ADMIN"
        viewModelScope.launch {
            repository.updateUserRole(user.uid, newRole)
                .onSuccess {
                    _actionMessage.value = "Rol de ${user.nombre} cambiado a $newRole."
                    loadUsers()
                }
                .onFailure { error ->
                    _actionMessage.value = "Error al actualizar rol: ${error.localizedMessage}"
                }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    class Factory(
        private val repository: AuthRepository = AuthRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminViewModel(repository) as T
        }
    }
}
