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
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.delay

// ==================== ألوان التصميم الفاخرة المتناسقة مع الشعار ====================
object PremiumColors {
    val DarkNavyStart = Color(0xFF071224)
    val DarkNavyEnd = Color(0xFF10233D)
    val CardCream = Color(0xFFFDFBF7)
    
    val LuxuryGold = Color(0xFFC7A556)
    val LuxuryGoldDark = Color(0xFF9E7E38)
    val LuxuryGoldLight = Color(0xFFFDF8EE)
    
    val TextOnDark = Color(0xFFFFFFFF)
    val TextInsideCard = Color(0xFF0A1424)
    val TextMutedLight = Color(0xFFA6A29C)
    
    val BorderLight = Color(0xFFE2E8F0)
    val ShadowColor = Color(0x1A000000)
}

class MainActivity : ComponentActivity() {

    private val viewModel: GoldViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language", "ar") ?: "ar"

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try { MobileAds.initialize(this) {} } catch (e: Exception) { e.printStackTrace() }
        try { GoldPriceWorker.enqueue(this) } catch (e: Exception) { e.printStackTrace() }
        askNotificationPermission()

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val skipSplashOnce = prefs.getBoolean("skip_splash_once", false)
        if (skipSplashOnce) {
            prefs.edit().putBoolean("skip_splash_once", false).apply()
        }

        setContent {
            GoldTheme {
                var showSplash by remember { mutableStateOf(!skipSplashOnce) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PremiumColors.DarkNavyStart
                ) {
                    if (showSplash) {
                        PremiumSplashScreen(onSplashFinished = { showSplash = false })
                    } else {
                        GoldPriceScreen(viewModel)
                    }
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

// ==================== شاشة الـ Splash Screen الفاخرة ====================
@Composable
fun PremiumSplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onSplashFinished()
    }
    Image(
        painter = painterResource(id = R.drawable.splash_art),
        contentDescription = "Splash Screen Art",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldPriceScreen(viewModel: GoldViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var isAed by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val isAr = prefs.getString("language", "ar") == "ar"
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PremiumColors.DarkNavyStart, PremiumColors.DarkNavyEnd)
                    )
                )
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                                .padding(vertical = 4.dp)
                        ) {
                            AdBanner() // يستدعي الملف الخارجي الحقيقي AdBanner.kt تلقائياً
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 40.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(36.dp))

                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                modifier = Modifier.size(112.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = PremiumColors.DarkNavyStart,
                                border = BorderStroke(1.5.dp, PremiumColors.LuxuryGold.copy(alpha = 0.8f)),
                                shadowElevation = 12.dp
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_logo_premium),
                                    contentDescription = "UAE Gold Market Logo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = stringResource(R.string.uae_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = PremiumColors.TextOnDark,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.app_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = PremiumColors.LuxuryGold,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PremiumToggleButton(
                                icon = Icons.Default.Refresh,
                                label = if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd),
                                onClick = { isAed = !isAed }
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            PremiumToggleButton(
                                icon = null,
                                label = if (isAr) "English" else "العربية",
                                onClick = {
                                    val newLanguage = if (isAr) "en" else "ar"
                                    val editor = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
                                    editor.putString("language", newLanguage)
                                    editor.putBoolean("skip_splash_once", true)
                                    editor.apply()
                                    (context as? ComponentActivity)?.recreate()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(PremiumColors.DarkNavyStart, RoundedCornerShape(50.dp))
                                .border(1.dp, PremiumColors.LuxuryGold.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            PulsingLiveDot()
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.live), 
                                color = PremiumColors.LuxuryGold, 
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (uiState is UiState.Success) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.width(1.dp).height(12.dp).background(PremiumColors.LuxuryGold.copy(alpha = 0.3f)))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    stringResource(R.string.last_updated, formatTime((uiState as UiState.Success).data.updatedAt)),
                                    color = PremiumColors.TextOnDark.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                               )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    when (uiState) {
                        is UiState.Loading -> {
                            item {
                                Spacer(modifier = Modifier.height(32.dp))
                                CircularProgressIndicator(color = PremiumColors.LuxuryGold)
                            }
                        }
                        is UiState.Success -> {
                            val data = (uiState as UiState.Success).data

                            item {
                                Text(
                                    stringResource(R.string.indicative_rates_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PremiumColors.LuxuryGold,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    textAlign = TextAlign.Start
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            item {
                                val multiplier = if (isAed) 3.6725 else 1.0
                                val currency = if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd)

                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        GoldPriceCardSmall(Modifier.weight(1f), "24K", data.karat24 * multiplier, currency)
                                        GoldPriceCardSmall(Modifier.weight(1f), "22K", data.karat22 * multiplier, currency)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        GoldPriceCardSmall(Modifier.weight(1f), "21K", data.karat21 * multiplier, currency)
                                        GoldPriceCardSmall(Modifier.weight(1f), "18K", data.karat18 * multiplier, currency)
                                    }
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                            }

                            item {
                                val multiplier = if (isAed) 3.6725 else 1.0
                                val currency = if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd)
                                val ounceUsd = data.karat24 * 31.1035

                                OunceCard(ounceUsd * multiplier, currency, isAed, ounceUsd)
                                Spacer(modifier = Modifier.height(32.dp))
                            }

                            item {
                                GoldCalculator(data, isAed, 3.6725)
                                Spacer(modifier = Modifier.height(32.dp))
                            }

                            item {
                                MarketSummaryCard(data, isAed, 3.6725)
                                Spacer(modifier = Modifier.height(32.dp))
                            }

                            item {
                                LegalSection()
                            }
                        }
                        is UiState.Error -> {
                            item {
                                Text(stringResource(R.string.error_loading), color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.fetchGoldPrice() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PremiumColors.LuxuryGold, contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
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
}

// ==================== الدوال المساعدة والأنيميشن ====================

@Composable
fun Modifier.bounceClick(): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}

@Composable
fun PulsingLiveDot() {
    val transition = rememberInfiniteTransition(label = "live-pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live-pulse-alpha"
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .background(PremiumColors.LuxuryGold.copy(alpha = alpha), CircleShape)
    )
}

@Composable
fun PremiumToggleButton(icon: ImageVector?, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = PremiumColors.DarkNavyStart,
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, PremiumColors.LuxuryGold),
        shadowElevation = 2.dp,
        modifier = Modifier.bounceClick()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = PremiumColors.LuxuryGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PremiumColors.TextOnDark)
        }
    }
}

@Composable
fun PremiumCardContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PremiumColors.CardCream,
    backgroundBrush: Brush? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = PremiumColors.ShadowColor,
                ambientColor = PremiumColors.ShadowColor
            )
            .clip(RoundedCornerShape(24.dp))
            .then(
                if (backgroundBrush != null) Modifier.background(backgroundBrush)
                else Modifier.background(backgroundColor)
            )
            .border(1.dp, PremiumColors.BorderLight, RoundedCornerShape(24.dp))
    ) {
        Column(content = content)
    }
}

@Composable
fun GoldPriceCardSmall(modifier: Modifier, karat: String, price: Double, currency: String) {
    PremiumCardContainer(modifier = modifier.bounceClick()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(PremiumColors.DarkNavyStart, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(karat, fontSize = 12.sp, fontWeight = FontWeight.Black, color = PremiumColors.LuxuryGold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                stringResource(
                    when (karat) {
                        "24K" -> R.string.gold_price_24k
                        "22K" -> R.string.gold_price_22k
                        "21K" -> R.string.gold_price_21k
                        else -> R.string.gold_price_18k
                    }
                ),
                fontSize = 13.sp,
                color = Color(0xFF4A5568),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text("%.2f".format(price), fontSize = 22.sp, fontWeight = FontWeight.Black, color = PremiumColors.TextInsideCard)
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currency == stringResource(R.string.aed)) {
                    Text("د.إ", fontSize = 11.sp, color = PremiumColors.LuxuryGoldDark, fontWeight = FontWeight.Bold)
                } else {
                    Text("$", fontSize = 11.sp, color = PremiumColors.LuxuryGoldDark, fontWeight = FontWeight.Bold)
                }
                Text(" / ${stringResource(R.string.gram)}", fontSize = 11.sp, color = Color(0xFF718096))
            }
        }
    }
}

@Composable
fun OunceCard(price: Double, currency: String, isAed: Boolean, priceUsd: Double) {
    PremiumCardContainer(modifier = Modifier.fillMaxWidth().bounceClick()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.global_ounce_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = PremiumColors.TextInsideCard,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(PremiumColors.DarkNavyStart, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("XAU", fontWeight = FontWeight.Black, color = PremiumColors.LuxuryGold, fontSize = 12.sp, letterSpacing = 1.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text("%.2f".format(price), fontSize = 32.sp, fontWeight = FontWeight.Black, color = PremiumColors.TextInsideCard)
                Spacer(modifier = Modifier.width(6.dp))
                if (isAed) {
                    Text("د.إ", fontSize = 16.sp, color = PremiumColors.LuxuryGoldDark, modifier = Modifier.padding(bottom = 6.dp), fontWeight = FontWeight.Bold)
                } else {
                    Text("$", fontSize = 16.sp, color = PremiumColors.LuxuryGoldDark, modifier = Modifier.padding(bottom = 6.dp), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            if (isAed) {
                Text("%.2f USD".format(priceUsd), fontSize = 13.sp, color = Color(0xFF4A5568), fontWeight = FontWeight.Medium)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("%.2f ".format(priceUsd * 3.6725), fontSize = 13.sp, color = Color(0xFF4A5568), fontWeight = FontWeight.Medium)
                    Text("د.إ", fontSize = 12.sp, color = Color(0xFF4A5568))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PremiumColors.BorderLight)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(stringResource(R.string.calculated_from_raw), fontSize = 11.sp, color = Color(0xFF718096))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    PremiumCardContainer(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                stringResource(R.string.gold_calculator),
                style = MaterialTheme.typography.titleMedium,
                color = PremiumColors.TextInsideCard,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.weight_grams)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PremiumColors.LuxuryGold,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedLabelColor = PremiumColors.LuxuryGold,
                        unfocusedLabelColor = Color(0xFF718096),
                        focusedTextColor = PremiumColors.TextInsideCard,
                        unfocusedTextColor = PremiumColors.TextInsideCard,
                        cursorColor = PremiumColors.LuxuryGold,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                var expanded by remember { mutableStateOf(false) }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                ) {
                    Surface(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, if (expanded) PremiumColors.LuxuryGold else Color(0xFFCBD5E1))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = "العيار / Karat",
                                    fontSize = 12.sp,
                                    color = if (expanded) PremiumColors.LuxuryGold else Color(0xFF718096)
                                )
                                Text(
                                    text = "${selectedKarat}K",
                                    fontSize = 16.sp,
                                    color = PremiumColors.TextInsideCard,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF718096)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        listOf(24, 22, 21, 18).forEach { k ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "${k}K", 
                                        fontWeight = if (selectedKarat == k) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedKarat == k) PremiumColors.LuxuryGold else PremiumColors.TextInsideCard
                                    ) 
                                },
                                onClick = { selectedKarat = k; expanded = false },
                                modifier = Modifier.background(Color.White)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7FAFC), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.approx_value), fontSize = 14.sp, color = Color(0xFF4A5568), fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("%.2f ".format(result), fontWeight = FontWeight.Black, fontSize = 24.sp, color = PremiumColors.LuxuryGoldDark)
                        if (isAed) {
                            Text("د.إ", fontSize = 16.sp, color = PremiumColors.LuxuryGoldDark, fontWeight = FontWeight.Bold)
                        } else {
                            Text("$", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PremiumColors.LuxuryGoldDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.calc_disclaimer), fontSize = 11.sp, color = Color(0xFF718096), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun MarketSummaryCard(data: GoldPriceModel, isAed: Boolean, aedRate: Double) {
    val multiplier = if (isAed) aedRate else 1.0
    val currency = if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd)

    PremiumCardContainer(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(5.dp).background(PremiumColors.LuxuryGoldDark, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.market_summary),
                    style = MaterialTheme.typography.titleMedium,
                    color = PremiumColors.LuxuryGoldDark,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(
                    R.string.market_summary_template,
                    "%.2f".format(data.karat24 * multiplier),
                    currency,
                    "%.2f".format(data.karat24 * 31.1035),
                    "%.2f".format(data.karat24 * 31.1035 * 3.6725)
                ),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = PremiumColors.TextInsideCard,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LegalSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.main_disclaimer), fontSize = 11.sp, color = PremiumColors.TextMutedLight, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.data_source_disclaimer), fontSize = 11.sp, color = PremiumColors.TextMutedLight, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.privacy_admob_note), fontSize = 11.sp, color = PremiumColors.TextMutedLight, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.footer_advice),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PremiumColors.LuxuryGold,
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
