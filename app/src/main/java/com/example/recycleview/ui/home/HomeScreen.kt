package com.example.recycleview.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.recycleview.R
import com.example.recycleview.ui.ScreenNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {

    LaunchedEffect(navController) {
        viewModel.searchQuery.postValue("")
    }
    val plantList = viewModel.plantPagingFlow.collectAsLazyPagingItems()
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.main))
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Heloo", Toast.LENGTH_SHORT).show()
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = stringResource(R.string.search)
                                )
                            })
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        ScreenNavigation.AddNewPlantScreen.route
                    )
                },
                content = {
                    Icon(Icons.Filled.Add, "Floating action button.")
                }
            )
        },
        content = { padding ->
            Surface(modifier = Modifier.padding(padding)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(plantList.itemCount) { index ->
                        PlantItem(
                            plant = plantList[index],
                            navController = navController
                        )
                    }
                }
            }
        }
    )
}
