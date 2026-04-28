package com.example.fridgemate.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val addedDate: LocalDate,
    val expiryDate: LocalDate
)
