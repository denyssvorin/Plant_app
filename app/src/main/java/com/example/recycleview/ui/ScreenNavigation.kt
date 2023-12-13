package com.example.recycleview.ui

sealed class ScreenNavigation(val route: String) {
    data object HomeScreen : ScreenNavigation("home_screen")
    data object EditScreen : ScreenNavigation("edit_screen")
    data object AddNewPlantScreen : ScreenNavigation("add_new_plant_screen")
    data object DetailsScreen : ScreenNavigation("details_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}