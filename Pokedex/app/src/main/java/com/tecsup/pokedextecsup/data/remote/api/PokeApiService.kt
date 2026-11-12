package com.tecsup.pokedextecsup.data.remote.api

import com.tecsup.pokedextecsup.data.remote.dto.FireTypeResponseDto
import com.tecsup.pokedextecsup.data.remote.dto.PokemonDetailDto
import retrofit2.http.GET
import retrofit2.http.Path

interface PokeApiService {

    @GET("type/fire")
    suspend fun getFireTypePokemon(): FireTypeResponseDto

    @GET("pokemon/{name}")
    suspend fun getPokemonDetail(
        @Path("name") name: String
    ): PokemonDetailDto
}
