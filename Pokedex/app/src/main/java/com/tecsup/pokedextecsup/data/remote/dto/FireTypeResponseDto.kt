package com.tecsup.pokedextecsup.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FireTypeResponseDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("pokemon") val pokemon: List<TypePokemonEntryDto>?
)

data class TypePokemonEntryDto(
    @SerializedName("slot") val slot: Int?,
    @SerializedName("pokemon") val pokemon: NamedApiResourceDto?
)

data class NamedApiResourceDto(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
) {
    fun extractId(): Int {
        return url.trimEnd('/').split('/').lastOrNull()?.toIntOrNull() ?: 0
    }

    fun getImageUrl(): String {
        val id = extractId()
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    }
}
