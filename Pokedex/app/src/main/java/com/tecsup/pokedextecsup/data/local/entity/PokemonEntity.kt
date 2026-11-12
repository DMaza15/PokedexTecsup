package com.tecsup.pokedextecsup.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tecsup.pokedextecsup.domain.model.PokemonItem

@Entity(tableName = "favorite_pokemon")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val types: String = "",
    val height: Int = 0,
    val weight: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toPokemonItem(): PokemonItem {
        return PokemonItem(
            id = id,
            name = name,
            imageUrl = imageUrl,
            isFavorite = true
        )
    }
}
