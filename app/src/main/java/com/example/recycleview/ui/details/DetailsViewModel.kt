package com.example.recycleview.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recycleview.data.Plant
import com.example.recycleview.data.PlantDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val plantDao: PlantDao
) : ViewModel() {

    fun getPlant(id: Int) = viewModelScope.launch {
        val plant = plantDao.getSinglePlant(id).first()
        _plantData.value = plant
    }

    private val _plantData = MutableStateFlow<Plant?>(null)
    val plantData: StateFlow<Plant?> = _plantData.asStateFlow()

}