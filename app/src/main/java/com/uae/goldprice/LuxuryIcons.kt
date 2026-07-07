package com.uae.goldprice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LuxuryGoldIcon(modifier: Modifier = Modifier) {
    // Placeholder for logo icon if needed, but we'll use the real logo in MainActivity
}

@Composable
fun DirhamSymbol(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Icon(
        painter = painterResource(id = R.drawable.ic_aed_symbol),
        contentDescription = "AED",
        tint = color,
        modifier = modifier.size(18.dp)
    )
}

val IconCurrencyExchange: ImageVector = Icons.Default.Refresh
