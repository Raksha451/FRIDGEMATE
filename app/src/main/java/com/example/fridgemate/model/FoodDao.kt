package com.example.fridgemate.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_items")
    fun getAllItems(): Flow<List<FoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: FoodItem)

    @Delete
    suspend fun deleteItem(item: FoodItem)
}
