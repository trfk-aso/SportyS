package com.example.sportys.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.bg_settings_dark
import sportys.composeapp.generated.resources.bg_settings_light
import sportys.composeapp.generated.resources.ic_back
import sportys.composeapp.generated.resources.ic_back_dark
import sportys.composeapp.generated.resources.ic_lock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigator: NavController,
    theme: AppTheme,
    vm: SettingsViewModel = koinInject()
) {
    val state by vm.state.collectAsState()

    val bgPainter = when (theme) {
        AppTheme.LIGHT -> painterResource(Res.drawable.bg_settings_light)
        AppTheme.DARK  -> painterResource(Res.drawable.bg_settings_dark)
    }

    val colorText = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color.White
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = bgPainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Settings",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.popBackStack() }) {
                            val backIcon = when (theme) {
                                AppTheme.LIGHT -> painterResource(Res.drawable.ic_back)
                                AppTheme.DARK  -> painterResource(Res.drawable.ic_back_dark)
                            }

                            Icon(
                                painter = backIcon,
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = when (theme) {
                            AppTheme.LIGHT -> Color(0xFF0DA160)
                            AppTheme.DARK  -> Color.Transparent
                        }
                    )
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                Text("APPEARANCE", fontWeight = FontWeight.Bold, color = colorText)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    val radioColor =
                        if (theme == AppTheme.LIGHT) Color(0xFF00D400) else Color.White

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = theme == AppTheme.LIGHT,
                            onClick = { if (state.isPremiumThemeUnlocked) vm.toggleTheme() else {} },
                            enabled = state.isPremiumThemeUnlocked,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = radioColor,
                                unselectedColor = radioColor
                            )
                        )
                        Text(
                            "Light",
                            color = colorText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (state.isPremiumThemeUnlocked) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = theme == AppTheme.DARK,
                                onClick = { vm.toggleTheme() },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = radioColor,
                                    unselectedColor = radioColor
                                )
                            )
                            Text(
                                "Dark",
                                color = colorText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (!state.isPremiumThemeUnlocked) {
                    PremiumThemeCard(
                        unlocked = false,
                        onUnlock = { vm.buyPremiumTheme() }
                    )
                }

                Text("DATA", fontWeight = FontWeight.Bold, color = colorText)

                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    onClick = { vm.exportJson() },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (theme == AppTheme.LIGHT) Color(0xFFD0D0D0) else Color(0xFF4A4A4A)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (theme == AppTheme.LIGHT) Color.White else Color(0xFF1A1A1A),
                        contentColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White
                    )
                ) {
                    Text(
                        "Export JSON file",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    onClick = { vm.onResetClicked() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!state.isResetPurchased) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_lock),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = if (state.isResetPurchased)
                                "Reset app data"
                            else
                                "Reset app data 1.99$",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    "(Clears favorites, history, settings)",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    onClick = { vm.restorePurchases() },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (theme == AppTheme.LIGHT) Color(0xFF00D400) else Color.White
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = if (theme == AppTheme.LIGHT) Color(0xFF00D400) else Color.White
                    )
                ) {
                    Text(
                        "Restore Purchases",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                AboutSection(theme = theme, navigator = navigator)
            }
        }

        if (state.showResetDialog) {
            ResetDialog(
                theme = theme,
                onConfirm = { vm.resetAll() },
                onCancel = { vm.showResetDialog(false) }
            )
        }
    }
}

@Composable
fun PremiumThemeCard(
    unlocked: Boolean,
    onUnlock: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF005000), Color.Black)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                "Premium Dark Theme",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "1.99$",
                color = Color.White,
                fontSize = 24.sp
            )
        }

        if (!unlocked) {
            Button(
                onClick = onUnlock,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(42.dp)
                    .width(110.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6DDE73),
                    contentColor = Color.Black
                )
            ) {
                Text("Apply", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        } else {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetDialog(
    theme: AppTheme,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val isDark = theme == AppTheme.DARK

    val backgroundColor = if (isDark) Color(0xFF2B2B2B) else Color.White
    val titleColor = if (isDark) Color.White else Color.Black
    val textColor = if (isDark) Color(0xFFE0E0E0) else Color.Black
    val cancelBg = if (isDark) Color(0xFF666666) else Color(0xFFE0E0E0)
    val cancelText = Color.Black

    AlertDialog(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(24.dp),
        containerColor = backgroundColor,
        titleContentColor = titleColor,
        textContentColor = textColor,

        title = {
            Text(
                "Reset app data?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },

        text = {
            Text(
                "This will delete all favorites, history, and settings.\nThis cannot be undone.",
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },

        dismissButton = {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cancelBg,
                    contentColor = cancelText
                )
            ) {
                Text(
                    "Cancel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },

        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Reset app data",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
fun AboutSection(
    theme: AppTheme,
    navigator: NavController
) {
    val isDark = theme == AppTheme.DARK

    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val borderColor = if (isDark) Color(0xFF3A3A3A) else Color(0xFFE3E3E3)
    val textColor = if (isDark) Color.White else Color.Black
    val arrowColor = if (isDark) Color.White else Color.Black

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            "ABOUT",
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 16.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .clickable { navigator.navigate("about") }
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "About the app",
                fontSize = 18.sp,
                color = textColor,
                fontWeight = FontWeight.Medium
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = arrowColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}