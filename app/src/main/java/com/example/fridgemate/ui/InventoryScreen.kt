package com.example.fridgemate.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgemate.R
import com.example.fridgemate.model.FoodItem
import com.example.fridgemate.ui.theme.*
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.LocalDate

@Composable
fun InventoryScreen(
    foodItems: List<FoodItem>,
    onScanFood: () -> Unit,
    onAddItem: () -> Unit,
    onSettings: () -> Unit,
    onDeleteItem: (FoodItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 for Inventory, 1 for Recipes
    var selectedCategory by remember { mutableStateOf("All") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<FoodItem?>(null) }
    var showExpiryAlert by remember { mutableStateOf(true) }

    val today = remember { LocalDate.now() }

    val categories = listOf("All", "Vegetable", "Fruit", "Bakery", "Dairy", "Meat", "Other")

    val filteredItems = remember(foodItems, searchQuery, selectedCategory) {
        foodItems.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // Header
        InventoryHeader(onSettings = onSettings)

        // Expiry Alert
        val expiringSoon = remember(foodItems, today) {
            foodItems.filter { 
                val days = ChronoUnit.DAYS.between(today, it.expiryDate)
                days in 0L..2L 
            }
        }
        
        if (expiringSoon.isNotEmpty() && showExpiryAlert) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                color = AlertBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error, 
                        contentDescription = null, 
                        tint = AlertRed, 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Attention: Food expires within 2 days: ${expiringSoon.joinToString { it.name }}",
                        color = AlertRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showExpiryAlert = false }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = AlertRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Tabs
        Surface(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            color = Color(0xFFF3F4F6),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InventoryTabButton(text = "Inventory", icon = Icons.Default.GridView, isSelected = selectedTab == 0) { selectedTab = 0 }
                InventoryTabButton(text = "Recipes", icon = Icons.Default.RestaurantMenu, isSelected = selectedTab == 1) { selectedTab = 1 }
            }
        }

        if (selectedTab == 0) {
            // Search and Filter Row - Adjusted for better spacing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search...", fontSize = 13.sp, color = Color.LightGray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Category Dropdown
                Box {
                    Surface(
                        modifier = Modifier
                            .height(52.dp)
                            .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { isDropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedCategory, color = TextDark, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = onAddItem,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(52.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Add Item", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Grid of Items
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), 
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    InventoryFoodCard(item, today = today, onDelete = { itemToDelete = item })
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                InventoryRecipeSuggestions(foodItems = foodItems, today = today)
            }
        }

        // Scan Food Button - Floating at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onScanFood,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .height(52.dp)
                    .padding(horizontal = 24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Food", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to remove ${itemToDelete?.name} from your fridge?") },
            confirmButton = {
                TextButton(onClick = {
                    itemToDelete?.let { onDeleteItem(it) }
                    itemToDelete = null
                }) {
                    Text("Delete", color = AlertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InventoryFoodCard(item: FoodItem, today: LocalDate, onDelete: () -> Unit) {
    val daysLeft = ChronoUnit.DAYS.between(today, item.expiryDate)
    val isExpired = daysLeft < 0
    
    // Status color and text
    val pillBgColor = if (isExpired) Color(0xFFFFEBEE) else LightGreen
    val pillTextColor = if (isExpired) Color(0xFFEF4444) else PrimaryGreen
    val pillText = if (isExpired) "EXPIRED" else "IN ${daysLeft}D"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), // Increased height to prevent truncation
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    color = LightGreen,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Inventory2, 
                        contentDescription = null, 
                        tint = PrimaryGreen, 
                        modifier = Modifier.padding(8.dp)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete", 
                        tint = Color.LightGray.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = item.name.replaceFirstChar { it.uppercase() }, 
                fontWeight = FontWeight.Bold, 
                color = TextDark,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.category, 
                color = Color.Gray, 
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Fixed Date Display to prevent "lagging" (truncation)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Added", 
                    color = Color.Gray, 
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday, 
                        contentDescription = null, 
                        tint = Color.Gray, 
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        item.addedDate.format(DateTimeFormatter.ofPattern("M/d/yyyy")), 
                        color = Color.Gray, 
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Status Pill positioned at bottom right
                Surface(
                    color = pillBgColor,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = pillText,
                        color = pillTextColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryHeader(onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.fridgemate_logo),
                contentDescription = "FridgeMate Logo",
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("FridgeMate", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextDark)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = TextDark, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Settings", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun InventoryTabButton(text: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (isSelected) TextDark else TextSecondary, 
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text, 
                color = if (isSelected) TextDark else TextSecondary, 
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

data class InventoryDetailedRecipe(
    val title: String,
    val description: String,
    val priorityIngredients: List<String>,
    val prepTime: String,
    val cookTime: String,
    val instructions: List<String>
)

data class InventoryChefTip(val ingredient: String, val tip: String)

@Composable
fun InventoryRecipeSuggestions(foodItems: List<FoodItem>, today: LocalDate) {
    var isGenerating by remember { mutableStateOf(false) }
    var recipes by remember { mutableStateOf<List<InventoryDetailedRecipe>>(emptyList()) }
    var chefTips by remember { mutableStateOf<List<InventoryChefTip>>(emptyList()) }
    var hasGenerated by remember { mutableStateOf(false) }

    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            delay(1500)
            val result = generateInventoryDetailedRecipes(foodItems, today)
            recipes = result.first
            chefTips = result.second
            isGenerating = false
            hasGenerated = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("AI Recipe Suggestions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        }
                        Text("Based on items expiring soon", color = Color.Gray, fontSize = 14.sp)
                    }

                    Button(
                        onClick = { isGenerating = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isGenerating && foodItems.any { ChronoUnit.DAYS.between(today, it.expiryDate) in 0L..2L }
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .background(Color(0xFF1F2937).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Analyzing ingredients...", color = Color.LightGray)
                        }
                    } else if (!hasGenerated) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Click generate to see recipes for items expiring within 2 days!", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    } else if (recipes.isEmpty()) {
                        Text("No items expiring within 2 days. You're doing great at managing waste!", color = Color.Gray, textAlign = TextAlign.Center)
                    } else {
                        Column {
                            recipes.forEachIndexed { index, recipe ->
                                InventoryDetailedRecipeItem(index + 1, recipe)
                                if (index < recipes.size - 1 || chefTips.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                            
                            if (chefTips.isNotEmpty()) {
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(24.dp))
                                InventoryChefsTipSection(chefTips)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun InventoryDetailedRecipeItem(index: Int, recipe: InventoryDetailedRecipe) {
    Column {
        Text(
            text = "$index. ${recipe.title}",
            color = PrimaryGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = recipe.description,
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Priority Ingredients
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(PrimaryGreen, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.Bold)) {
                        append("Priority Ingredients: ")
                    }
                    append(recipe.priorityIngredients.joinToString(", "))
                },
                color = Color.White,
                fontSize = 13.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Prep/Cook Time
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(PrimaryGreen, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.Bold)) {
                        append("Prep time: ")
                    }
                    append("${recipe.prepTime} | ")
                    withStyle(SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.Bold)) {
                        append("Cook time: ")
                    }
                    append(recipe.cookTime)
                },
                color = Color.White,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Instructions:", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        recipe.instructions.forEachIndexed { i, step ->
            val annotatedStep = buildAnnotatedString {
                val parts = step.split(":", limit = 2)
                if (parts.size == 2) {
                    withStyle(SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.Bold)) {
                        append("${i + 1}. ${parts[0]}:")
                    }
                    append(parts[1])
                } else {
                    append("${i + 1}. $step")
                }
            }
            Text(
                text = annotatedStep,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun InventoryChefsTipSection(tips: List<InventoryChefTip>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("💡", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Chef's Tip for Food Waste:",
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        tips.forEach { tip ->
            Row(modifier = Modifier.padding(bottom = 12.dp)) {
                Text("•", color = Color.Gray, modifier = Modifier.padding(end = 12.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.Bold)) {
                            append("${tip.ingredient}: ")
                        }
                        append(tip.tip)
                    },
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

fun generateInventoryDetailedRecipes(items: List<FoodItem>, today: LocalDate): Pair<List<InventoryDetailedRecipe>, List<InventoryChefTip>> {
    val expiringSoon = items.filter { 
        val days = ChronoUnit.DAYS.between(today, it.expiryDate)
        days in 0L..2L 
    }
    
    if (expiringSoon.isEmpty()) return Pair(emptyList(), emptyList())

    val recipes = mutableListOf<InventoryDetailedRecipe>()
    val tips = mutableListOf<InventoryChefTip>()
    val formatter = DateTimeFormatter.ofPattern("MM-dd")
    
    // Generate 2-3 recipes using expiring items
    val recipeCount = 3
    for (i in 0 until recipeCount) {
        val priorityItem = expiringSoon[i % expiringSoon.size]
        val accompaniments = items.filter { it.id != priorityItem.id }.shuffled().take(2)
        val allIncluded = listOf(priorityItem) + accompaniments
        
        val title = when(i) {
            0 -> "Savory ${priorityItem.name} & Aromatic Medley"
            1 -> "Quick ${priorityItem.name} Stir-fry"
            else -> "Healthy ${priorityItem.name} Harvest Bowl"
        }

        recipes.add(InventoryDetailedRecipe(
            title = title,
            description = "A chef-inspired dish focusing on using your ${priorityItem.name} which expires soon (${priorityItem.expiryDate.format(formatter)}).",
            priorityIngredients = allIncluded.map { "${it.name} (expires ${it.expiryDate.format(formatter)})" },
            prepTime = "${10 + i * 2} minutes",
            cookTime = "${15 + i * 5} minutes",
            instructions = listOf(
                "Prep: Clean and roughly chop the ${priorityItem.name}. If using ${accompaniments.firstOrNull()?.name ?: "aromatics"}, prep them accordingly.",
                "Sauté/Cook: In a pan with a splash of oil, cook the ${priorityItem.name} over medium heat until it develops a nice texture.",
                "Combine: Add your ${accompaniments.joinToString(", ") { it.name }}. Season with salt, pepper, and herbs.",
                "Finish: Cover and let flavors meld for a few minutes. Serve warm."
            )
        ))
    }

    // Chef's Tips only for expiring items
    expiringSoon.forEach { item ->
        when {
            item.name.contains("Spinach", true) -> {
                if (tips.none { it.ingredient == "Spinach" }) {
                    tips.add(InventoryChefTip("Spinach", "If you notice your spinach is wilting faster than you can eat it, blanch and freeze it. Drop it in boiling water for 30 seconds, then ice water, squeeze out all the moisture, and freeze in small balls. You can drop these into soups or smoothies later!"))
                }
            }
            item.name.contains("Garlic", true) -> {
                if (tips.none { it.ingredient == "Garlic" }) {
                    tips.add(InventoryChefTip("Garlic", "Since your garlic has a long shelf life, use it generously in these recipes to add depth of flavor since the ingredient list is short!"))
                }
            }
            item.category.contains("Bakery", true) -> {
                if (tips.none { it.ingredient == item.name }) {
                    tips.add(InventoryChefTip(item.name, "Revive stale bread by lightly misting it with water and popping it in a 350°F oven for 5 minutes."))
                }
            }
            item.category.contains("Veg", true) && tips.none { it.ingredient == item.name } -> {
                tips.add(InventoryChefTip(item.name, "Store your ${item.name} in a perforated bag in the crisper drawer to maintain optimal humidity and double its shelf life."))
            }
            else -> {
                if (tips.none { it.ingredient == item.name }) {
                    tips.add(InventoryChefTip(item.name, "Use this item as the star of your meal today to ensure zero waste!"))
                }
            }
        }
    }

    return Pair(recipes, tips.take(3))
}

private fun offset(x: Float, y: Float) = androidx.compose.ui.geometry.Offset(x, y)
