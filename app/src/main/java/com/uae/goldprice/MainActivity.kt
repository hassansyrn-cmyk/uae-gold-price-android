package com.uae.goldprice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: GoldViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle notification permission result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            MobileAds.initialize(this) {}
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            GoldPriceWorker.enqueue(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        askNotificationPermission()

        setContent {
            GoldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GoldPriceScreen(viewModel)
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldPriceScreen(viewModel: GoldViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var isAed by remember { mutableStateOf(true) }
    var isAr by remember { mutableStateOf(true) }
    val aedRate = 3.6725

    Scaffold(
        bottomBar = {
            Column {
                AdBanner()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Logo
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = if (isAr) "سوق الذهب في الإمارات" else "UAE Gold Market",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isAed = !isAed },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Refresh as ImageVector, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd), fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { isAr = !isAr },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(if (isAr) "English" else "العربية", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier
                        .size(8.dp)
                        .background(GoldColors.LiveGreen, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.live),
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldColors.TextMuted
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    if (uiState is UiState.Success) {
                        Text(
                            stringResource(R.string.last_updated, formatTime((uiState as UiState.Success).data.updatedAt)),
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldColors.TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            when (uiState) {
                is UiState.Loading -> {
                    item { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
                }
                is UiState.Success -> {
                    val data = (uiState as UiState.Success).data
                    
                    item {
                        Text(
                            stringResource(R.string.indicative_rates_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldColors.TextMuted,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Price Grid
                    item {
                        val multiplier = if (isAed) aedRate else 1.0
                        val currency = if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd)

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GoldPriceCardSmall(Modifier.weight(1f), "24K", data.karat24 * multiplier, currency)
                                GoldPriceCardSmall(Modifier.weight(1f), "22K", data.karat22 * multiplier, currency)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GoldPriceCardSmall(Modifier.weight(1f), "21K", data.karat21 * multiplier, currency)
                                GoldPriceCardSmall(Modifier.weight(1f), "18K", data.karat18 * multiplier, currency)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Ounce Card
                    item {
                        val multiplier = if (isAed) aedRate else 1.0
                        val currency = if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd)
                        val ounceUsd = data.karat24 * 31.1035

                        OunceCard(ounceUsd * multiplier, currency, isAed, ounceUsd)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Calculator
                    item {
                        GoldCalculator(data, isAed, aedRate)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    // Market Summary
                    item {
                        MarketSummaryCard(data, isAed, aedRate)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Legal
                    item {
                        LegalSection()
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(stringResource(R.string.error_loading), color = Color.Red)
                        Button(onClick = { viewModel.fetchGoldPrice() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoldPriceCardSmall(modifier: Modifier, karat: String, price: Double, currency: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(GoldColors.SecondaryCard, RoundedCornerShape(50.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(karat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(if (karat == "24K") R.string.gold_price_24k else if (karat == "22K") R.string.gold_price_22k else if (karat == "21K") R.string.gold_price_21k else R.string.gold_price_18k), fontSize = 12.sp, color = GoldColors.TextMuted)
            Text("%.2f".format(price), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currency == stringResource(R.string.aed)) {
                    DirhamSymbol(modifier = Modifier.size(12.dp), color = GoldColors.TextMuted)
                } else {
                    Text("$", fontSize = 10.sp, color = GoldColors.TextMuted)
                }
                Text(" / ${stringResource(R.string.gram)}", fontSize = 10.sp, color = GoldColors.TextMuted)
            }
        }
    }
}

@Composable
fun OunceCard(price: Double, currency: String, isAed: Boolean, priceUsd: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(MaterialTheme.colorScheme.surface, GoldColors.SecondaryCard)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text("XAU", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                Text(stringResource(R.string.global_ounce_title), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("%.2f".format(price), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    if (isAed) {
                        DirhamSymbol(modifier = Modifier.padding(bottom = 6.dp).size(20.dp), color = GoldColors.TextMuted)
                    } else {
                        Text("$", fontSize = 14.sp, color = GoldColors.TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
                if (isAed) {
                    Text("%.2f USD".format(priceUsd), fontSize = 12.sp, color = GoldColors.TextMuted)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("%.2f ".format(priceUsd * 3.6725), fontSize = 12.sp, color = GoldColors.TextMuted)
                        DirhamSymbol(modifier = Modifier.size(12.dp), color = GoldColors.TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.calculated_from_raw), fontSize = 10.sp, color = GoldColors.TextMuted)
            }
        }
    }
}

@Composable
fun GoldCalculator(data: GoldPriceModel, isAed: Boolean, aedRate: Double) {
    var weight by remember { mutableStateOf("") }
    var selectedKarat by remember { mutableStateOf(24) }

    val multiplier = if (isAed) aedRate else 1.0
    val pricePerGram = when(selectedKarat) {
        24 -> data.karat24
        22 -> data.karat22
        21 -> data.karat21
        18 -> data.karat18
        else -> data.karat24
    }

    val result = (weight.toDoubleOrNull() ?: 0.0) * pricePerGram * multiplier

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(stringResource(R.string.gold_calculator), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.weight_grams)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(0.6f)) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("${selectedKarat}K")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf(24, 22, 21, 18).forEach { k ->
                            DropdownMenuItem(text = { Text("${k}K") }, onClick = { selectedKarat = k; expanded = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoldColors.SecondaryCard, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.approx_value), fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("%.2f ".format(result), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                        if (isAed) {
                            DirhamSymbol(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text("$", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.calc_disclaimer), fontSize = 10.sp, color = GoldColors.TextMuted)
        }
    }
}

@Composable
fun MarketSummaryCard(data: GoldPriceModel, isAed: Boolean, aedRate: Double) {
    val multiplier = if (isAed) aedRate else 1.0
    val currency = if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoldColors.SecondaryCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(stringResource(R.string.market_summary), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(
                    R.string.market_summary_template,
                    "%.2f".format(data.karat24 * multiplier),
                    currency,
                    "%.2f".format(data.karat24 * 31.1035),
                    "%.2f".format(data.karat24 * 31.1035 * 3.6725)
                ),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun LegalSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GoldColors.SecondaryCard.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.main_disclaimer), fontSize = 11.sp, color = GoldColors.TextMuted, textAlign = TextAlign.Justify)
        Text(stringResource(R.string.data_source_disclaimer), fontSize = 11.sp, color = GoldColors.TextMuted, textAlign = TextAlign.Justify)
        Text(stringResource(R.string.privacy_admob_note), fontSize = 11.sp, color = GoldColors.TextMuted, textAlign = TextAlign.Justify)
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.footer_advice), fontSize = 11.sp, color = GoldColors.TextMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

fun formatTime(isoTime: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoTime)
        val outSdf = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault())
        outSdf.format(date ?: Date())
    } catch (e: Exception) {
        isoTime
    }
}
