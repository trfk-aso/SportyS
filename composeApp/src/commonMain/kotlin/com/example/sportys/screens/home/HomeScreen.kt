package com.example.sportys.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sportys.model.Article
import com.example.sportys.model.Match
import com.example.sportys.screens.Screen
import com.example.sportys.screens.bottombar.AppBottomBar
import com.example.sportys.screens.search.ContentType
import com.example.sportys.screens.search.SearchViewModel
import com.example.sportys.screens.search.TimeRange
import com.example.sportys.screens.settings.AppTheme
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.bg_settings_dark
import sportys.composeapp.generated.resources.bg_settings_light
import sportys.composeapp.generated.resources.ic_search_icon
import sportys.composeapp.generated.resources.ic_settings
import sportys.composeapp.generated.resources.news_placeholder

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinInject(),
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
                HomeTopBar(
                    onSearchClick = { navController.navigate(Screen.Search.route) },
                    onSettingsClick = { navController.navigate(Screen.Setting.route) },
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

            when {
                state.isLoading -> HomeLoading()

                state.error != null -> HomeError(
                    state.error!!,
                    viewModel::refreshAll
                )

                else -> HomeContent(
                    state,
                    Modifier.padding(padding),
                    navController,
                    theme
                )
            }
        }
    }
}

@Composable
fun HomeTopBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    theme: AppTheme
) {
    val bgColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFF0DA160)
        AppTheme.DARK  -> Color.Transparent
    }

    val iconTint = when (theme) {
        AppTheme.LIGHT -> Color.White
        AppTheme.DARK  -> Color.White
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
            painter = painterResource(Res.drawable.ic_settings),
            contentDescription = "Settings",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(26.dp)
                .clickable { onSettingsClick() },
            colorFilter = ColorFilter.tint(iconTint)
        )

        Text(
            text = "Today in Football",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )

        Image(
            painter = painterResource(Res.drawable.ic_search_icon),
            contentDescription = "Search",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(26.dp)
                .clickable { onSearchClick() },
            colorFilter = ColorFilter.tint(iconTint)
        )
    }
}

@Composable
fun HomeContent(state: HomeScreenState, modifier: Modifier = Modifier, navController: NavController, theme: AppTheme) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        FeaturedBlock(state.topArticles, navController, theme)

        QuickAccessBlock(navController, theme)

        NewsSection(state.latestNews, navController, theme)

        MatchesSection(state.todayMatches, navController, theme)

        ForYouSection(state.recommended, navController, theme)

        PopularSection(state.recommended.shuffled(), navController, theme)
    }
}

@Composable
fun FeaturedBlock(
    articles: List<Article>,
    navController: NavController,
    theme: AppTheme
) {
    if (articles.isEmpty()) return

    val textColor: Color
    val secondaryColor: Color
    val cardColor: Color
    val borderColor: Color

    when (theme) {
        AppTheme.LIGHT -> {
            textColor = Color.Black
            secondaryColor = Color.Gray
            cardColor = Color.White
            borderColor = Color(0xFFE0E0E0)
        }
        AppTheme.DARK -> {
            textColor = Color.White
            secondaryColor = Color(0xFFBBBBBB)
            cardColor = Color(0xFF1E1E1E)
            borderColor = Color.Transparent
        }
    }

    Text(
        "Main Featured Block",
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

    val featured = articles.first()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(
                width = if (theme == AppTheme.LIGHT) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                navController.navigate("details/article/${featured.id}")
            }
            .padding(12.dp)
    ) {
        KamelImage(
            resource = asyncPainterResource(featured.imageUrl ?: ""),
            contentDescription = null,
            onFailure = {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (theme == AppTheme.DARK)
                                Color(0xFF2C2C2C)
                            else
                                Color(0xFFF0F0F0)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.news_placeholder),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.height(110.dp)
        ) {
            Text(
                featured.title,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = textColor
            )

            Text(
                formatArticleDate(featured.publishedAt),
                color = secondaryColor,
                fontSize = 13.sp
            )
        }
    }
}

fun formatArticleDate(instant: Instant): String {
    val now = Clock.System.now()
    val diff = now - instant

    val minutes = diff.inWholeMinutes
    val hours = diff.inWholeHours
    val days = diff.inWholeDays

    return when {
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> {
            val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${local.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${local.dayOfMonth}, ${local.year}"
        }
    }
}

fun formatMatchDateTime(start: Instant): String {
    val now = Clock.System.now()
    val zone = TimeZone.currentSystemDefault()

    val localStart = start.toLocalDateTime(zone)
    val localNow = now.toLocalDateTime(zone)

    val dateLabel = when (localStart.date) {
        localNow.date -> "Today"
        localNow.date.plus(DatePeriod(days = 1)) -> "Tomorrow"
        else -> {
            val month = localStart.month.name.lowercase().replaceFirstChar { it.uppercase() }
            "$month ${localStart.dayOfMonth}"
        }
    }

    val hour = localStart.hour.toString().padStart(2, '0')
    val minute = localStart.minute.toString().padStart(2, '0')

    return "$hour:$minute · $dateLabel"
}

fun formatNewsDate(instant: Instant): String {
    val zone = TimeZone.currentSystemDefault()
    val localDate = instant.toLocalDateTime(zone).date

    val today = Clock.System.now().toLocalDateTime(zone).date
    val yesterday = today.minus(DatePeriod(days = 1))

    return when (localDate) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> {
            val month = localDate.month.name.lowercase()
                .replaceFirstChar { it.uppercase() }

            "$month ${localDate.dayOfMonth}, ${localDate.year}"
        }
    }
}

@Composable
fun HomeLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
fun QuickAccessBlock(navController: NavController, theme: AppTheme) {

    val searchViewModel: SearchViewModel = koinInject()

    val textColor = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color.White
    }

    Text(
        "Quick Access",
        color = textColor,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    val buttons = listOf(
        "My Teams", "Top Leagues",
        "Matches Today", "Standings"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (row in buttons.chunked(2)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { label ->
                    QuickButton(
                        text = label,
                        theme = theme,
                        onClick = {
                            when (label) {
                                "My Teams" -> navController.navigate(Screen.Favorites.route)

                                "Top Leagues" -> {
                                    searchViewModel.setContentType(ContentType.LEAGUES)
                                    searchViewModel.onQueryChange("")
                                    searchViewModel.applySearch()
                                    navController.navigate(Screen.SearchResults.route)
                                }

                                "Matches Today" -> {
                                    searchViewModel.setContentType(ContentType.MATCHES)
                                    searchViewModel.setTimeRange(TimeRange.TODAY)
                                    searchViewModel.onQueryChange("")
                                    searchViewModel.applySearch()
                                    navController.navigate(Screen.SearchResults.route)
                                }

                                "Standings" -> navController.navigate(Screen.Statistics.route)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.QuickButton(
    text: String,
    onClick: () -> Unit,
    theme: AppTheme
) {
    val bgColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFF65EFB1)
        AppTheme.DARK  -> Color(0xFF0A2F1A)
    }

    val textColor = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color.White
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun NewsSection(
    articles: List<Article>,
    navController: NavController,
    theme: AppTheme
) {
    val textColor = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color.White
    }

    Text(
        "News",
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

    val rows = articles.take(6).chunked(2)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { article ->
                    NewsCard(article, navController, theme)
                }
            }
        }
    }
}

@Composable
fun RowScope.NewsCard(
    article: Article,
    navController: NavController,
    theme: AppTheme
) {
    val cardColor: Color
    val borderColor: Color
    val titleColor: Color
    val dateColor: Color
    val placeholderColor: Color

    when (theme) {
        AppTheme.LIGHT -> {
            cardColor = Color.White
            borderColor = Color(0xFFE0E0E0)
            titleColor = Color.Black
            dateColor = Color.Gray
            placeholderColor = Color(0xFFF0F0F0)
        }
        AppTheme.DARK -> {
            cardColor = Color(0xFF1E1E1E)
            borderColor = Color.Transparent
            titleColor = Color.White
            dateColor = Color(0xFFBBBBBB)
            placeholderColor = Color(0xFF2C2C2C)
        }
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(
                width = if (theme == AppTheme.LIGHT) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                navController.navigate("details/article/${article.id}")
            }
            .padding(12.dp)
    ) {
        KamelImage(
            resource = asyncPainterResource(article.imageUrl ?: ""),
            contentDescription = null,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            onFailure = {
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(placeholderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.news_placeholder),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        )

        Spacer(Modifier.height(8.dp))

        Text(
            article.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = titleColor
        )

        Spacer(Modifier.height(6.dp))

        Text(
            formatNewsDate(article.publishedAt),
            color = dateColor,
            fontSize = 12.sp
        )
    }
}

@Composable
fun MatchesSection(matches: List<Match>, navController: NavController, theme: AppTheme) {
    if (matches.isEmpty()) return

    val textColor = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color.White
    }

    Text(
        "Matches Today",
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        matches.take(3).forEach { match ->
            MatchCard(match, navController, theme)
        }
    }
}

@Composable
fun MatchCard(
    match: Match,
    navController: NavController,
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(
                width = if (theme == AppTheme.LIGHT) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                navController.navigate("details/match/${match.id}")
            }
            .padding(16.dp)
    ) {
        Text(
            "${match.homeTeam.name} — ${match.awayTeam.name}",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = titleColor
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "${formatMatchDateTime(match.startTime)} · ${match.league.name}",
            color = subtitleColor,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ForYouSection(articles: List<Article>, navController: NavController, theme: AppTheme) {

    val textColor = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color.White
    }

    Text(
        "For You",
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        articles.forEach { article ->
            FeaturedRowCard(article, navController, theme)
        }
    }
}

@Composable
fun FeaturedRowCard(
    article: Article,
    navController: NavController,
    theme: AppTheme
) {
    val cardColor: Color
    val borderColor: Color
    val titleColor: Color
    val dateColor: Color
    val placeholderColor: Color

    when (theme) {
        AppTheme.LIGHT -> {
            cardColor = Color.White
            borderColor = Color(0xFFE0E0E0)
            titleColor = Color.Black
            dateColor = Color.Gray
            placeholderColor = Color(0xFFF0F0F0)
        }
        AppTheme.DARK -> {
            cardColor = Color(0xFF1E1E1E)
            borderColor = Color.Transparent
            titleColor = Color.White
            dateColor = Color(0xFFBBBBBB)
            placeholderColor = Color(0xFF2C2C2C)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(
                width = if (theme == AppTheme.LIGHT) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                navController.navigate("details/article/${article.id}")
            }
            .padding(12.dp)
    ) {

        KamelImage(
            resource = asyncPainterResource(article.imageUrl ?: ""),
            contentDescription = null,
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            onFailure = {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(placeholderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.news_placeholder),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                article.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )

            Spacer(Modifier.height(4.dp))

            Text(
                formatArticleDate(article.publishedAt),
                color = dateColor,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun PopularSection(articles: List<Article>, navController: NavController, theme: AppTheme) {

    val textColor = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color.White
    }

    Text("Popular / Recommended", color = textColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        articles.take(4).forEach {
            FeaturedRowCard(it, navController, theme)
        }
    }
}

@Composable
fun HomeError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Failed to load data:\n$message")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Try again") }
    }
}