package com.example.fridgemate.model

import android.content.Context
import androidx.room.*

@Database(entities = [FoodItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fridgemate_database"
                )
                .fallbackToDestructiveMigration() // Ensures the app launches even if schema changes during development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
