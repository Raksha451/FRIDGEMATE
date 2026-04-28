package com.example.fridgemate.ui

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.fridgemate.R
import com.example.fridgemate.ui.theme.BackgroundWhite
import com.example.fridgemate.ui.theme.FRIDGEMATETheme
import com.example.fridgemate.ui.theme.LightGreen
import com.example.fridgemate.ui.theme.PrimaryGreen
import com.example.fridgemate.ui.theme.TextDark
import com.example.fridgemate.ui.theme.TextSecondary
import java.util.concurrent.TimeUnit

// 1. Worker Class definition for background expiration checks
class ExpirationWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val channelId = "fridge_alerts"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        // Create the Channel (Required for Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId, "Fridge Alerts",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("FridgeMate Alert")
            .setContentText("Some items in your fridge are expiring soon!")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon later
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }
}

@Composable
fun LandingScreen(onGetStarted: () -> Unit) {
    val context = LocalContext.current // Context for WorkManager

    val infiniteTransition = rememberInfiniteTransition(label = "logoScale")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, BackgroundWhite, Color(0xFFE8F5E9))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Animated Logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(Color.White, RoundedCornerShape(30.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fridgemate_logo),
                    contentDescription = "FridgeMate Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome to",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "FridgeMate",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Smart inventory management to reduce waste and discover new flavors.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Feature Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedFeatureCard(
                    icon = Icons.Default.AddAPhoto,
                    title = "AI Scan",
                    modifier = Modifier.weight(1f)
                )
                EnhancedFeatureCard(
                    icon = Icons.Default.Kitchen,
                    title = "Recipes",
                    modifier = Modifier.weight(1f)
                )
                EnhancedFeatureCard(
                    icon = Icons.Default.NotificationsActive,
                    title = "Alerts",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // 2. Updated Button with WorkManager Logic
            Button(
                onClick = {
                    // Define constraints (Battery not low)
                    val constraints = Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()

                    // Create Periodic Work Request (Every 24 Hours)
                    val expirationWorkRequest = PeriodicWorkRequestBuilder<ExpirationWorker>(
                        15, TimeUnit.HOURS
                    ).setConstraints(constraints).build()

                    // Enqueue the work
                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                        "FridgeExpirationCheck",
                        ExistingPeriodicWorkPolicy.KEEP,
                        expirationWorkRequest
                    )

                    // Proceed to next screen
                    onGetStarted()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EnhancedFeatureCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(LightGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = TextDark,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    FRIDGEMATETheme {
        LandingScreen(onGetStarted = {})
    }
}