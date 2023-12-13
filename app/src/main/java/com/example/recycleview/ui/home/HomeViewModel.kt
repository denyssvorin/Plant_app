package com.example.recycleview.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.recycleview.data.Plant
import com.example.recycleview.repo.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PlantRepository
): ViewModel() {

    val searchQuery = MutableLiveData("")

    val plantPagingFlow: Flow<PagingData<Plant>> = searchQuery.asFlow()
        .flatMapLatest {
            repository.getPagingPlants(it)
        }.cachedIn(viewModelScope)
}