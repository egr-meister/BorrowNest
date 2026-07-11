package com.borrownest.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.borrownest.app.ui.components.Copy
import com.borrownest.app.ui.theme.BorrowedIndigo
import com.borrownest.app.ui.theme.GivenTerracotta
import com.borrownest.app.ui.theme.SpineCharcoal

@Composable
fun OnboardingScreen(
    onAddFirstItem: () -> Unit,
    onExplore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SplitIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.height(20.dp))
        Text(
            "BorrowNest",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Keep both sides of item lending clear.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(Modifier.height(16.dp))
        OnboardingPoint("Given and Borrowed", "Track what you gave and what you borrowed on a two-sided board.")
        OnboardingPoint("Manual names", "Enter names manually and set an expected return date.")
        OnboardingPoint("Statuses & reminders", "See due soon, due today and overdue items with in-app reminders.")
        OnboardingPoint("History & archive", "Review returns, browse local history, and archive completed records.")
        OnboardingPoint("Private & offline", "Your records stay on this device. No contacts, no account, no cloud.")

        Spacer(Modifier.height(16.dp))
        InfoCard(Copy.MANUAL_TRACKING_DISCLAIMER)
        Spacer(Modifier.height(8.dp))
        InfoCard(Copy.PRIVACY_NOTE)

        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddFirstItem, modifier = Modifier.fillMaxWidth()) {
            Text("Add First Item")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onExplore, modifier = Modifier.fillMaxWidth()) {
            Text("Explore Board")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SplitIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val half = size.width / 2f
        drawRect(color = GivenTerracotta, size = androidx.compose.ui.geometry.Size(half, size.height))
        drawRect(
            color = BorrowedIndigo,
            topLeft = Offset(half, 0f),
            size = androidx.compose.ui.geometry.Size(half, size.height)
        )
        // Central return spine.
        drawRect(
            color = Color.White,
            topLeft = Offset(half - 3f, 0f),
            size = androidx.compose.ui.geometry.Size(6f, size.height)
        )
        // Opposing directional tabs.
        val tabW = size.width * 0.16f
        val tabH = size.height * 0.14f
        val cy = size.height / 2f
        drawRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(half - tabW - 14f, cy - tabH / 2f),
            size = androidx.compose.ui.geometry.Size(tabW, tabH)
        )
        drawRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(half + 14f, cy - tabH / 2f),
            size = androidx.compose.ui.geometry.Size(tabW, tabH)
        )
    }
}

@Composable
private fun OnboardingPoint(title: String, body: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SpineCharcoal.copy(alpha = 0.06f))
            .padding(12.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
