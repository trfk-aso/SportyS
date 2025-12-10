package com.example.sportys.screens.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.Navigator
import com.example.sportys.model.Article
import com.example.sportys.model.League
import com.example.sportys.model.Match
import com.example.sportys.model.MatchStatus
import com.example.sportys.model.Team
import com.example.sportys.screens.settings.AppTheme
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.bg_settings_dark
import sportys.composeapp.generated.resources.bg_settings_light
import sportys.composeapp.generated.resources.ic_back
import sportys.composeapp.generated.resources.ic_back_dark
import sportys.composeapp.generated.resources.ic_star_filled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: SearchViewModel = koinInject(),
    theme: AppTheme
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
        viewModel.loadFavoriteTeams()
    }

    val bgPainter = when (theme) {
        AppTheme.LIGHT -> painterResource(Res.drawable.bg_settings_light)
        AppTheme.DARK  -> painterResource(Res.drawable.bg_settings_dark)
    }

    val title = when (state.contentType) {
        ContentType.ARTICLES -> when (state.timeRange) {
            TimeRange.TODAY -> "Articles today"
            TimeRange.LAST_7_DAYS -> "Articles last 7 days"
            TimeRange.LAST_MONTH -> "Articles last month"
        }

        ContentType.MATCHES -> when (state.timeRange) {
            TimeRange.TODAY -> "Matches today"
            TimeRange.LAST_7_DAYS -> "Matches last 7 days"
            TimeRange.LAST_MONTH -> "Matches last month"
        }

        ContentType.TEAMS -> "Teams"
        ContentType.LEAGUES -> "Leagues"
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Фон
        Image(
            painter = bgPainter,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SearchResultsTopBar(
                    title = title,
                    theme = theme,
                    onBack = onBack
                )
            }
        ) { padding ->

            when {
                state.isLoading -> Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    when (state.contentType) {
                        ContentType.TEAMS,
                        ContentType.LEAGUES -> TeamsSkeleton(theme)
                        ContentType.MATCHES -> MatchesSkeletonList(theme)
                        ContentType.ARTICLES -> ArticleSkeletonList(theme)
                    }
                }

                state.error != null -> Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    ErrorState(
                        message = state.error!!,
                        onRetry = { viewModel.applySearch() }
                    )
                }

                else -> {
                    when (state.contentType) {
                        ContentType.ARTICLES -> ArticleResults(
                            navController = navController,
                            modifier = Modifier.padding(padding),
                            items = state.articles,
                            theme = theme
                        )

                        ContentType.MATCHES -> MatchResults(
                            navController = navController,
                            modifier = Modifier.padding(padding),
                            items = state.matches,
                            theme = theme
                        )

                        ContentType.TEAMS -> TeamResults(
                            modifier = Modifier.padding(padding),
                            items = state.teams,
                            state = state,
                            viewModel = viewModel,
                            theme = theme
                        )

                        ContentType.LEAGUES -> LeagueResults(
                            modifier = Modifier.padding(padding),
                            items = state.leagues,
                            viewModel = viewModel,
                            theme = theme
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeagueResults(
    modifier: Modifier = Modifier,
    items: List<League>,
    viewModel: SearchViewModel,
    theme: AppTheme
) {
    val state by viewModel.state.collectAsState()

    if (items.isEmpty()) {
        EmptyState("Nothing here yet.", theme = theme)
        return
    }

    val chunked = items.chunked(3)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(chunked) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { league ->
                    LeagueCard(
                        league = league,
                        isFavorite = league.id in state.favoriteLeagues,
                        onToggleFavorite = { viewModel.toggleLeagueFavorite(it) },
                        theme = theme
                    )
                }

                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.width(100.dp))
                }
            }
        }
    }
}

@Composable
fun LeagueCard(
    league: League,
    isFavorite: Boolean,
    onToggleFavorite: (League) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    theme: AppTheme
) {
    val bgCircle = when (theme) {
        AppTheme.LIGHT -> Color(0xFFF2F2F2)
        AppTheme.DARK  -> Color(0xFF3A3A3A)
    }

    val textColor = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color.White
    }

    Column(
        modifier = modifier
            .width(100.dp)
            .clickable {
                if (!isFavorite) onToggleFavorite(league)
                else onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(bgCircle),
                contentAlignment = Alignment.Center
            ) {
                league.logoUrl?.let { url ->
                    KamelImage(
                        resource = asyncPainterResource(url),
                        contentDescription = league.name,
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            if (isFavorite) {
                Image(
                    painter = painterResource(Res.drawable.ic_star_filled),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(28.dp)
                        .clickable { onToggleFavorite(league) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = league.name,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = textColor
        )
    }
}

@Composable
fun SearchResultsTopBar(
    title: String,
    theme: AppTheme,
    onBack: () -> Unit
) {
    val bgColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFF0DA160)
        AppTheme.DARK  -> Color.Transparent
    }

    val backIcon = when (theme) {
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

        Image(
            painter = backIcon,
            contentDescription = "Back",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(28.dp)
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

@Composable
private fun TeamResults(
    modifier: Modifier = Modifier,
    items: List<Team>,
    state: SearchState,
    viewModel: SearchViewModel,
    theme: AppTheme
) {
    if (items.isEmpty()) {
        EmptyState("Nothing here yet.", theme = theme)
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Most popular in your area",
            modifier = Modifier.padding(16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { team ->
                TeamCircleCard(
                    team = team,
                    isFavorite = team.id in state.favoriteTeams,
                    onToggleFavorite = { viewModel.toggleTeamFavorite(team) },
                    theme = theme
                )
            }
        }
    }
}

@Composable
private fun MatchResults(
    navController: NavController,
    modifier: Modifier = Modifier,
    items: List<Match>,
    theme: AppTheme
) {
    if (items.isEmpty()) {
        EmptyState(
            title = "Nothing found for the query.",
            subtitle = "Try changing your query or filters",
            theme = theme
        )
        return
    }

    var onlyFinished by remember { mutableStateOf(false) }
    var onlyScheduled by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {

        MatchFilters(
            onlyFinished = onlyFinished,
            onlyScheduled = onlyScheduled,
            onSelectStartTime = {
                onlyFinished = false
                onlyScheduled = false
            },
            onSelectFinished = {
                onlyFinished = true
                onlyScheduled = false
            },
            onSelectScheduled = {
                onlyFinished = false
                onlyScheduled = true
            },
            theme = theme
        )

        val filtered = items.filter { m ->
            when {
                onlyFinished  -> m.status.isFinished()
                onlyScheduled -> m.status.isScheduled()
                else -> true
            }
        }.sortedBy { it.startTime }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { match ->
                MatchCardSimple(match, navController, theme)
            }
        }
    }
}

@Composable
fun MatchFilters(
    onlyFinished: Boolean,
    onlyScheduled: Boolean,
    onSelectStartTime: () -> Unit,
    onSelectFinished: () -> Unit,
    onSelectScheduled: () -> Unit,
    theme: AppTheme
) {
    val containerColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFFEDEDED)
        AppTheme.DARK  -> Color(0xFF2A2A2A)
    }

    val selectedColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFF34C759)
        AppTheme.DARK  -> Color.White
    }

    val unselectedText = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color(0xFFCCCCCC)
    }

    val selectedText = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color.Black
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        FilterChipItem(
            text = "By start time",
            selected = !onlyFinished && !onlyScheduled,
            onClick = onSelectStartTime,
            selectedColor = selectedColor,
            selectedText = selectedText,
            unselectedText = unselectedText
        )

        Divider(
            modifier = Modifier
                .height(22.dp)
                .width(1.dp),
            color = if (theme == AppTheme.LIGHT) Color(0xFFBDBDBD) else Color(0xFF555555)
        )

        FilterChipItem(
            text = "Only finished",
            selected = onlyFinished,
            onClick = onSelectFinished,
            selectedColor = selectedColor,
            selectedText = selectedText,
            unselectedText = unselectedText
        )

        Divider(
            modifier = Modifier
                .height(22.dp)
                .width(1.dp),
            color = if (theme == AppTheme.LIGHT) Color(0xFFBDBDBD) else Color(0xFF555555)
        )

        FilterChipItem(
            text = "Only scheduled",
            selected = onlyScheduled,
            onClick = onSelectScheduled,
            selectedColor = selectedColor,
            selectedText = selectedText,
            unselectedText = unselectedText
        )
    }
}

@Composable
private fun FilterChipItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    selectedText: Color,
    unselectedText: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) selectedColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) selectedText else unselectedText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


private fun MatchStatus.isFinished() =
    this == MatchStatus.FINISHED

private fun MatchStatus.isScheduled() =
    this == MatchStatus.SCHEDULED

@Composable
private fun ArticleResults(
    navController: NavController,
    modifier: Modifier = Modifier,
    items: List<Article>,
    theme: AppTheme
) {
    if (items.isEmpty()) {
        EmptyState(
            title = "Nothing found for the query.",
            subtitle = "Try changing your query or filters",
            theme = theme
        )
        return
    }

    var sortByDate by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize()) {

        SortSwitch(
            sortByDate = sortByDate,
            onChange = { sortByDate = it },
            theme = theme
        )

        Spacer(Modifier.height(12.dp))

        val sorted = if (sortByDate) {
            items.sortedByDescending { it.publishedAt }
        } else {
            items.sortedByDescending { (it.summary ?: "").length }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sorted) { article ->
                ArticleCard(article, navController, theme = theme)
            }
        }
    }
}

@Composable
private fun SortSwitch(
    sortByDate: Boolean,
    onChange: (Boolean) -> Unit,
    theme: AppTheme
) {
    val bgContainer = when (theme) {
        AppTheme.LIGHT -> Color(0xFFEDEDED)
        AppTheme.DARK  -> Color(0xFF2A2A2A)
    }

    val selectedBg = when (theme) {
        AppTheme.LIGHT -> Color(0xFF06C167)
        AppTheme.DARK  -> Color.White
    }

    val selectedText = when (theme) {
        AppTheme.LIGHT -> Color.White
        AppTheme.DARK  -> Color.Black
    }

    val unselectedText = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color(0xFFB5B5B5)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier
                .background(bgContainer, RoundedCornerShape(20.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (sortByDate) selectedBg else Color.Transparent)
                    .clickable { onChange(true) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "By date",
                    color = if (sortByDate) selectedText else unselectedText
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color.Gray.copy(alpha = 0.4f))
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (!sortByDate) selectedBg else Color.Transparent)
                    .clickable { onChange(false) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "By popularity",
                    color = if (!sortByDate) selectedText else unselectedText
                )
            }
        }
    }
}