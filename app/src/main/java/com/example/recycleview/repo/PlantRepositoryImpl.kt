package com.example.recycleview.repo

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.recycleview.data.Plant
import com.example.recycleview.data.PlantDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlantRepositoryImpl @Inject constructor(
    private val context: Application,
    private val db: PlantDatabase
) : PlantRepository {
    override suspend fun mapPhotosFromExternalStorage(imagePath: String): String {
        return withContext(Dispatchers.IO) {
            val numericPart = imagePath.replace(Regex("[^0-9]"), "")
            var result = "null"

            val projection = arrayOf(
                MediaStore.Images.Media._ID
            )

            val selection = "${MediaStore.Images.Media._ID} = ?"
            val selectionArgs = arrayOf(numericPart)
            Log.d("TAG", "EditPlantFragment: selectionArgs: $selectionArgs")

            context.contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(idColumn)
                    val currentContentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    result = currentContentUri.toString()

                } else {
                    Log.e("TAG", "mapPhotosFromExternalStorage: error")
                }
                result
            } ?: "null"
        }
    }

    override fun getPagingPlants(searchQuery: String): Flow<PagingData<Plant>> {
        val dbLoader: PlantReposDBPageLoader = { limit, offset ->
            getPlants(limit, offset, searchQuery)
        }
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                PlantPagingSource(
                    dbLoader,
                    PAGE_SIZE
                )
            }
        ).flow
    }

    private suspend fun getPlants(limit: Int, offset: Int, searchQuery: String) : List<Plant> =
        withContext(Dispatchers.IO) {
            val list = db.plantDao().getPlants(limit = limit, offset = offset, searchText = searchQuery)
            return@withContext list
        }

    companion object {
        const val PAGE_SIZE = 20
    }
}