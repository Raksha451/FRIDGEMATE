package com.example.fridgemate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fridgemate.model.AppDatabase
import com.example.fridgemate.model.FoodItem
import com.example.fridgemate.ui.AddItemDialog
import com.example.fridgemate.ui.InventoryScreen
import com.example.fridgemate.ui.LandingScreen
import com.example.fridgemate.ui.ScanFoodScreen
import com.example.fridgemate.ui.theme.FRIDGEMATETheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val foodDao = database.foodDao()

        setContent {
            FRIDGEMATETheme {
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()
                var showAddItemDialog by remember { mutableStateOf(false) }
                
                // Collect food items from the database as a state
                val foodItems by foodDao.getAllItems().collectAsState(initial = emptyList())

                NavHost(navController = navController, startDestination = "landing") {
                    composable("landing") {
                        LandingScreen(onGetStarted = {
                            navController.navigate("inventory")
                        })
                    }
                    composable("inventory") {
                        InventoryScreen(
                            foodItems = foodItems,
                            onScanFood = { navController.navigate("scan") },
                            onAddItem = { showAddItemDialog = true },
                            onSettings = { /* Handle settings */ },
                            onDeleteItem = { item -> 
                                coroutineScope.launch {
                                    foodDao.deleteItem(item)
                                }
                            }
                        )
                    }
                    composable("scan") {
                        ScanFoodScreen(
                            onClose = { navController.popBackStack() },
                            onDetected = { item ->
                                coroutineScope.launch {
                                    foodDao.insertItem(item)
                                }
                                navController.popBackStack()
                            }
                        )
                    }
                }

                if (showAddItemDialog) {
                    AddItemDialog(
                        onDismiss = { showAddItemDialog = false },
                        onSave = { name, category, expiry ->
                            coroutineScope.launch {
                                try {
                                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                    val expiryDate = try {
                                        LocalDate.parse(expiry, formatter)
                                    } catch (e: Exception) {
                                        LocalDate.now().plusDays(7)
                                    }
                                    val newItem = FoodItem(
                                        id = UUID.randomUUID().toString(),
                                        name = name,
                                        category = category,
                                        addedDate = LocalDate.now(),
                                        expiryDate = expiryDate
                                    )
                                    foodDao.insertItem(newItem)
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }
                            showAddItemDialog = false
                        }
                    )
                }
            }
        }
    }
}
