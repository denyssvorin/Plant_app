package com.example.recycleview.ui.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recycleview.data.Plant
import com.example.recycleview.data.PlantDao
import com.example.recycleview.repo.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditPlantViewModel @Inject constructor(
    private val plantDao: PlantDao,
    private val repository: PlantRepository
) : ViewModel() {

    private val _plantImageData = MutableStateFlow("")
    val plantImageData: StateFlow<String> = _plantImageData.asStateFlow()

    fun getPlant(id: Int) {
        if (_plantImageData.value == ""
            && plantName == ""
            && plantDescription == ""
        ) {
            viewModelScope.launch {
                val plant = plantDao.getSinglePlant(id).first()

                _plantImageData.value = plant.plantImagePath
                plantName = plant.plantName
                plantDescription = plant.plantDescription
            }
        }
    }

    fun savePlant(plant: Plant) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                plantDao.upsertPlant(plant)
            }
        }
    }

    var plantName by mutableStateOf("")
        private set
    var plantDescription by mutableStateOf("")
        private set

    fun updatePlantNameTextField(text: String) {
        plantName = text
    }

    fun updatePlantDescriptionTextField(text: String) {
        plantDescription = text
    }

    private val _mappedPhotos = MutableStateFlow<String?>(null)
    val mappedPhotos: StateFlow<String?> = _mappedPhotos

    fun mapPhotos(imagePath: String) = viewModelScope.launch {
        val photos = repository.mapPhotosFromExternalStorage(imagePath)
        _mappedPhotos.value = photos
    }
}