package com.tecsup.pokedextecsup.data.repository

import com.tecsup.pokedextecsup.data.local.dao.PokemonDao
import com.tecsup.pokedextecsup.data.local.entity.PokemonEntity
import com.tecsup.pokedextecsup.data.remote.api.PokeApiService
import com.tecsup.pokedextecsup.data.remote.api.RetrofitClient
import com.tecsup.pokedextecsup.domain.model.PokemonDetail
import com.tecsup.pokedextecsup.domain.model.PokemonItem
import com.tecsup.pokedextecsup.domain.model.StatModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PokemonRepository(
    private val apiService: PokeApiService = RetrofitClient.apiService,
    private val pokemonDao: PokemonDao? = null
) {
    val favorites: Flow<List<PokemonItem>> =
        pokemonDao?.getAllFavorites()?.map { list ->
            list.map { it.toPokemonItem() }
        } ?: flowOf(emptyList())

    fun isFavorite(id: Int): Flow<Boolean> {
        return pokemonDao?.isFavorite(id) ?: flowOf(false)
    }

    suspend fun addFavorite(item: PokemonItem) = withContext(Dispatchers.IO) {
        val entity = PokemonEntity(
            id = item.id,
            name = item.name,
            imageUrl = item.imageUrl
        )
        pokemonDao?.insertFavorite(entity)
    }

    suspend fun addFavoriteFromDetail(detail: PokemonDetail) = withContext(Dispatchers.IO) {
        val entity = PokemonEntity(
            id = detail.id,
            name = detail.name,
            imageUrl = detail.imageUrl,
            types = detail.types.joinToString(","),
            height = detail.height,
            weight = detail.weight
        )
        pokemonDao?.insertFavorite(entity)
    }

    suspend fun removeFavorite(id: Int) = withContext(Dispatchers.IO) {
        pokemonDao?.deleteFavoriteById(id)
    }

    suspend fun getFirePokemonList(): Result<List<PokemonItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFireTypePokemon()
            val items = response.pokemon?.mapNotNull { entry ->
                entry.pokemon?.let { res ->
                    val id = res.extractId()
                    PokemonItem(
                        id = id,
                        name = res.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        imageUrl = res.getImageUrl()
                    )
                }
            } ?: emptyList()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPokemonDetail(name: String): Result<PokemonDetail> = withContext(Dispatchers.IO) {
        try {
            val dto = apiService.getPokemonDetail(name.lowercase())
            val isFav = pokemonDao?.getFavoriteById(dto.id) != null
            val detail = PokemonDetail(
                id = dto.id,
                name = dto.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                height = dto.height,
                weight = dto.weight,
                imageUrl = dto.getBestImageUrl(),
                types = dto.types?.mapNotNull { it.type?.name?.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } } ?: emptyList(),
                stats = dto.stats?.mapNotNull {
                    it.stat?.name?.let { statName ->
                        StatModel(
                            name = formatStatName(statName),
                            value = it.baseStat
                        )
                    }
                } ?: emptyList(),
                abilities = dto.abilities?.mapNotNull { it.ability?.name?.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } } ?: emptyList(),
                isFavorite = isFav
            )
            Result.success(detail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatStatName(statName: String): String {
        return when (statName.lowercase()) {
            "hp" -> "HP"
            "attack" -> "Ataque"
            "defense" -> "Defensa"
            "special-attack" -> "At. Esp."
            "special-defense" -> "Def. Esp."
            "speed" -> "Velocidad"
            else -> statName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
