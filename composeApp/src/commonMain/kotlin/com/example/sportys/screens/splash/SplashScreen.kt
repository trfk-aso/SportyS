package com.example.sportys.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sportys.screens.Screen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.splash_background

@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    val alpha = remember { Animatable(0f) }

    val blur = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(900, easing = LinearOutSlowInEasing)
            )
        }

        launch {
            blur.animateTo(
                targetValue = 0f,
                animationSpec = tween(900, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(uiState.isFirstLaunch) {
        viewModel.splashDelay(1L).collect {
            if (uiState.isFirstLaunch) {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
                viewModel.markLaunched()
            } else {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    Image(
        painter = painterResource(Res.drawable.splash_background),
        contentDescription = "Splash Background",
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = alpha.value)
            .blur(blur.value.dp),
        contentScale = ContentScale.Crop
    )
}