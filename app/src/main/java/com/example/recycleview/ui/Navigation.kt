package com.example.recycleview.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recycleview.ui.add.AddNewPlantScreen
import com.example.recycleview.ui.details.DetailsScreen
import com.example.recycleview.ui.edit.EditScreen
import com.example.recycleview.ui.home.HomeScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ScreenNavigation.HomeScreen.route
    ) {
        composable(route = ScreenNavigation.HomeScreen.route) {
            HomeScreen(navController = navController)
        }
        composable(
            route = ScreenNavigation.EditScreen.route + "/{plant_id}",
            arguments = listOf(
                navArgument("plant_id") {
                    type = NavType.IntType
                }
            )
        ) { navBackStackEntry ->
            navBackStackEntry.arguments?.getInt("plant_id")?.let {
                EditScreen(plantId = it, navController = navController)
            }
        }
        composable(
            route = ScreenNavigation.AddNewPlantScreen.route
        ) {
            AddNewPlantScreen(navController = navController)
        }
        composable(
            route = ScreenNavigation.DetailsScreen.route + "/{plant_id}",
            arguments = listOf(
                navArgument("plant_id") {
                    type = NavType.IntType
                }
            )
        ) { navBackStackEntry ->
            navBackStackEntry.arguments?.getInt("plant_id")?.let {
                DetailsScreen(plantId = it, navController = navController)
            }
        }
    }
}