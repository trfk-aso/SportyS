package com.example.sportys.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sportys.screens.Screen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.btn_continue
import sportys.composeapp.generated.resources.btn_get_started
import sportys.composeapp.generated.resources.btn_skip
import sportys.composeapp.generated.resources.onboarding_slide1
import sportys.composeapp.generated.resources.onboarding_slide2
import sportys.composeapp.generated.resources.onboarding_slide3

@Composable
fun OnboardingScreen(navController: NavHostController) {
    val pages = listOf(
        Res.drawable.onboarding_slide1,
        Res.drawable.onboarding_slide2,
        Res.drawable.onboarding_slide3
    )

    val pagerState = rememberPagerState(initialPage = 0) { pages.size }
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        HorizontalPager(state = pagerState) { page ->
            Image(
                painter = painterResource(pages[page]),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Image(
            painter = painterResource(Res.drawable.btn_skip),
            contentDescription = "Skip",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = if (maxHeight < 700.dp) 50.dp else 90.dp,
                    end = 2.dp
                )
                .size(width = 115.dp, height = 40.dp)
                .clickable {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
        )

        val isLastPage = pagerState.currentPage == pages.lastIndex

        Image(
            painter = painterResource(
                if (isLastPage) Res.drawable.btn_get_started
                else Res.drawable.btn_continue
            ),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp)
                .size(width = 300.dp, height = 140.dp)
                .clickable {
                    if (isLastPage) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
        )
    }
}