package com.tecsup.pokedextecsup.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PokemonDetailDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("height") val height: Int,
    @SerializedName("weight") val weight: Int,
    @SerializedName("sprites") val sprites: SpritesDto?,
    @SerializedName("types") val types: List<PokemonTypeEntryDto>?,
    @SerializedName("stats") val stats: List<PokemonStatDto>?,
    @SerializedName("abilities") val abilities: List<PokemonAbilityDto>?
) {
    fun getBestImageUrl(): String {
        return sprites?.other?.officialArtwork?.frontDefault
            ?: sprites?.frontDefault
            ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    }
}

data class SpritesDto(
    @SerializedName("front_default") val frontDefault: String?,
    @SerializedName("other") val other: OtherSpritesDto?
)

data class OtherSpritesDto(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtworkDto?
)

data class OfficialArtworkDto(
    @SerializedName("front_default") val frontDefault: String?
)

data class PokemonTypeEntryDto(
    @SerializedName("slot") val slot: Int?,
    @SerializedName("type") val type: NamedApiResourceDto?
)

data class PokemonStatDto(
    @SerializedName("base_stat") val baseStat: Int,
    @SerializedName("effort") val effort: Int?,
    @SerializedName("stat") val stat: NamedApiResourceDto?
)

data class PokemonAbilityDto(
    @SerializedName("ability") val ability: NamedApiResourceDto?,
    @SerializedName("is_hidden") val isHidden: Boolean?,
    @SerializedName("slot") val slot: Int?
)
