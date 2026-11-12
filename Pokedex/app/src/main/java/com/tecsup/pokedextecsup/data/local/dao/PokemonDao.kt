package com.tecsup.pokedextecsup.data.local.dao

import androidx.room.*
import com.tecsup.pokedextecsup.data.local.entity.PokemonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Query("SELECT * FROM favorite_pokemon ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<PokemonEntity>>

    @Query("SELECT * FROM favorite_pokemon WHERE id = :id")
    suspend fun getFavoriteById(id: Int): PokemonEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_pokemon WHERE id = :id)")
    fun isFavorite(id: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(pokemon: PokemonEntity)

    @Delete
    suspend fun deleteFavorite(pokemon: PokemonEntity)

    @Query("DELETE FROM favorite_pokemon WHERE id = :id")
    suspend fun deleteFavoriteById(id: Int)
}
