package com.example.sportys.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sportys.model.Article
import com.example.sportys.model.Match
import com.example.sportys.screens.bottombar.AppBottomBar
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.news_placeholder

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = {
            AppBottomBar(
                currentRoute = currentRoute,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
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
                Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun HomeTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0DA160))
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                start = 16.dp,
                end = 16.dp,
                bottom = 12.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Today in Football",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HomeContent(state: HomeScreenState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        FeaturedBlock(state.topArticles)

        QuickAccessBlock()

        NewsSection(state.latestNews)

        MatchesSection(state.todayMatches)

        ForYouSection(state.recommended)

        PopularSection(state.recommended.shuffled())
    }
}

@Composable
fun FeaturedBlock(articles: List<Article>) {
    if (articles.isEmpty()) return

    Text(
        "Main Featured Block",
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        articles.take(1).forEach { article ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                val imageUrl = article.imageUrl
                val hasValidImage = !imageUrl.isNullOrBlank() && imageUrl.startsWith("http")

                KamelImage(
                    resource = asyncPainterResource(imageUrl ?: ""),
                    contentDescription = null,
                    onFailure = {
                        Image(
                            painter = painterResource(Res.drawable.news_placeholder),
                            contentDescription = null,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
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
                        article.title,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )

                    Text(
                        "4 hours ago",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
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
fun QuickAccessBlock() {
    Text(
        "Quick Access",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    val buttons = listOf(
        "My Teams", "Top Leagues",
        "Matches Today", "Standings"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (row in buttons.chunked(2)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach {
                    QuickButton(text = it)
                }
            }
        }
    }
}


@Composable
fun RowScope.QuickButton(text: String) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF65EFB1))
            .clickable { }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun NewsSection(articles: List<Article>) {
    Text("News", fontWeight = FontWeight.Bold, fontSize = 20.sp)

    val rows = articles.take(6).chunked(2)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { article ->
                    NewsCard(article)
                }
            }
        }
    }
}

@Composable
fun RowScope.NewsCard(article: Article) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        KamelImage(
            resource = asyncPainterResource(article.imageUrl ?: ""),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.height(8.dp))

        Text(
            article.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        Text("Aug 12, 2025", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun MatchesSection(matches: List<Match>) {
    Text("Matches Today", fontWeight = FontWeight.Bold, fontSize = 20.sp)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        matches.forEach { match ->
            MatchCard(match)
        }
    }
}

@Composable
fun MatchCard(match: Match) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            "${match.homeTeam.name} — ${match.awayTeam.name}",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "18:30 · ${match.league.name}",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ForYouSection(articles: List<Article>) {
    Text("For You", fontWeight = FontWeight.Bold, fontSize = 20.sp)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        articles.forEach {
            FeaturedRowCard(it)
        }
    }
}

@Composable
fun FeaturedRowCard(article: Article) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        KamelImage(
            resource = asyncPainterResource(article.imageUrl ?: ""),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                article.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text("2 hours ago", color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
fun PopularSection(articles: List<Article>) {
    Text("Popular / Recommended", fontWeight = FontWeight.Bold, fontSize = 20.sp)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        articles.take(4).forEach {
            FeaturedRowCard(it)
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