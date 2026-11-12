package com.tecsup.pokedextecsup.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Catalog : Screen("catalog")
    object Detail : Screen("detail/{pokemonName}") {
        fun createRoute(pokemonName: String) = "detail/$pokemonName"
    }
    object Favorites : Screen("favorites")
    object Admin : Screen("admin")
}
