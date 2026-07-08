package com.uae.goldprice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
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
    ) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        // تطبيق اللغة المحفوظة قبل تحميل أي شيء
        applyLanguageFromPreferences()

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
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GoldColors.BackgroundGradient)
                    ) {
                        GoldPriceScreen(viewModel)
                    }
                }
            }
        }
    }

    private fun applyLanguageFromPreferences() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language", "ar") ?: "ar"

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
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
    val context = LocalContext.current

    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var isAr by remember {
        mutableStateOf(prefs.getString("language", "ar") == "ar")
    }

    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column {
                    HorizontalDivider(color = GoldColors.GlassBorder, thickness = 1.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GoldColors.Surface)
                            .padding(vertical = 4.dp)
                    ) {
                        AdBanner()
                    }
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
                    Spacer(modifier = Modifier.height(28.dp))

                    // Logo
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(104.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            GoldColors.Gold.copy(alpha = 0.22f),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_premium),
                            contentDescription = "UAE Gold Market Logo",
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(R.string.uae_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.app_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldColors.Gold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Language & Currency buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassToggleButton(
                            icon = Icons.Default.Refresh,
                            label = if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd),
                            onClick = { isAed = !isAed }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        GlassToggleButton(
                            icon = null,
                            label = if (isAr) "English" else "العربية",
                            onClick = {
                                isAr = !isAr
                                val newLanguage = if (isAr) "ar" else "en"

                                // حفظ اللغة
                                val editor = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
                                editor.putString("language", newLanguage)
                                editor.apply()

                                // إعادة تشغيل التطبيق
                                (context as? ComponentActivity)?.recreate()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Live status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingLiveDot()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.live),
                            color = GoldColors.EmeraldGlow,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        if (uiState is UiState.Success) {
                            Text(
                                stringResource(
                                    R.string.last_updated,
                                    formatTime((uiState as UiState.Success).data.updatedAt)
                                ),
                                color = GoldColors.TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))
                }

                when (uiState) {
                    is UiState.Loading -> {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            CircularProgressIndicator(color = GoldColors.Gold)
                        }
                    }
                    is UiState.Success -> {
                        val data = (uiState as UiState.Success).data

                        // Indicative Rates Title
                        item {
                            Text(
                                stringResource(R.string.indicative_rates_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldColors.TextMuted,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Price Cards
                        item {
                            val multiplier = if (isAed) 3.6725 else 1.0
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
                            val multiplier = if (isAed) 3.6725 else 1.0
                            val currency = if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd)
                            val ounceUsd = data.karat24 * 31.1035

                            OunceCard(ounceUsd * multiplier, currency, isAed, ounceUsd)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Calculator
                        item {
                            GoldCalculator(data, isAed, 3.6725)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Market Summary
                        item {
                            MarketSummaryCard(data, isAed, 3.6725)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Legal
                        item {
                            LegalSection()
                        }
                    }
                    is UiState.Error -> {
                        item {
                            Text(stringResource(R.string.error_loading), color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.fetchGoldPrice() },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldColors.Gold, contentColor = Color(0xFF1A1408))
                            ) {
                                Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== باقي الدوال (كاملة) ====================

@Composable
fun PulsingLiveDot() {
    val transition = rememberInfiniteTransition(label = "live-pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live-pulse-alpha"
    )
    Box(
        modifier = Modifier
            .size(9.dp)
            .background(GoldColors.EmeraldGlow.copy(alpha = alpha), CircleShape)
    )
}

@Composable
fun GlassToggleButton(icon: ImageVector?, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = GoldColors.GlassCard,
            contentColor = GoldColors.TextPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldColors.GlassBorder),
        shape = RoundedCornerShape(50.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = GoldColors.Gold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun GlassCardContainer(
    modifier: Modifier = Modifier,
    borderColor: Color = GoldColors.GlassBorder,
    background: Brush? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (background != null) Modifier.background(background)
                else Modifier.background(GoldColors.GlassCard)
            )
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
    ) {
        Column(content = content)
    }
}

@Composable
fun GoldPriceCardSmall(modifier: Modifier, karat: String, price: Double, currency: String) {
    GlassCardContainer(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(GoldColors.Emerald.copy(alpha = 0.22f), RoundedCornerShape(50.dp))
                    .border(1.dp, GoldColors.EmeraldGlow.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(karat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldColors.EmeraldGlow)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(
                    when (karat) {
                        "24K" -> R.string.gold_price_24k
                        "22K" -> R.string.gold_price_22k
                        "21K" -> R.string.gold_price_21k
                        else -> R.string.gold_price_18k
                    }
                ),
                fontSize = 12.sp,
                color = GoldColors.TextMuted
            )
            Text("%.2f".format(price), fontSize = 19.sp, fontWeight = FontWeight.Bold, color = GoldColors.Gold)
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
    GlassCardContainer(
        modifier = Modifier.fillMaxWidth(),
        borderColor = GoldColors.GlassBorderStrong,
        background = GoldColors.HeroGradient
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("XAU", fontWeight = FontWeight.Black, color = GoldColors.Gold, fontSize = 18.sp, letterSpacing = 1.sp)
            Text(
                stringResource(R.string.global_ounce_title),
                style = MaterialTheme.typography.titleMedium,
                color = GoldColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("%.2f".format(price), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GoldColors.TextPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                if (isAed) {
                    DirhamSymbol(modifier = Modifier.padding(bottom = 6.dp).size(20.dp), color = GoldColors.Gold)
                } else {
                    Text("$", fontSize = 14.sp, color = GoldColors.Gold, modifier = Modifier.padding(bottom = 4.dp))
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
            Text(stringResource(R.string.calculated_from_raw), fontSize = 10.sp, color = GoldColors.TextFaint)
        }
    }
}

@Composable
fun GoldCalculator(data: GoldPriceModel, isAed: Boolean, aedRate: Double) {
    var weight by remember { mutableStateOf("") }
    var selectedKarat by remember { mutableStateOf(24) }

    val multiplier = if (isAed) aedRate else 1.0
    val pricePerGram = when (selectedKarat) {
        24 -> data.karat24
        22 -> data.karat22
        21 -> data.karat21
        18 -> data.karat18
        else -> data.karat24
    }

    val result = (weight.toDoubleOrNull() ?: 0.0) * pricePerGram * multiplier

    GlassCardContainer(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.gold_calculator),
                style = MaterialTheme.typography.titleMedium,
                color = GoldColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.weight_grams)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldColors.Gold,
                        unfocusedBorderColor = GoldColors.GlassBorder,
                        focusedLabelColor = GoldColors.Gold,
                        unfocusedLabelColor = GoldColors.TextMuted,
                        focusedTextColor = GoldColors.TextPrimary,
                        unfocusedTextColor = GoldColors.TextPrimary,
                        cursorColor = GoldColors.Gold
                    )
                )

                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(0.6f)) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldColors.TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldColors.GlassBorder)
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
                    .background(
                        Brush.linearGradient(listOf(GoldColors.SecondaryCard, GoldColors.Emerald.copy(alpha = 0.18f))),
                        RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, GoldColors.Gold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.approx_value), fontSize = 12.sp, color = GoldColors.TextMuted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("%.2f ".format(result), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = GoldColors.Gold)
                        if (isAed) {
                            DirhamSymbol(modifier = Modifier.size(20.dp), color = GoldColors.Gold)
                        } else {
                            Text("$", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = GoldColors.Gold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.calc_disclaimer), fontSize = 10.sp, color = GoldColors.TextFaint)
        }
    }
}

@Composable
fun MarketSummaryCard(data: GoldPriceModel, isAed: Boolean, aedRate: Double) {
    val multiplier = if (isAed) aedRate else 1.0
    val currency = if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd)

    GlassCardContainer(
        modifier = Modifier.fillMaxWidth(),
        borderColor = GoldColors.Emerald.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.market_summary),
                style = MaterialTheme.typography.titleMedium,
                color = GoldColors.EmeraldGlow,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(
                    R.string.market_summary_template,
                    "%.2f".format(data.karat24 * multiplier),
                    currency,
                    "%.2f".format(data.karat24 * 31.1035),
                    "%.2f".format(data.karat24 * 31.1035 * 3.6725)
                ),
                fontSize = 14.sp,
                color = GoldColors.TextPrimary
            )
        }
    }
}

@Composable
fun LegalSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GoldColors.Surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .border(1.dp, GoldColors.GlassBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.main_disclaimer), fontSize = 11.sp, color = GoldColors.TextFaint, textAlign = TextAlign.Justify)
        Text(stringResource(R.string.data_source_disclaimer), fontSize = 11.sp, color = GoldColors.TextFaint, textAlign = TextAlign.Justify)
        Text(stringResource(R.string.privacy_admob_note), fontSize = 11.sp, color = GoldColors.TextFaint, textAlign = TextAlign.Justify)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.footer_advice),
            fontSize = 11.sp,
            color = GoldColors.Gold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
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