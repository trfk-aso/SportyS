package com.example.sportys.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportys.screens.settings.AppTheme
import org.jetbrains.compose.resources.painterResource
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.about_bg
import sportys.composeapp.generated.resources.about_bg_dark
import sportys.composeapp.generated.resources.about_dark
import sportys.composeapp.generated.resources.about_light
import sportys.composeapp.generated.resources.bg_settings_dark
import sportys.composeapp.generated.resources.bg_settings_light
import sportys.composeapp.generated.resources.ic_back
import sportys.composeapp.generated.resources.ic_back_dark

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    val background = when (theme) {
        AppTheme.LIGHT -> painterResource(Res.drawable.bg_settings_light)
        AppTheme.DARK  -> painterResource(Res.drawable.bg_settings_dark)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AboutTopBarDynamic(
                    title = "About the App",
                    theme = theme,
                    onBack = onBack
                )
            }
        ) { padding ->
            AboutSportysScreen(
                background = background,
                onBack = onBack,
                onPrivacy = { uriHandler.openUri("https://iosanalytics.top/8Mhtqf") },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                theme = theme,
                linkColor = if (theme == AppTheme.LIGHT) Color(0xFF7A5E45) else Color.White
            )
        }
    }
}

@Composable
private fun AboutSportysScreen(
    background: Painter,
    onBack: () -> Unit,
    onPrivacy: () -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier,
    leftPadding: Dp = 16.dp,
    bottomPadding: Dp = 28.dp,
    linkColor: Color
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val screenHeight = maxHeight

        Image(
            painter = background,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .matchParentSize(),
            contentScale = ContentScale.Crop
        )

        val aboutImage = when (theme) {
            AppTheme.LIGHT -> painterResource(Res.drawable.about_light)
            AppTheme.DARK  -> painterResource(Res.drawable.about_dark)
        }

        Image(
            painter = aboutImage,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.10f)
                .size(400.dp),
            contentScale = ContentScale.Fit
        )
        PrivacyLink(
            onPrivacy = onPrivacy,
            color = linkColor,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.75f)
        )
    }
}

@Composable
private fun PrivacyLink(
    onPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF7A5E45)
) {
    val label = "Privacy Policy"

    val annotated = remember(color) {
        buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = color,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(label)
            }
            addStringAnnotation(
                tag = "privacy",
                annotation = "",
                start = 0,
                end = label.length
            )
        }
    }

    androidx.compose.foundation.text.ClickableText(
        text = annotated,
        style = LocalTextStyle.current.copy(fontSize = 14.sp, lineHeight = 18.sp),
        modifier = modifier
    ) { offset ->
        annotated.getStringAnnotations(start = offset, end = offset)
            .firstOrNull()?.let {
                if (it.tag == "privacy") onPrivacy()
            }
    }
}

@Composable
fun AboutTopBarDynamic(
    title: String,
    theme: AppTheme,
    onBack: () -> Unit
) {
    val bgColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFF0DA160)
        AppTheme.DARK  -> Color.Transparent
    }

    val iconPainter = when (theme) {
        AppTheme.LIGHT -> painterResource(Res.drawable.ic_back)
        AppTheme.DARK  -> painterResource(Res.drawable.ic_back_dark)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                start = 12.dp,
                end = 12.dp,
                bottom = 12.dp
            )
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = "Back",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(26.dp)
                .align(Alignment.CenterStart)
                .clickable { onBack() }
        )

        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}