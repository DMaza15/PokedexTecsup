package com.tecsup.pokedextecsup.domain.model

data class UserModel(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val rol: String = "USER"
)
