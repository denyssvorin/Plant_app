package com.example.recycleview.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.recycleview.data.datastore.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    fun getPlants(
        searchText: String, sortOrder: SortOrder,
        limit: Int, offset: Int
    ): List<Plant> =
        when(sortOrder) {
            SortOrder.A2Z -> getPlantsSortedA2Z(searchText, limit, offset)
            SortOrder.Z2A -> getPlantsSortedZ2A(searchText, limit, offset)
        }
    @Query("SELECT * FROM plant_table WHERE plantName LIKE '%' || :searchText || '%' " +
            "ORDER BY plantName ASC " +
            "LIMIT :limit " +
            "OFFSET :offset")
    fun getPlantsSortedA2Z(searchText: String, limit: Int, offset: Int): List<Plant>
    @Query("SELECT * FROM plant_table WHERE plantName LIKE '%' || :searchText || '%' " +
            "ORDER BY plantName DESC " +
            "LIMIT :limit " +
            "OFFSET :offset")
    fun getPlantsSortedZ2A(searchText: String, limit: Int, offset: Int): List<Plant>

    @Query("SELECT * FROM plant_table WHERE plantId LIKE '%' || :id || '%'")
    fun getSinglePlant(id: Int): Flow<Plant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlant(plant: Plant)

    @Delete
    fun deletePlant(plant: Plant)
}