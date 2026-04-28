package com.example.fridgemate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fridgemate.ui.theme.PrimaryGreen
import com.example.fridgemate.ui.theme.TextDark
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Vegetable") }
    
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }
    var expiryDateString by remember { mutableStateOf(LocalDate.of(2026, 4, 16).format(dateFormatter)) }
    
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val categories = listOf("Vegetable", "Fruit", "Bakery", "Dairy", "Meat", "Other")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Item",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Item Name
                AddItemFieldLabel(icon = Icons.Default.Inventory, label = "Item Name")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Fresh Spinach", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Dropdown
                AddItemFieldLabel(icon = Icons.AutoMirrored.Filled.Label, label = "Category")
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                null,
                                Modifier.clickable { expanded = true }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = PrimaryGreen
                        )
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .background(Color.White)
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(text = selectionOption) },
                                onClick = {
                                    category = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Expiry Date
                AddItemFieldLabel(icon = Icons.Default.CalendarToday, label = "Expiry Date")
                OutlinedTextField(
                    value = expiryDateString,
                    onValueChange = { expiryDateString = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { showDatePicker = true }
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Save Button
                Button(
                    onClick = { onSave(name, category, expiryDateString) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Save Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val initialDate = try {
            LocalDate.parse(expiryDateString, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
        
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        expiryDateString = selectedDate.format(dateFormatter)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun AddItemFieldLabel(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.DarkGray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.DarkGray, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
