package com.tecsup.pokedextecsup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.pokedextecsup.data.repository.PokemonRepository
import com.tecsup.pokedextecsup.domain.model.PokemonDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val pokemon: PokemonDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(
    private val pokemonName: String,
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            repository.getPokemonDetail(pokemonName)
                .onSuccess { detail ->
                    _uiState.value = DetailUiState.Success(detail)
                }
                .onFailure { error ->
                    _uiState.value = DetailUiState.Error(
                        error.localizedMessage ?: "No se pudieron obtener los detalles de $pokemonName."
                    )
                }
        }
    }

    fun toggleFavorite(detail: PokemonDetail) {
        viewModelScope.launch {
            if (detail.isFavorite) {
                repository.removeFavorite(detail.id)
                _uiState.value = DetailUiState.Success(detail.copy(isFavorite = false))
            } else {
                repository.addFavoriteFromDetail(detail)
                _uiState.value = DetailUiState.Success(detail.copy(isFavorite = true))
            }
        }
    }

    class Factory(
        private val pokemonName: String,
        private val repository: PokemonRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetailViewModel(pokemonName, repository) as T
        }
    }
}
