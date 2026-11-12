package com.tecsup.pokedextecsup.domain.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val imageUrl: String,
    val types: List<String>,
    val stats: List<StatModel>,
    val abilities: List<String>,
    val isFavorite: Boolean = false
)

data class StatModel(
    val name: String,
    val value: Int
)
