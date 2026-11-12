package com.tecsup.pokedextecsup.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tecsup.pokedextecsup.data.local.database.AppDatabase
import com.tecsup.pokedextecsup.data.repository.AuthRepository
import com.tecsup.pokedextecsup.data.repository.PokemonRepository
import com.tecsup.pokedextecsup.ui.screens.admin.AdminPanelScreen
import com.tecsup.pokedextecsup.ui.screens.catalog.CatalogScreen
import com.tecsup.pokedextecsup.ui.screens.detail.DetailScreen
import com.tecsup.pokedextecsup.ui.screens.favorites.FavoritesScreen
import com.tecsup.pokedextecsup.ui.screens.login.LoginScreen
import com.tecsup.pokedextecsup.ui.screens.register.RegisterScreen
import com.tecsup.pokedextecsup.viewmodel.AdminViewModel
import com.tecsup.pokedextecsup.viewmodel.AuthViewModel
import com.tecsup.pokedextecsup.viewmodel.CatalogViewModel
import com.tecsup.pokedextecsup.viewmodel.DetailViewModel
import com.tecsup.pokedextecsup.viewmodel.FavoritesViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Catalog.route
) {
    val context = LocalContext.current.applicationContext
    val pokemonRepository = remember {
        val database = AppDatabase.getInstance(context)
        PokemonRepository(pokemonDao = database.pokemonDao())
    }
    val authRepository = remember { AuthRepository() }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(repository = authRepository))

    val userEmail by authViewModel.currentUserEmail.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val isLoggedIn = authViewModel.isUserLoggedIn()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToRegister = {
                    authViewModel.clearState()
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { role ->
                    authViewModel.clearState()
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Catalog.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    authViewModel.clearState()
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Catalog.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Catalog.route) {
            val catalogViewModel: CatalogViewModel = viewModel(
                factory = CatalogViewModel.Factory(repository = pokemonRepository)
            )
            CatalogScreen(
                viewModel = catalogViewModel,
                isLoggedIn = isLoggedIn,
                userEmail = userEmail,
                userName = userName,
                userRole = userRole,
                onPokemonClick = { pokemonName ->
                    navController.navigate(Screen.Detail.createRoute(pokemonName))
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                },
                onNavigateToLogin = {
                    authViewModel.clearState()
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.Admin.route)
                },
                onLogout = {
                    authViewModel.logout()
                }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("pokemonName") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("pokemonName") ?: ""
            val detailViewModel: DetailViewModel = viewModel(
                factory = DetailViewModel.Factory(pokemonName = name, repository = pokemonRepository)
            )
            DetailScreen(
                viewModel = detailViewModel,
                onBackClick = { navController.popBackStack() },
                onToggleFavorite = { detail ->
                    detailViewModel.toggleFavorite(detail)
                }
            )
        }
        composable(Screen.Favorites.route) {
            val favoritesViewModel: FavoritesViewModel = viewModel(
                factory = FavoritesViewModel.Factory(repository = pokemonRepository)
            )
            FavoritesScreen(
                viewModel = favoritesViewModel,
                onBackClick = { navController.popBackStack() },
                onPokemonClick = { pokemonName ->
                    navController.navigate(Screen.Detail.createRoute(pokemonName))
                },
                onNavigateToCatalog = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Catalog.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Admin.route) {
            // Protected Route: Check if user has ADMIN role from Firestore
            if (isLoggedIn && userRole == "ADMIN") {
                val adminViewModel: AdminViewModel = viewModel(
                    factory = AdminViewModel.Factory(repository = authRepository)
                )
                AdminPanelScreen(
                    viewModel = adminViewModel,
                    adminEmail = userEmail,
                    adminName = userName,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                AccessDeniedScreen(
                    onBackClick = { navController.popBackStack() },
                    onLoginClick = {
                        authViewModel.clearState()
                        navController.navigate(Screen.Login.route)
                    }
                )
            }
        }
    }
}

@Composable
fun AccessDeniedScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Bloqueado",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Acceso Denegado",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Solo los usuarios con rol ADMINISTRADOR en Firestore pueden acceder a este panel.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBackClick) {
                Text("Volver")
            }
            Button(onClick = onLoginClick) {
                Text("Iniciar Sesión")
            }
        }
    }
}
