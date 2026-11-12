package com.tecsup.pokedextecsup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.pokedextecsup.data.repository.PokemonRepository
import com.tecsup.pokedextecsup.domain.model.PokemonItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CatalogUiState {
    object Loading : CatalogUiState
    data class Success(val pokemonList: List<PokemonItem>) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

class CatalogViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadFirePokemon()
    }

    fun loadFirePokemon() {
        _uiState.value = CatalogUiState.Loading
        viewModelScope.launch {
            repository.getFirePokemonList()
                .onSuccess { list ->
                    if (list.isEmpty()) {
                        _uiState.value = CatalogUiState.Error("No se encontraron Pokémon de tipo Fuego.")
                    } else {
                        _uiState.value = CatalogUiState.Success(list)
                    }
                }
                .onFailure { error ->
                    _uiState.value = CatalogUiState.Error(
                        error.localizedMessage ?: "Error de conexión al obtener el catálogo de tipo Fuego."
                    )
                }
        }
    }

    fun toggleFavorite(item: PokemonItem) {
        viewModelScope.launch {
            if (item.isFavorite) {
                repository.removeFavorite(item.id)
            } else {
                repository.addFavorite(item)
            }
        }
    }

    class Factory(
        private val repository: PokemonRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CatalogViewModel(repository) as T
        }
    }
}
