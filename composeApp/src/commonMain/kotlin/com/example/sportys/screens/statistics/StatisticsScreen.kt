package com.example.sportys.screens.statistics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sportys.model.TeamLeagueStats
import com.example.sportys.screens.bottombar.AppBottomBar
import com.example.sportys.screens.settings.AppTheme
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.bg_settings_dark
import sportys.composeapp.generated.resources.bg_settings_light

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = koinInject(),
    onTeamClick: (String) -> Unit,
    theme: AppTheme
) {
    val state by viewModel.state.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bgPainter = when (theme) {
        AppTheme.LIGHT -> painterResource(Res.drawable.bg_settings_light)
        AppTheme.DARK  -> painterResource(Res.drawable.bg_settings_dark)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = bgPainter,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                StatisticsTopBar(theme)
            },
            bottomBar = {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    theme = theme
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                when {
                    state.isLoading -> {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            CircularProgressIndicator(
                                color = if (theme == AppTheme.LIGHT) Color.Black else Color.White
                            )
                        }
                    }

                    state.teams.isEmpty() -> {
                        EmptyStatisticsState(theme = theme)
                    }

                    else -> {
                        StatisticsContent(
                            state = state,
                            onTeamClick = onTeamClick,
                            theme = theme
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsTopBar(theme: AppTheme) {

    val bgColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFF0DA160)
        AppTheme.DARK  -> Color.Transparent
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
        Text(
            text = "Statistics",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun StatisticsTabItem(
    text: String,
    period: StatisticsPeriod,
    selected: StatisticsPeriod,
    onSelect: (StatisticsPeriod) -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onSelect(period) }
    ) {
        Text(
            text,
            fontSize = 18.sp,
            fontWeight = if (selected == period) FontWeight.Bold else FontWeight.Medium,
            color = if (selected == period) Color.Black else Color.Gray
        )
        if (selected == period) {
            Spacer(
                modifier = Modifier
                    .height(2.dp)
                    .width(36.dp)
                    .background(Color.Black)
            )
        }
    }
}

@Composable
fun StatisticsContent(
    state: StatisticsState,
    onTeamClick: (String) -> Unit,
    theme: AppTheme
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "My Teams",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )

        TeamStatsTable(state.teams, onTeamClick, theme = theme)

        Text(
            "My Activity",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Articles read this week:",
                value = state.articlesRead.toString(),
                theme = theme,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Matches watched:",
                value = state.matchesWatched.toString(),
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TeamStatsTable(
    teams: List<TeamLeagueStats>,
    onTeamClick: (String) -> Unit,
    theme: AppTheme
) {
    val textColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White

    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Text("Teams", modifier = Modifier.weight(2f), color = textColor)
        Text("Position", modifier = Modifier.weight(1f), color = textColor)
        Text("Played", modifier = Modifier.weight(1f), color = textColor)
        Text("Points", modifier = Modifier.weight(1f), color = textColor)
    }

    teams.forEach { t ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onTeamClick(t.teamId) },
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier.weight(2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                t.logoUrl?.let { logo ->
                    KamelImage(
                        resource = asyncPainterResource(logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(Modifier.width(6.dp))

                Text(
                    text = t.name,
                    fontSize = 16.sp,
                    color = textColor
                )
            }

            Text(
                text = t.position.toString(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = textColor
            )

            Text(
                text = t.played.toString(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = textColor
            )

            Text(
                text = t.points.toString(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = textColor
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    val background = when (theme) {
        AppTheme.LIGHT -> Color.White
        AppTheme.DARK  -> Color(0xFF1E1E1E)
    }

    val titleColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFF666666)
        AppTheme.DARK  -> Color(0xFFAAAAAA)
    }

    val valueColor = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color.White
    }

    val borderColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFFE5E5E5)
        AppTheme.DARK  -> Color.Transparent
    }

    Column(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            color = titleColor
        )

        Spacer(Modifier.height(12.dp))

        Text(
            value,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun EmptyStatisticsState(theme: AppTheme) {
    val textColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Add your favorite teams to see their statistics.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = textColor
        )
    }
}
