package com.google.firebase.example.dataconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.example.dataconnect.feature.genredetail.genreDetailScreen
import com.google.firebase.example.dataconnect.feature.genredetail.navigateToGenreDetail
import com.google.firebase.example.dataconnect.feature.genres.GENRES_ROUTE
import com.google.firebase.example.dataconnect.feature.genres.genresScreen
import com.google.firebase.example.dataconnect.feature.genres.navigateToGenres
import com.google.firebase.example.dataconnect.feature.moviedetail.movieDetailScreen
import com.google.firebase.example.dataconnect.feature.moviedetail.navigateToMovieDetail
import com.google.firebase.example.dataconnect.feature.movies.MOVIES_ROUTE
import com.google.firebase.example.dataconnect.feature.movies.moviesScreen
import com.google.firebase.example.dataconnect.feature.movies.navigateToMovies
import com.google.firebase.example.dataconnect.feature.profile.PROFILE_ROUTE
import com.google.firebase.example.dataconnect.feature.profile.navigateToProfile
import com.google.firebase.example.dataconnect.feature.profile.profileScreen
import com.google.firebase.example.dataconnect.feature.search.SEARCH_ROUTE
import com.google.firebase.example.dataconnect.feature.search.navigateToSearch
import com.google.firebase.example.dataconnect.feature.search.searchScreen
import com.google.firebase.example.dataconnect.ui.theme.FirebaseDataConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirebaseDataConnectTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                                label = { Text(stringResource(R.string.label_movies)) },
                                selected = isRouteSelected(currentDestination, MOVIES_ROUTE),
                                onClick = {
                                    navController.navigateToMovies { launchSingleTop = true }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Menu, contentDescription = null) },
                                label = { Text(stringResource(R.string.label_genres)) },
                                selected = isRouteSelected(currentDestination, GENRES_ROUTE),
                                onClick = {
                                    navController.navigateToGenres { launchSingleTop = true }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                label = { Text(stringResource(R.string.label_search)) },
                                selected = isRouteSelected(currentDestination, SEARCH_ROUTE),
                                onClick = {
                                    navController.navigateToSearch { launchSingleTop = true }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                label = { Text(stringResource(R.string.label_profile)) },
                                selected = isRouteSelected(currentDestination, PROFILE_ROUTE),
                                onClick = {
                                    navController.navigateToProfile { launchSingleTop = true }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = MOVIES_ROUTE,
                        Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                    ) {
                        moviesScreen(onMovieClicked = { movieId ->
                            navController.navigateToMovieDetail(movieId) {
                                launchSingleTop = true
                            }
                        })
                        movieDetailScreen()
                        genresScreen(onGenreClicked = { genre ->
                            navController.navigateToGenreDetail(genre) {
                                launchSingleTop = true
                            }
                        })
                        genreDetailScreen()
                        searchScreen()
                        profileScreen()
                    }
                }
            }
        }
    }
}

private fun isRouteSelected(currentDestination: NavDestination?, route: String) =
    currentDestination?.hierarchy?.any { it.route?.startsWith(route) ?: false } == true
