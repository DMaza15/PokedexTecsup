package com.tecsup.pokedextecsup.domain.model

data class PokemonItem(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val isFavorite: Boolean = false
)
