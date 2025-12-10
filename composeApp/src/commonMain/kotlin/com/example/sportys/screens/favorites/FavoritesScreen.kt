package com.example.sportys.screens.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sportys.model.Article
import com.example.sportys.model.FavoriteItem
import com.example.sportys.model.FavoriteType
import com.example.sportys.model.League
import com.example.sportys.model.Match
import com.example.sportys.model.MatchStatus
import com.example.sportys.model.Team
import com.example.sportys.screens.Screen
import com.example.sportys.screens.bottombar.AppBottomBar
import com.example.sportys.screens.details.displayName
import com.example.sportys.screens.home.formatMatchDateTime
import com.example.sportys.screens.search.LoadingState
import com.example.sportys.screens.settings.AppTheme
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.bg_settings_dark
import sportys.composeapp.generated.resources.bg_settings_light
import sportys.composeapp.generated.resources.ic_back
import sportys.composeapp.generated.resources.ic_favorite_filled
import sportys.composeapp.generated.resources.ic_search_icon
import sportys.composeapp.generated.resources.ic_settings
import sportys.composeapp.generated.resources.ic_star_filled

@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = koinInject(),
    theme: AppTheme
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.setTab(FavoriteTab.TEAMS)
    }
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
                FavoritesTopBarDynamic(
                    title = "Favorites",
                    theme = theme
                )
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
            ) {

                FavoritesTabs(
                    selected = state.tab,
                    onSelect = viewModel::setTab,
                    theme = theme
                )

                when {
                    state.isLoading -> LoadingState("Loading…", theme = theme)

                    state.isAllEmpty() -> EmptyFavorites(theme)

                    else -> when (state.tab) {
                        FavoriteTab.TEAMS    -> TeamsList(state.teams, viewModel, theme)
                        FavoriteTab.LEAGUES  -> LeaguesList(state.leagues, viewModel, theme)
                        FavoriteTab.MATCHES  -> MatchesList(state.matches, viewModel, navController, theme)
                        FavoriteTab.ARTICLES -> ArticlesList(state.articles, viewModel, navController, theme)
                    }
                }
            }
        }
    }
}
private fun FavoritesState.isAllEmpty(): Boolean {
    return teams.isEmpty() &&
            leagues.isEmpty() &&
            matches.isEmpty() &&
            articles.isEmpty()
}

@Composable
fun FavoritesTabs(
    selected: FavoriteTab,
    onSelect: (FavoriteTab) -> Unit,
    theme: AppTheme
) {
    val containerColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFFF2F2F2)
        AppTheme.DARK  -> Color(0xFF1E1E1E)
    }

    val dividerColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFFBDBDBD)
        AppTheme.DARK  -> Color(0xFF555555)
    }

    Row(
        modifier = Modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        TabItem(
            text = "Teams",
            selected = selected == FavoriteTab.TEAMS,
            onClick = { onSelect(FavoriteTab.TEAMS) },
            theme = theme
        )

        TabDivider(dividerColor)

        TabItem(
            text = "Leagues",
            selected = selected == FavoriteTab.LEAGUES,
            onClick = { onSelect(FavoriteTab.LEAGUES) },
            theme = theme
        )

        TabDivider(dividerColor)

        TabItem(
            text = "Matches",
            selected = selected == FavoriteTab.MATCHES,
            onClick = { onSelect(FavoriteTab.MATCHES) },
            theme = theme
        )

        TabDivider(dividerColor)

        TabItem(
            text = "Articles",
            selected = selected == FavoriteTab.ARTICLES,
            onClick = { onSelect(FavoriteTab.ARTICLES) },
            theme = theme
        )
    }
}

@Composable
private fun TabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    theme: AppTheme
) {
    val selectedBg = Color(0xFF39B36D)

    val textColor = when {
        selected -> Color.White
        theme == AppTheme.LIGHT -> Color.Black
        else -> Color(0xFFDDDDDD)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) selectedBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun TabDivider(color: Color) {
    Box(
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
            .background(color)
    )
}


@Composable
fun Tab(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(if (selected) Color(0xFF0DA160) else Color(0xFFEAEAEA))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            color = if (selected) Color.White else Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FavoritesTopBarDynamic(
    title: String,
    theme: AppTheme
) {
    val bgColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFF0DA160)
        AppTheme.DARK  -> Color.Transparent
    }

    val iconTint = Color.White

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
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun EmptyFavorites(theme: AppTheme) {

    val titleColor: Color
    val subtitleColor: Color

    when (theme) {
        AppTheme.LIGHT -> {
            titleColor = Color.Black
            subtitleColor = Color.Gray
        }
        AppTheme.DARK -> {
            titleColor = Color.White
            subtitleColor = Color(0xFFBBBBBB)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            "Your favorites are empty so far.",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Add teams, leagues, matches or articles using the star icon.",
            fontSize = 14.sp,
            color = subtitleColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TeamsList(items: List<Team>, vm: FavoritesViewModel, theme: AppTheme) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { team ->
            FavoriteTeamCard(
                team = team,
                onRemove = { vm.removeFavorite(FavoriteType.TEAM, team.id) },
                theme = theme
            )
        }
    }
}

@Composable
fun FavoriteTeamCard(
    team: Team,
    onRemove: () -> Unit,
    theme: AppTheme
) {
    val nameColor = if (theme == AppTheme.DARK) Color.White else Color.Black

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {

        Box(
            modifier = Modifier.size(85.dp),
            contentAlignment = Alignment.Center
        ) {

            val logo = team.logoUrl

            if (logo.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            if (theme == AppTheme.DARK) Color(0xFF2A2A2A)
                            else Color.LightGray
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        team.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = nameColor
                    )
                }
            } else {
                KamelImage(
                    resource = asyncPainterResource(logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Image(
                painter = painterResource(Res.drawable.ic_star_filled),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(24.dp)
                    .clickable { onRemove() }
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            team.name,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = nameColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LeaguesList(
    items: List<League>,
    vm: FavoritesViewModel,
    theme: AppTheme
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { league ->
            FavoriteLeagueCard(
                league = league,
                onRemove = { vm.removeFavorite(FavoriteType.LEAGUE, league.id) },
                theme = theme
            )
        }
    }
}

@Composable
fun MatchesList(
    items: List<Match>,
    vm: FavoritesViewModel,
    navController: NavController,
    theme: AppTheme
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { match ->
            MatchFavoriteCard(
                match = match,
                navController = navController,
                onRemove = { vm.removeFavorite(FavoriteType.MATCH, match.id) },
                theme = theme
            )
        }
    }
}

@Composable
fun ArticlesList(
    items: List<Article>,
    vm: FavoritesViewModel,
    navController: NavController,
    theme: AppTheme
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { article ->
            ArticleCardSmall(
                article = article,
                onClick = {
                    navController.navigate("details/article/${article.id}")
                },
                onRemove = {
                    vm.removeFavorite(FavoriteType.ARTICLE, article.id)
                },
                theme = theme
            )
        }
    }
}

@Composable
fun FavoriteLeagueCard(
    league: League,
    onRemove: () -> Unit,
    theme: AppTheme
) {
    val textColor = if (theme == AppTheme.DARK) Color.White else Color.Black

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {

        Box(
            modifier = Modifier.size(85.dp),
            contentAlignment = Alignment.Center
        ) {

            val logo = league.logoUrl

            if (logo.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            if (theme == AppTheme.DARK)
                                Color(0xFF2A2A2A)
                            else
                                Color.LightGray
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        league.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            } else {
                KamelImage(
                    resource = asyncPainterResource(logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Image(
                painter = painterResource(Res.drawable.ic_star_filled),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(24.dp)
                    .clickable { onRemove() }
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = league.name,
            fontSize = 12.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MatchFavoriteCard(
    match: Match,
    navController: NavController,
    onRemove: () -> Unit,
    theme: AppTheme
) {
    val cardColor: Color
    val borderColor: Color
    val titleColor: Color
    val subtitleColor: Color

    when (theme) {
        AppTheme.LIGHT -> {
            cardColor = Color.White
            borderColor = Color(0xFFE0E0E0)
            titleColor = Color.Black
            subtitleColor = Color.Gray
        }
        AppTheme.DARK -> {
            cardColor = Color(0xFF1E1E1E)
            borderColor = Color.Transparent
            titleColor = Color.White
            subtitleColor = Color(0xFFBBBBBB)
        }
    }

    val timeLabel = remember(match.startTime) { formatMatchDateTime(match.startTime) }
    val statusText = match.status.displayName()
    val statusColor =
        if (match.status == MatchStatus.LIVE) Color(0xFF34C759) else subtitleColor

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardColor)
            .border(
                width = if (theme == AppTheme.LIGHT) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
    ) {

        Icon(
            painter = painterResource(Res.drawable.ic_favorite_filled),
            contentDescription = "Remove",
            tint = Color(0xFFFFD700),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clickable { onRemove() }
        )

        Column(
            modifier = Modifier
                .clickable {
                    navController.navigate("details/match/${match.id}")
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(Modifier.weight(1f)) {

                    TeamRow(
                        name = match.homeTeam.name,
                        logoUrl = match.homeTeam.logoUrl,
                        bold = true,
                        textColor = titleColor
                    )

                    Spacer(Modifier.height(6.dp))

                    TeamRow(
                        name = match.awayTeam.name,
                        logoUrl = match.awayTeam.logoUrl,
                        bold = false,
                        textColor = titleColor
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(end = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = match.score?.home?.toString() ?: "-",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = match.score?.away?.toString() ?: "-",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = subtitleColor,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = timeLabel,
                    color = subtitleColor,
                    fontSize = 13.sp
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "Status: $statusText",
                    color = statusColor,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun TeamRow(
    name: String,
    logoUrl: String?,
    bold: Boolean = false,
    textColor: Color = Color.Black,
    placeholderColor: Color = Color.LightGray
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (!logoUrl.isNullOrBlank()) {

            KamelImage(
                resource = asyncPainterResource(logoUrl),
                contentDescription = name,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

        } else {

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(placeholderColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = name,
            fontSize = 15.sp,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
fun ArticleCardSmall(
    article: Article,
    onClick: () -> Unit = {},
    onRemove: () -> Unit,
    theme: AppTheme
) {
    val cardColor: Color
    val borderColor: Color
    val titleColor: Color
    val subtitleColor: Color

    when (theme) {
        AppTheme.LIGHT -> {
            cardColor = Color.White
            borderColor = Color(0xFFE0E0E0)
            titleColor = Color.Black
            subtitleColor = Color.Gray
        }
        AppTheme.DARK -> {
            cardColor = Color(0xFF1E1E1E)
            borderColor = Color.Transparent
            titleColor = Color.White
            subtitleColor = Color(0xFFBBBBBB)
        }
    }

    Box(
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(
                width = if (theme == AppTheme.LIGHT) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {

        Column {

            Box {
                KamelImage(
                    resource = asyncPainterResource(article.imageUrl ?: ""),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                Icon(
                    painter = painterResource(Res.drawable.ic_favorite_filled),
                    tint = Color(0xFFFFD700),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(20.dp)
                        .clickable { onRemove() }
                )
            }

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                Text(
                    article.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = titleColor
                )

                Text(
                    article.publishedAt.toString(),
                    fontSize = 12.sp,
                    color = subtitleColor
                )
            }
        }
    }
}