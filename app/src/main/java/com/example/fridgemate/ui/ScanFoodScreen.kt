package com.example.fridgemate.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.fridgemate.model.FoodItem
import com.example.fridgemate.ui.theme.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanFoodScreen(onClose: () -> Unit, onDetected: (FoodItem) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Ensure executor is shut down when the screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var isProcessing by remember { mutableStateOf(false) }
    var detectedItem by remember { mutableStateOf<FoodItem?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    val labeler = remember { 
        // Use a slightly lower confidence threshold to catch more specific food items
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.4f)
            .build()
        ImageLabeling.getClient(options) 
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(top = 60.dp, start = 24.dp, end = 24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.DarkGray)
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            imageCapture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (exc: Exception) {
                                Log.e("ScanFoodScreen", "Use case binding failed", exc)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // The Green Brackets overlay
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(240.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        val cornerLength = 40.dp.toPx()
                        val color = PrimaryGreen
                        
                        drawLine(color, offset(0f, 0f), offset(cornerLength, 0f), strokeWidth)
                        drawLine(color, offset(0f, 0f), offset(0f, cornerLength), strokeWidth)
                        drawLine(color, offset(size.width, 0f), offset(size.width - cornerLength, 0f), strokeWidth)
                        drawLine(color, offset(size.width, 0f), offset(size.width, cornerLength), strokeWidth)
                        drawLine(color, offset(0f, size.height), offset(cornerLength, size.height), strokeWidth)
                        drawLine(color, offset(0f, size.height), offset(0f, size.height - cornerLength), strokeWidth)
                        drawLine(color, offset(size.width, size.height), offset(size.width - cornerLength, size.height), strokeWidth)
                        drawLine(color, offset(size.width, size.height), offset(size.width, size.height - cornerLength), strokeWidth)
                    }
                }
            }
        }

        // Bottom Controls Container
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    onClick = onClose,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color(0xFF1F2937)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }
                }

                Surface(
                    onClick = {
                        if (!isProcessing && imageCapture != null) {
                            isProcessing = true
                            imageCapture?.takePicture(
                                cameraExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    @SuppressLint("UnsafeOptInUsageError")
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                            labeler.process(image)
                                                .addOnSuccessListener { labels ->
                                                    Log.d("ScanFoodScreen", "Detected labels: ${labels.joinToString { "${it.text} (${it.confidence})" }}")
                                                    
                                                    // Labels to ignore as they are too generic or not food
                                                    val genericLabels = listOf(
                                                        "food", "cuisine", "dish", "ingredient", "produce", "plant", 
                                                        "vegetable", "fruit", "meat", "bakery", "dairy", "other",
                                                        "tableware", "plate", "table", "indoor", "furniture", "room", 
                                                        "kitchenware", "accessory", "hand", "finger", "person", "fast food"
                                                    )
                                                    
                                                    // Filter for common food-related labels or use the most confident one
                                                    val bestLabel = labels.filter { label ->
                                                        val text = label.text.lowercase()
                                                        // Prefer specific items over generic category names
                                                        !genericLabels.contains(text)
                                                    }.firstOrNull() ?: labels.firstOrNull()

                                                    val detectedName = bestLabel?.text ?: "Unknown Item"
                                                    val category = mapToCategory(detectedName)
                                                    
                                                    detectedItem = FoodItem(
                                                        id = UUID.randomUUID().toString(),
                                                        name = detectedName.replaceFirstChar { it.uppercase() },
                                                        category = category,
                                                        addedDate = LocalDate.now(),
                                                        expiryDate = LocalDate.now().plusDays(getDefaultExpiryDays(category))
                                                    )
                                                    showConfirmationDialog = true
                                                    isProcessing = false
                                                }
                                                .addOnFailureListener {
                                                    isProcessing = false
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else {
                                            imageProxy.close()
                                            isProcessing = false
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("ScanFoodScreen", "Capture failed", exception)
                                        isProcessing = false
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.size(82.dp),
                    shape = CircleShape,
                    color = PrimaryGreen,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Color.White, strokeWidth = 3.dp)
                        } else {
                            Icon(Icons.Default.PhotoCamera, "Capture", tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Point camera at a food item to identify it",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        if (showConfirmationDialog && detectedItem != null) {
            ConfirmationDialog(
                item = detectedItem!!,
                onDismiss = { showConfirmationDialog = false },
                onConfirm = { updatedItem ->
                    onDetected(updatedItem)
                    showConfirmationDialog = false
                    onClose()
                }
            )
        }
    }
}

private fun mapToCategory(label: String): String {
    val l = label.lowercase()
    return when {
        l.contains("vegetable") || l.contains("tomato") || l.contains("onion") || l.contains("garlic") || 
        l.contains("pepper") || l.contains("cucumber") || l.contains("carrot") || l.contains("broccoli") || 
        l.contains("spinach") || l.contains("potato") || l.contains("cabbage") || l.contains("lettuce") ||
        l.contains("legume") || l.contains("bean") || l.contains("corn") || l.contains("mushroom") -> "Vegetable"
        
        l.contains("fruit") || l.contains("apple") || l.contains("banana") || l.contains("orange") || 
        l.contains("grape") || l.contains("strawberry") || l.contains("lemon") || l.contains("berry") || 
        l.contains("mango") || l.contains("pineapple") || l.contains("pear") || l.contains("peach") || 
        l.contains("melon") || l.contains("watermelon") || l.contains("cherry") || l.contains("kiwi") -> "Fruit"
        
        l.contains("bread") || l.contains("cake") || l.contains("pastry") || l.contains("croissant") || 
        l.contains("muffin") || l.contains("bakery") || l.contains("doughnut") || l.contains("toast") ||
        l.contains("cookie") || l.contains("biscuit") || l.contains("bagel") -> "Bakery"
        
        l.contains("milk") || l.contains("cheese") || l.contains("yogurt") || l.contains("butter") || 
        l.contains("dairy") || l.contains("egg") || l.contains("cream") || l.contains("curd") -> "Dairy"
        
        l.contains("meat") || l.contains("chicken") || l.contains("beef") || l.contains("pork") || 
        l.contains("fish") || l.contains("seafood") || l.contains("sausage") || l.contains("steak") || 
        l.contains("ham") || l.contains("bacon") || l.contains("turkey") || l.contains("lamb") || 
        l.contains("duck") || l.contains("shrimp") || l.contains("prawn") || l.contains("crab") -> "Meat"

        else -> "Other"
    }
}

private fun getDefaultExpiryDays(category: String): Long {
    return when (category) {
        "Vegetable" -> 7
        "Fruit" -> 5
        "Bakery" -> 3
        "Dairy" -> 10
        "Meat" -> 3
        else -> 14
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
    item: FoodItem,
    onDismiss: () -> Unit,
    onConfirm: (FoodItem) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    var expiryDate by remember { mutableStateOf(item.expiryDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = listOf("Vegetable", "Fruit", "Bakery", "Dairy", "Meat", "Other")
    var expanded by remember { mutableStateOf(false) }

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
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Confirm AI Detection",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                FieldLabel(icon = Icons.Default.Inventory, label = "Item Name")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                FieldLabel(icon = Icons.AutoMirrored.Filled.Label, label = "Category")
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { expanded = true })
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FieldLabel(icon = Icons.Default.CalendarToday, label = "Expiry Date")
                OutlinedTextField(
                    value = expiryDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, null, Modifier.size(20.dp).clickable { showDatePicker = true })
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        onConfirm(item.copy(name = name, category = category, expiryDate = expiryDate))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Add to Fridge", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        expiryDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun FieldLabel(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.DarkGray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.DarkGray, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

private fun offset(x: Float, y: Float) = androidx.compose.ui.geometry.Offset(x, y)
