package com.borrownest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borrownest.app.ui.theme.SpineAccent
import com.borrownest.app.ui.theme.SpineCharcoal

/** Header summary shown on the central return spine. */
data class SpineInfo(
    val todayLabel: String,
    val nearestReturnLabel: String,
    val dueTodayCount: Int,
    val overdueCount: Int
)

/** The narrow central spine content: date, nearest return, due/overdue counts. */
@Composable
fun ReturnSpineHeader(info: SpineInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SpineCharcoal)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TODAY",
            color = SpineAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = info.todayLabel,
            color = androidx.compose.ui.graphics.Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Next return",
            color = androidx.compose.ui.graphics.Color(0xFFB9BCC2),
            fontSize = 9.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = info.nearestReturnLabel,
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        SpineCounter("Due today", info.dueTodayCount, androidx.compose.ui.graphics.Color(0xFFC46B2D))
        SpineCounter("Overdue", info.overdueCount, androidx.compose.ui.graphics.Color(0xFFB64545))
    }
}

@Composable
private fun SpineCounter(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = if (count > 0) 0.9f else 0.35f))
            .padding(vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$count $label",
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

/** A thin vertical divider used as the visual spine between lanes. */
@Composable
fun VerticalSpine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(2.dp)
            .background(SpineCharcoal.copy(alpha = 0.35f))
    )
}
