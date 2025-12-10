package com.example.sportys.screens.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.sportys.model.Article
import com.example.sportys.model.Event
import com.example.sportys.model.Match
import com.example.sportys.model.MatchStatus
import com.example.sportys.model.Team
import com.example.sportys.model.TeamLeagueStats
import com.example.sportys.model.TeamShort
import com.example.sportys.screens.settings.AppTheme
import com.example.sportys.share.getShareManager
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.bg_settings_dark
import sportys.composeapp.generated.resources.bg_settings_light
import sportys.composeapp.generated.resources.ic_back
import sportys.composeapp.generated.resources.ic_favorite_filled
import sportys.composeapp.generated.resources.ic_favorite_outline
import sportys.composeapp.generated.resources.ic_share

@Composable
fun DetailsScreen(
    navController: NavController,
    theme: AppTheme
) {
    val viewModel: DetailsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

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
                DetailsTopBar(
                    title = when (state.mode) {
                        DetailMode.Article -> "News"
                        DetailMode.Match -> "Match"
                        DetailMode.Team -> "Team"
                    },
                    isFavorite = state.isFavorite,
                    onBack = { navController.popBackStack() },
                    onShare = { viewModel.share(getShareManager()) },
                    onFavorite = { viewModel.toggleFavorite() },
                    theme = theme
                )
            }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                when {
                    state.isLoading -> DetailsLoading(theme)
                    state.error != null -> DetailsError(state.error!!)
                    else -> {
                        when (state.mode) {

                            DetailMode.Article ->
                                ArticleDetails(state.article!!, theme)

                            DetailMode.Match ->
                                MatchDetails(
                                    match = state.match!!,
                                    onClose = { navController.popBackStack() },
                                    onRefresh = { viewModel.loadDetails() },
                                    theme = theme
                                )

                            DetailMode.Team ->
                                TeamDetails(
                                    team = state.team!!,
                                    stats = state.teamStats,
                                    theme = theme
                                )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsLoading(theme: AppTheme) {

    val indicatorColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFF0DA160)
        AppTheme.DARK  -> Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = indicatorColor
        )
    }
}

@Composable
fun DetailsError(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Error",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )

            Text(
                text = message,
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DetailsTopBar(
    title: String,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onFavorite: () -> Unit,
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

        Image(
            painter = painterResource(Res.drawable.ic_back),
            contentDescription = null,
            modifier = Modifier
                .size(26.dp)
                .align(Alignment.CenterStart)
                .clickable(onClick = onBack),
            colorFilter = ColorFilter.tint(iconTint)
        )

        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(Res.drawable.ic_share),
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onShare),
                colorFilter = ColorFilter.tint(iconTint)
            )

            Spacer(Modifier.width(16.dp))

            val favoriteIcon =
                if (isFavorite) Res.drawable.ic_favorite_filled
                else Res.drawable.ic_favorite_outline

            Image(
                painter = painterResource(favoriteIcon),
                contentDescription = null,
                modifier = Modifier
                    .size(25.dp)
                    .clickable(onClick = onFavorite)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleDetails(article: Article, theme: AppTheme) {

    val titleColor: Color
    val textColor: Color
    val subtitleColor: Color
    val tagBgColor: Color
    val tagTextColor: Color
    val dividerColor: Color

    when (theme) {
        AppTheme.LIGHT -> {
            titleColor = Color.Black
            textColor = Color.Black
            subtitleColor = Color.Gray
            tagBgColor = Color(0xFFEFEFEF)
            tagTextColor = Color.Black
            dividerColor = Color(0xFFDDDDDD)
        }

        AppTheme.DARK -> {
            titleColor = Color.White
            textColor = Color.White
            subtitleColor = Color(0xFFBBBBBB)
            tagBgColor = Color(0x33FFFFFF)
            tagTextColor = Color.White
            dividerColor = Color(0x44FFFFFF)
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        KamelImage(
            resource = asyncPainterResource(article.imageUrl ?: ""),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = article.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor
        )

        Text(
            text = "Today, ${article.publishedAt} · ${article.source ?: "Unknown"}",
            color = subtitleColor,
            fontSize = 14.sp
        )

        if (article.tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                article.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(tagBgColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag.toString(),
                            color = tagTextColor
                        )
                    }
                }
            }
        }

        Divider(color = dividerColor)

        Text(
            text = article.content ?: "No article content.",
            fontSize = 16.sp,
            lineHeight = 22.sp,
            color = textColor
        )
    }
}

@Composable
fun MatchDetails(
    match: Match,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    theme: AppTheme
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        MatchHeader(match, theme)

        MatchMeta(match, theme)

        MatchLineups(match, theme)

        MatchStats(match, theme)

        MatchEvents(match, theme)

        if (match.status == MatchStatus.LIVE) {
            LiveActionButtons(
                onClose = onClose,
                onRefresh = onRefresh
            )
        }
    }
}

@Composable
fun LiveActionButtons(
    onClose: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 12.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFCFCFCF))
                .clickable(onClick = onClose)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Close", fontSize = 16.sp, color = Color.Black)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0DA160))
                .clickable(onClick = onRefresh)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Refresh", fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun MatchHeader(match: Match, theme: AppTheme) {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TeamBlock(match.homeTeam, theme)
            ScoreBlock(match, theme)
            TeamBlock(match.awayTeam, theme)
        }
    }
}

@Composable
fun TeamBlock(team: TeamShort, theme: AppTheme) {

    val textColor =
        if (theme == AppTheme.LIGHT) Color.Black else Color.White

    Column(
        modifier = Modifier.width(90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KamelImage(
            resource = asyncPainterResource(team.logoUrl ?: ""),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = team.name,
            color = textColor,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ScoreBlock(match: Match, theme: AppTheme) {

    val numberColor =
        if (theme == AppTheme.LIGHT) Color.Black else Color.White

    val subtitleColor =
        if (theme == AppTheme.LIGHT) Color.Gray else Color(0xFFBBBBBB)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = match.score?.home?.toString() ?: "-",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = numberColor
            )

            Text(
                text = " : ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = numberColor
            )

            Text(
                text = match.score?.away?.toString() ?: "-",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = numberColor
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = match.status.displayName(),
            color = subtitleColor,
            fontSize = 14.sp
        )
    }
}

fun MatchStatus.displayName(): String {
    return when (this) {
        MatchStatus.SCHEDULED -> "Scheduled"
        MatchStatus.LIVE -> "Live"
        MatchStatus.FINISHED -> "Finished"
        MatchStatus.POSTPONED -> "Postponed"
        MatchStatus.CANCELED -> "Canceled"
    }
}

@Composable
fun MatchMeta(match: Match, theme: AppTheme) {

    val labelColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White
    val valueColor = if (theme == AppTheme.LIGHT) Color.DarkGray else Color(0xFFBBBBBB)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

        MetaItem(
            label = "League:",
            value = match.league.name,
            labelColor = labelColor,
            valueColor = valueColor
        )

        val local = match.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
        MetaItem(
            label = "Kick-off:",
            value = "${local.hour}:${local.minute.toString().padStart(2, '0')}",
            labelColor = labelColor,
            valueColor = valueColor
        )
    }
}

@Composable
fun MetaItem(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = labelColor)
        Text(value, color = valueColor)
    }
}

@Composable
fun MatchLineups(match: Match, theme: AppTheme) {

    val titleColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White
    val subtitleColor = if (theme == AppTheme.LIGHT) Color.Gray else Color(0xFFBBBBBB)
    val textColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White

    var selectedTeam by remember { mutableStateOf(LineupTab.HOME) }

    Text("Lineups", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = titleColor)
    Spacer(Modifier.height(12.dp))

    LineupsSwitcher(
        homeName = match.homeTeam.name,
        awayName = match.awayTeam.name,
        selected = selectedTeam,
        onSelect = { selectedTeam = it },
        theme = theme
    )
    Spacer(Modifier.height(16.dp))

    if (match.lineups.isEmpty()) {
        Text("Lineups unavailable", color = subtitleColor)
        return
    }

    val lineup = when (selectedTeam) {
        LineupTab.HOME -> match.lineups.firstOrNull { it.team == match.homeTeam.name }
        LineupTab.AWAY -> match.lineups.firstOrNull { it.team == match.awayTeam.name }
    } ?: return

    Text(lineup.team, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = titleColor)
    Spacer(Modifier.height(8.dp))

    lineup.startXI.forEach { p ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {

            if (p.photo != null) {
                KamelImage(
                    resource = asyncPainterResource(p.photo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = null, tint = titleColor)
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = "${p.number}. ${p.name} (${p.position})",
                color = textColor,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun MatchStats(match: Match, theme: AppTheme) {

    val titleColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White
    val textColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White

    if (match.statistics.isEmpty()) return

    Text("Statistics", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = titleColor)

    val stats = match.statistics.firstOrNull()?.stats ?: emptyList()

    val mapping = mapOf(
        "Total Shots" to "Shots",
        "Shots on Goal" to "Shots on goal",
        "Ball Possession" to "Possession",
        "Fouls" to "Fouls",
        "Corner Kicks" to "Corners",
        "Expected Goals" to "xG",
        "xG" to "xG"
    )

    val orderedKeys = listOf("Shots", "Possession", "Fouls", "Corners", "xG")

    orderedKeys.forEach { displayName ->

        val apiEntry = stats.firstOrNull { stat ->
            val normalized = mapping[stat.type] ?: stat.type
            normalized.equals(displayName, ignoreCase = true)
        }

        if (apiEntry != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$displayName:", color = textColor, fontSize = 16.sp)
                Text(apiEntry.value ?: "-", color = textColor, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun LineupsSwitcher(
    homeName: String,
    awayName: String,
    selected: LineupTab,
    onSelect: (LineupTab) -> Unit,
    theme: AppTheme
) {
    val bgColor = if (theme == AppTheme.LIGHT) Color(0xFFE7E7E7) else Color(0xFF333333)
    val activeBg = if (theme == AppTheme.LIGHT) Color.White else Color(0xFF1E1E1E)
    val borderColor = if (theme == AppTheme.LIGHT) Color(0xFFD5D5D5) else Color(0xFF555555)
    val activeText = if (theme == AppTheme.LIGHT) Color.Black else Color.White
    val inactiveText = if (theme == AppTheme.LIGHT) Color.DarkGray else Color(0xFFBBBBBB)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(30.dp))
            .border(1.dp, borderColor, RoundedCornerShape(30.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
                .background(if (selected == LineupTab.HOME) activeBg else Color.Transparent)
                .clickable { onSelect(LineupTab.HOME) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = homeName,
                color = if (selected == LineupTab.HOME) activeText else inactiveText,
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
                .background(if (selected == LineupTab.AWAY) activeBg else Color.Transparent)
                .clickable { onSelect(LineupTab.AWAY) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = awayName,
                color = if (selected == LineupTab.AWAY) activeText else inactiveText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MatchEvents(match: Match, theme: AppTheme) {

    val titleColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White
    val subtitleColor = if (theme == AppTheme.LIGHT) Color.Gray else Color(0xFFBBBBBB)

    Text("Event timeline", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = titleColor)

    if (match.events.isEmpty()) {
        Text("No events", color = subtitleColor)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        match.events.forEach { event ->
            EventRow(event, theme)
        }
    }
}

@Composable
fun EventRow(event: Event, theme: AppTheme) {

    val bgColor = if (theme == AppTheme.LIGHT) Color(0xFFF5F5F5) else Color(0x22FFFFFF)
    val textColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White
    val secondaryColor = if (theme == AppTheme.LIGHT) Color.DarkGray else Color(0xFFDDDDDD)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {

        Text(
            text = "${event.minute}",
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(Modifier.width(12.dp))

        when (event.type) {
            "Goal" -> Text(
                text = "⚽ ${event.player ?: ""} (${event.detail})",
                color = textColor
            )

            "Card" -> Text(
                text = "🟨 ${event.player}",
                color = textColor
            )

            "subst" -> Text(
                text = "🔄 ${event.player} ↔ ${event.assist}",
                color = textColor
            )

            else -> Text(
                text = event.type ?: "-",
                color = secondaryColor
            )
        }
    }
}

@Composable
fun TeamDetails(
    team: Team,
    stats: TeamLeagueStats?,
    theme: AppTheme
) {
    val titleColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White
    val subtitleColor = if (theme == AppTheme.LIGHT) Color.Gray else Color(0xFFBBBBBB)
    val dividerColor = if (theme == AppTheme.LIGHT) Color(0xFFDDDDDD) else Color(0x44FFFFFF)
    val textColor = if (theme == AppTheme.LIGHT) Color.DarkGray else Color.White

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            KamelImage(
                resource = asyncPainterResource(team.logoUrl ?: ""),
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = team.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = titleColor,
            modifier = Modifier.fillMaxWidth()
        )

        Divider(color = dividerColor)

        if (stats != null) {
            TeamStatsSection(stats, theme)
        } else {
            Text(
                "Statistics unavailable for this team",
                fontSize = 14.sp,
                color = subtitleColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TeamStatsSection(stats: TeamLeagueStats, theme: AppTheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatBox("Position", stats.position.toString(), theme)
        StatBox("Played", stats.played.toString(), theme)
        StatBox("Points", stats.points.toString(), theme)
    }
}

@Composable
fun StatBox(label: String, value: String, theme: AppTheme) {

    val valueColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White
    val labelColor = if (theme == AppTheme.LIGHT) Color.Gray else Color(0xFFBBBBBB)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )

        Text(
            text = label,
            fontSize = 14.sp,
            color = labelColor
        )
    }
}