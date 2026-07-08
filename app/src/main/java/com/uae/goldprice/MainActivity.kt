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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.SolidColor
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

// ==================== ألوان التصميم الفاخر (Premium Palette) ====================
object PremiumColors {
    val BackgroundBeige = Color(0xFFF9F7F2) // خلفية بيج دافئة ومريحة
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF2C2A28) // رمادي داكن مائل للبني للقراءة المريحة
    val TextMuted = Color(0xFF8C8781)
    val TextFaint = Color(0xFFB5B0A8)
    
    val SageGreen = Color(0xFF7A9382) // أخضر مريمية أنيق
    val SageGreenLight = Color(0xFFE8EFEA)
    
    val LuxuryGold = Color(0xFFC7A556) // ذهبي هادئ وفاخر
    val LuxuryGoldLight = Color(0xFFFDF8EE)
    
    val BorderLight = Color(0xFFEFECE5) // حدود ناعمة جداً
    val ShadowColor = Color(0x0F4A4640) // ظل دافئ وخفيف
}

class MainActivity : ComponentActivity() {

    private val viewModel: GoldViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguageFromPreferences()
        super.onCreate(savedInstanceState)

        try { MobileAds.initialize(this) {} } catch (e: Exception) { e.printStackTrace() }
        try { GoldPriceWorker.enqueue(this) } catch (e: Exception) { e.printStackTrace() }
        askNotificationPermission()

        setContent {
            GoldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PremiumColors.BackgroundBeige
                ) {
                    GoldPriceScreen(viewModel)
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
                    HorizontalDivider(color = PremiumColors.BorderLight, thickness = 1.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PremiumColors.SurfaceWhite)
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
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(36.dp))

                    // Premium Hero Header
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(PremiumColors.LuxuryGoldLight, Color.Transparent)
                                    ),
                                    CircleShape
                                )
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_premium),
                            contentDescription = "UAE Gold Market Logo",
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .shadow(8.dp, CircleShape, ambientColor = PremiumColors.LuxuryGold, spotColor = PremiumColors.LuxuryGold),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.uae_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = PremiumColors.TextPrimary,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.app_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PremiumColors.TextMuted,
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
                                isAr = !isAr
                                val newLanguage = if (isAr) "ar" else "en"

                                val editor = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
                                editor.putString("language", newLanguage)
                                editor.apply()

                                (context as? ComponentActivity)?.recreate()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Live Status - Clean & Elegant
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(PremiumColors.SurfaceWhite, RoundedCornerShape(50.dp))
                            .border(1.dp, PremiumColors.BorderLight, RoundedCornerShape(50.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        PulsingLiveDot()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.live), 
                            color = PremiumColors.SageGreen, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (uiState is UiState.Success) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.width(1.dp).height(12.dp).background(PremiumColors.BorderLight))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                stringResource(R.string.last_updated, formatTime((uiState as UiState.Success).data.updatedAt)),
                                color = PremiumColors.TextMuted,
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
                                color = PremiumColors.TextPrimary,
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

// ==================== الدوال المساعدة والرسوم المتحركة ====================

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
            .size(6.dp) // أصغر وأكثر أناقة
            .background(PremiumColors.SageGreen.copy(alpha = alpha), CircleShape)
    )
}

@Composable
fun PremiumToggleButton(icon: ImageVector?, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = PremiumColors.SurfaceWhite,
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, PremiumColors.BorderLight),
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
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PremiumColors.TextPrimary)
        }
    }
}

@Composable
fun PremiumCardContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PremiumColors.SurfaceWhite,
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
                    .background(PremiumColors.SageGreenLight, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(karat, fontSize = 12.sp, fontWeight = FontWeight.Black, color = PremiumColors.SageGreen)
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
                color = PremiumColors.TextMuted,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text("%.2f".format(price), fontSize = 22.sp, fontWeight = FontWeight.Black, color = PremiumColors.TextPrimary)
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currency == stringResource(R.string.aed)) {
                    Text("د.إ", fontSize = 11.sp, color = PremiumColors.TextFaint, fontWeight = FontWeight.Bold)
                } else {
                    Text("$", fontSize = 11.sp, color = PremiumColors.TextFaint, fontWeight = FontWeight.Bold)
                }
                Text(" / ${stringResource(R.string.gram)}", fontSize = 11.sp, color = PremiumColors.TextFaint)
            }
        }
    }
}

@Composable
fun OunceCard(price: Double, currency: String, isAed: Boolean, priceUsd: Double) {
    PremiumCardContainer(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(PremiumColors.SurfaceWhite, PremiumColors.LuxuryGoldLight)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.global_ounce_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = PremiumColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(PremiumColors.LuxuryGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("XAU", fontWeight = FontWeight.Black, color = PremiumColors.LuxuryGold, fontSize = 12.sp, letterSpacing = 1.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text("%.2f".format(price), fontSize = 32.sp, fontWeight = FontWeight.Black, color = PremiumColors.TextPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                if (isAed) {
                    Text("د.إ", fontSize = 16.sp, color = PremiumColors.LuxuryGold, modifier = Modifier.padding(bottom = 6.dp), fontWeight = FontWeight.Bold)
                } else {
                    Text("$", fontSize = 16.sp, color = PremiumColors.LuxuryGold, modifier = Modifier.padding(bottom = 6.dp), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            if (isAed) {
                Text("%.2f USD".format(priceUsd), fontSize = 13.sp, color = PremiumColors.TextMuted, fontWeight = FontWeight.Medium)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("%.2f ".format(priceUsd * 3.6725), fontSize = 13.sp, color = PremiumColors.TextMuted, fontWeight = FontWeight.Medium)
                    Text("د.إ", fontSize = 12.sp, color = PremiumColors.TextMuted)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PremiumColors.BorderLight)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(stringResource(R.string.calculated_from_raw), fontSize = 11.sp, color = PremiumColors.TextFaint)
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
                color = PremiumColors.TextPrimary,
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
                        focusedBorderColor = PremiumColors.SageGreen,
                        unfocusedBorderColor = PremiumColors.BorderLight,
                        focusedLabelColor = PremiumColors.SageGreen,
                        unfocusedLabelColor = PremiumColors.TextMuted,
                        focusedTextColor = PremiumColors.TextPrimary,
                        unfocusedTextColor = PremiumColors.TextPrimary,
                        cursorColor = PremiumColors.SageGreen,
                        focusedContainerColor = PremiumColors.SurfaceWhite,
                        unfocusedContainerColor = PremiumColors.SurfaceWhite
                    )
                )

                var expanded by remember { mutableStateOf(false) }
                
                // Custom Dropdown trigger to match TextField exactly
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                ) {
                    Surface(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        color = PremiumColors.SurfaceWhite,
                        border = BorderStroke(1.dp, if (expanded) PremiumColors.SageGreen else PremiumColors.BorderLight)
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
                                    color = if (expanded) PremiumColors.SageGreen else PremiumColors.TextMuted
                                )
                                Text(
                                    text = "${selectedKarat}K",
                                    fontSize = 16.sp,
                                    color = PremiumColors.TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = PremiumColors.TextMuted
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(PremiumColors.SurfaceWhite)
                    ) {
                        listOf(24, 22, 21, 18).forEach { k ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "${k}K", 
                                        fontWeight = if (selectedKarat == k) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedKarat == k) PremiumColors.SageGreen else PremiumColors.TextPrimary
                                    ) 
                                },
                                onClick = { selectedKarat = k; expanded = false },
                                modifier = Modifier.background(PremiumColors.SurfaceWhite)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PremiumColors.BackgroundBeige, RoundedCornerShape(16.dp))
                    .border(1.dp, PremiumColors.BorderLight, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.approx_value), fontSize = 14.sp, color = PremiumColors.TextMuted, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("%.2f ".format(result), fontWeight = FontWeight.Black, fontSize = 24.sp, color = PremiumColors.LuxuryGold)
                        if (isAed) {
                            Text("د.إ", fontSize = 16.sp, color = PremiumColors.LuxuryGold, fontWeight = FontWeight.Bold)
                        } else {
                            Text("$", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PremiumColors.LuxuryGold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.calc_disclaimer), fontSize = 11.sp, color = PremiumColors.TextFaint, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun MarketSummaryCard(data: GoldPriceModel, isAed: Boolean, aedRate: Double) {
    val multiplier = if (isAed) aedRate else 1.0
    val currency = if (isAed) stringResource(R.string.aed) else stringResource(R.string.usd)

    PremiumCardContainer(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = PremiumColors.SageGreenLight.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp).background(PremiumColors.SageGreen, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.market_summary),
                    style = MaterialTheme.typography.titleMedium,
                    color = PremiumColors.SageGreen,
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
                color = PremiumColors.TextPrimary,
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
        Text(stringResource(R.string.main_disclaimer), fontSize = 11.sp, color = PremiumColors.TextFaint, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.data_source_disclaimer), fontSize = 11.sp, color = PremiumColors.TextFaint, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.privacy_admob_note), fontSize = 11.sp, color = PremiumColors.TextFaint, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.footer_advice),
            fontSize = 12.sp,
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
