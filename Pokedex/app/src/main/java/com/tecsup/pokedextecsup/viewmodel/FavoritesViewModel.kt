package com.tecsup.pokedextecsup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.pokedextecsup.data.repository.PokemonRepository
import com.tecsup.pokedextecsup.domain.model.PokemonItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface FavoritesUiState {
    object Loading : FavoritesUiState
    data class Success(val favorites: List<PokemonItem>) : FavoritesUiState
    object Empty : FavoritesUiState
}

class FavoritesViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = repository.favorites
        .map { list ->
            if (list.isEmpty()) {
                FavoritesUiState.Empty
            } else {
                FavoritesUiState.Success(list)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesUiState.Loading
        )

    fun removeFavorite(id: Int) {
        viewModelScope.launch {
            repository.removeFavorite(id)
        }
    }

    class Factory(
        private val repository: PokemonRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoritesViewModel(repository) as T
        }
    }
}
