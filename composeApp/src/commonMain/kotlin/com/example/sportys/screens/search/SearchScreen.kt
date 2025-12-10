package com.example.sportys.screens.search

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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sportys.model.Article
import com.example.sportys.model.League
import com.example.sportys.model.Match
import com.example.sportys.model.MatchStatus
import com.example.sportys.model.Team
import com.example.sportys.screens.bottombar.AppBottomBar
import com.example.sportys.screens.details.displayName
import com.example.sportys.screens.home.MatchCard
import com.example.sportys.screens.home.formatMatchDateTime
import com.example.sportys.screens.settings.AppTheme
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.about_bg
import sportys.composeapp.generated.resources.about_bg_dark
import sportys.composeapp.generated.resources.bg_settings_dark
import sportys.composeapp.generated.resources.bg_settings_light
import sportys.composeapp.generated.resources.ic_clear
import sportys.composeapp.generated.resources.ic_close
import sportys.composeapp.generated.resources.ic_close_dark
import sportys.composeapp.generated.resources.ic_history_item
import sportys.composeapp.generated.resources.ic_history_item_dark
import sportys.composeapp.generated.resources.ic_search_black
import sportys.composeapp.generated.resources.ic_search_icon
import sportys.composeapp.generated.resources.ic_search_white
import sportys.composeapp.generated.resources.ic_star_filled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    onOpenResults: () -> Unit,
    viewModel: SearchViewModel = koinInject(),
    theme: AppTheme
) {
    val state by viewModel.state.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        viewModel.preloadTeams()
    }

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
                SearchTopBar(theme)
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                SearchBar(
                    query = state.query,
                    onQueryChange = viewModel::onQueryChange,
                    onClear = { viewModel.onQueryChange("") },
                    onSubmit = {
                        viewModel.applySearch()
                        onOpenResults()
                    },
                    theme = theme
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Filters",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (theme == AppTheme.LIGHT) Color.Black else Color.White
                )

                Spacer(Modifier.height(8.dp))

                ContentTypeSelector(
                    selected = state.contentType,
                    onSelect = {
                        viewModel.setContentType(it)

                        if (it == ContentType.TEAMS || it == ContentType.LEAGUES) {
                            viewModel.applySearch()
                            onOpenResults()
                        }
                    },
                    theme = theme
                )

                Spacer(Modifier.height(16.dp))

                if (state.contentType == ContentType.ARTICLES ||
                    state.contentType == ContentType.MATCHES
                ) {
                    TimeRangeSelector(
                        selected = state.timeRange,
                        onSelect = viewModel::setTimeRange,
                        theme = theme
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Text(
                    "Recent searches",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (theme == AppTheme.LIGHT) Color.Black else Color.White
                )

                Spacer(Modifier.height(8.dp))

                RecentSearchList(
                    items = state.recent,
                    onSelect = {
                        viewModel.useRecentQuery(it)
                        viewModel.applySearch()
                    },
                    onDelete = viewModel::deleteRecentQuery,
                    theme = theme
                )

                Spacer(Modifier.height(24.dp))

                SearchActionButtons(
                    theme = theme,
                    onReset = { viewModel.clearFilters() },
                    onApply = {
                        viewModel.applySearch()
                        onOpenResults()
                    }
                )

                if (state.isLoading) {
                    LoadingState(
                        text = "Searching…",
                        theme = theme
                    )
                } else if (state.error != null) {
                    ErrorState(
                        message = state.error ?: "",
                        onRetry = { viewModel.applySearch() }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchTopBar(theme: AppTheme) {

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
            text = "Search",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun SearchActionButtons(
    theme: AppTheme,
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    Row {

        val resetBg = if (theme == AppTheme.LIGHT) Color(0xFFCFCFCF) else Color(0xFF6E6E6E)
        val resetText = Color.Black

        Button(
            onClick = onReset,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = resetBg,
                contentColor = resetText
            )
        ) {
            Text(
                "Reset",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.width(12.dp))

        val applyBg = Color(0xFF4DBA74)
        val applyText = Color.Black

        Button(
            onClick = onApply,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = applyBg,
                contentColor = applyText
            )
        ) {
            Text(
                "Apply",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    theme: AppTheme
) {
    val keyboard = LocalSoftwareKeyboardController.current

    val isDark = theme == AppTheme.DARK

    val bgColor = if (isDark) Color(0xFF1C1C1C) else Color.White
    val borderColor = if (isDark) Color(0xFF2F2F2F) else Color(0xFFE0E0E0)
    val placeholderColor = if (isDark) Color(0xFF9C9C9C) else Color(0xFF555555)
    val textColor = if (isDark) Color.White else Color.Black

    val searchIcon = if (isDark) Res.drawable.ic_search_white else Res.drawable.ic_search_black
    val clearIcon = Res.drawable.ic_clear

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bgColor, RoundedCornerShape(16.dp)),

        placeholder = {
            Text(
                "Team, league, match or article",
                fontSize = 16.sp,
                color = placeholderColor
            )
        },

        singleLine = true,

        leadingIcon = {
            Image(
                painter = painterResource(searchIcon),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        },

        trailingIcon = {
            if (query.isNotEmpty()) {
                Image(
                    painter = painterResource(clearIcon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onClear() }
                )
            }
        },

        colors = TextFieldDefaults.colors(
            focusedContainerColor = bgColor,
            unfocusedContainerColor = bgColor,
            disabledContainerColor = bgColor,

            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),

        textStyle = LocalTextStyle.current.copy(
            color = textColor,
            fontSize = 18.sp
        ),

        shape = RoundedCornerShape(16.dp),

        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboard?.hide()
                onSubmit()
            }
        )
    )
}

@Composable
fun ContentTypeSelector(
    selected: ContentType,
    onSelect: (ContentType) -> Unit,
    theme: AppTheme
) {
    Column {
        Text(
            "Content type",
            fontSize = 14.sp,
            color = if (theme == AppTheme.LIGHT) Color.Black else Color.White
        )
        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TypeChip(
                text = "Articles",
                isSelected = selected == ContentType.ARTICLES,
                theme = theme,
                onClick = { onSelect(ContentType.ARTICLES) },
                modifier = Modifier.weight(1f)
            )
            TypeChip(
                text = "Matches",
                isSelected = selected == ContentType.MATCHES,
                theme = theme,
                onClick = { onSelect(ContentType.MATCHES) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TypeChip(
                text = "Teams",
                isSelected = selected == ContentType.TEAMS,
                theme = theme,
                onClick = { onSelect(ContentType.TEAMS) },
                modifier = Modifier.weight(1f)
            )
            TypeChip(
                text = "Leagues",
                isSelected = selected == ContentType.LEAGUES,
                theme = theme,
                onClick = { onSelect(ContentType.LEAGUES) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TypeChip(
    text: String,
    isSelected: Boolean,
    theme: AppTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when {
        isSelected && theme == AppTheme.LIGHT -> Color(0xFF4CAF50)
        isSelected && theme == AppTheme.DARK  -> Color(0xFF0E2A14)
        !isSelected && theme == AppTheme.LIGHT -> Color.White
        else -> Color(0xFF2C2C2C)
    }

    val borderColor = when {
        isSelected && theme == AppTheme.LIGHT -> Color.Transparent
        isSelected && theme == AppTheme.DARK  -> Color(0xFF4CAF50)
        !isSelected && theme == AppTheme.LIGHT -> Color(0xFFE0E0E0)
        else -> Color(0xFF555555)
    }

    val textColor = when {
        isSelected -> Color.White
        theme == AppTheme.LIGHT -> Color.Black
        else -> Color.White
    }

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
    }
}
@Composable
fun TimeRangeSelector(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit,
    theme: AppTheme
) {
    val textColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White

    Column {
        Text(
            "Time range",
            fontSize = 14.sp,
            color = textColor
        )
        Spacer(Modifier.height(8.dp))

        TimeRangeItem(
            label = "Today",
            isSelected = selected == TimeRange.TODAY,
            onClick = { onSelect(TimeRange.TODAY) },
            theme = theme
        )
        TimeRangeItem(
            label = "Last 7 days",
            isSelected = selected == TimeRange.LAST_7_DAYS,
            onClick = { onSelect(TimeRange.LAST_7_DAYS) },
            theme = theme
        )
        TimeRangeItem(
            label = "Last month",
            isSelected = selected == TimeRange.LAST_MONTH,
            onClick = { onSelect(TimeRange.LAST_MONTH) },
            theme = theme
        )
    }
}

@Composable
fun TimeRangeItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    theme: AppTheme
) {
    val textColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White

    val borderColor = when {
        isSelected -> Color(0xFF28C76F)
        theme == AppTheme.LIGHT -> Color(0xFFCFCFCF)
        else -> Color(0xFF555555)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {

        Box(
            modifier = Modifier
                .size(24.dp)
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF28C76F), CircleShape)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = label,
            color = textColor,
            fontSize = 16.sp
        )
    }
}

@Composable
fun RecentSearchList(
    items: List<String>,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    theme: AppTheme
) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            RecentSearchItem(
                text = item,
                onClick = { onSelect(item) },
                onDelete = { onDelete(item) },
                theme = theme
            )
        }
    }
}

@Composable
fun RecentSearchItem(
    text: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    theme: AppTheme
) {
    val isDark = theme == AppTheme.DARK

    val bgColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDark) Color(0xFFD0D0D0) else Color(0xFF333333)
    val deleteTint = if (isDark) Color(0xFFC08484) else Color.Gray
    val borderColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE0E0E0)

    val historyIcon = if (isDark)
        painterResource(Res.drawable.ic_history_item_dark)
    else
        painterResource(Res.drawable.ic_history_item)

    val deleteIcon = if (isDark)
        painterResource(Res.drawable.ic_close_dark)
    else
        painterResource(Res.drawable.ic_close)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = historyIcon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = deleteIcon,
            contentDescription = null,
            tint = deleteTint,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onDelete)
        )
    }
}

@Composable
fun LoadingState(
    text: String = "Searching...",
    theme: AppTheme
) {
    val indicatorColor =
        if (theme == AppTheme.LIGHT) Color(0xFF06C167)
        else Color.White

    val textColor =
        if (theme == AppTheme.LIGHT) Color.Black
        else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = indicatorColor,
            strokeWidth = 4.dp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text,
            color = textColor,
            fontSize = 16.sp
        )
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String? = null,
    theme: AppTheme,
    onClearFilters: (() -> Unit)? = null
) {
    val isDark = theme == AppTheme.DARK

    val titleColor = if (isDark) Color.White else Color.Black
    val subtitleColor = if (isDark) Color(0xFFB0B0B0) else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            title,
            color = titleColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        subtitle?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                it,
                fontSize = 15.sp,
                color = subtitleColor,
                textAlign = TextAlign.Center
            )
        }

        if (onClearFilters != null) {
            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onClearFilters,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(18.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF06C167),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    "Clear filters",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️", fontSize = 40.sp)
        Spacer(Modifier.height(8.dp))
        Text("Failed to load data.")
        Spacer(Modifier.height(4.dp))
        Text(message, fontSize = 13.sp, color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(Color(0xFF06C167)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Retry", color = Color.White)
        }
    }
}

@Composable
fun TeamsSkeleton(theme: AppTheme) {

    val placeholder = when (theme) {
        AppTheme.LIGHT -> Color(0xFFEDEDED)
        AppTheme.DARK  -> Color(0xFF2A2A2A)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        repeat(4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                repeat(3) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {

                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(placeholder, shape = RoundedCornerShape(35.dp))
                        )

                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .height(10.dp)
                                .fillMaxWidth(0.7f)
                                .background(placeholder, shape = RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ArticleCard(
    article: Article,
    navController: NavController,
    theme: AppTheme
) {
    val cardBg = when (theme) {
        AppTheme.LIGHT -> Color.White
        AppTheme.DARK  -> Color(0xFF1A1A1A)
    }

    val titleColor = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color.White
    }

    val sourceColor = when (theme) {
        AppTheme.LIGHT -> Color.Gray
        AppTheme.DARK  -> Color(0xFFBBBBBB)
    }

    val imagePlaceholderBg = when (theme) {
        AppTheme.LIGHT -> Color(0xFFEAEAEA)
        AppTheme.DARK  -> Color(0xFF2A2A2A)
    }

    val noImageTextColor = when (theme) {
        AppTheme.LIGHT -> Color.DarkGray
        AppTheme.DARK  -> Color.LightGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable {
                navController.navigate("details/article/${article.id}")
            },
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(Modifier.fillMaxSize()) {

            KamelImage(
                resource = asyncPainterResource(article.imageUrl ?: ""),
                contentDescription = null,
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .background(imagePlaceholderBg),
                contentScale = ContentScale.Crop,
                onFailure = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(imagePlaceholderBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No image",
                            fontSize = 10.sp,
                            color = noImageTextColor
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    article.title,
                    color = titleColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    article.source ?: "",
                    fontSize = 12.sp,
                    color = sourceColor
                )
            }
        }
    }
}

@Composable
fun ArticleCardSkeleton(theme: AppTheme) {

    val placeholder = when (theme) {
        AppTheme.LIGHT -> Color(0xFFEDEDED)
        AppTheme.DARK  -> Color(0xFF2A2A2A)
    }

    val cardBg = when (theme) {
        AppTheme.LIGHT -> Color.White
        AppTheme.DARK  -> Color(0xFF1A1A1A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .background(placeholder)
            )

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(placeholder)
                    )

                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.7f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(placeholder)
                    )
                }

                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(placeholder)
                )
            }
        }
    }
}

@Composable
fun ArticleSkeletonList(theme: AppTheme) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(6) {
            ArticleCardSkeleton(theme)
        }
    }
}

@Composable
fun MatchCardSimple(
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

    val timeLabel = remember(match.startTime) { formatMatchDateTime(match.startTime) }
    val statusText = match.status.displayName()
    val statusColor =
        if (match.status == MatchStatus.LIVE) Color(0xFF34C759) else subtitleColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardColor)
            .border(
                width = if (theme == AppTheme.LIGHT) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
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

            Column(horizontalAlignment = Alignment.End) {
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
                timeLabel,
                color = subtitleColor,
                fontSize = 13.sp
            )

            Spacer(Modifier.weight(1f))

            Text(
                "Status: $statusText",
                color = statusColor,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun MatchCardSkeleton(theme: AppTheme) {

    val cardColor = when (theme) {
        AppTheme.LIGHT -> Color.White
        AppTheme.DARK  -> Color(0xFF1B1B1B)
    }

    val placeholder = when (theme) {
        AppTheme.LIGHT -> Color(0xFFEDEDED)
        AppTheme.DARK  -> Color(0xFF2A2A2A)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(placeholder)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .fillMaxWidth(0.6f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(placeholder)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(placeholder)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .fillMaxWidth(0.5f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(placeholder)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(placeholder)
                )
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(placeholder)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(80.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(placeholder)
            )

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(60.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(placeholder)
            )
        }
    }
}

@Composable
fun MatchesSkeletonList(theme: AppTheme) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(6) {
            MatchCardSkeleton(theme)
        }
    }
}

@Composable
private fun TeamRow(
    name: String,
    logoUrl: String?,
    bold: Boolean,
    textColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        if (!logoUrl.isNullOrBlank()) {
            KamelImage(
                resource = asyncPainterResource(logoUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
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
fun TeamCircleCard(
    team: Team,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    theme: AppTheme
) {
    val textColor = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK -> Color.White
    }

    val circleBg = when (theme) {
        AppTheme.LIGHT -> Color(0xFFEDEDED)
        AppTheme.DARK -> Color(0xFF2C2C2E)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onToggleFavorite() }
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
                        .background(circleBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        team.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            } else {
                KamelImage(
                    resource = asyncPainterResource(logo),
                    contentDescription = team.name,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(circleBg),
                    contentScale = ContentScale.Crop
                )
            }

            if (isFavorite) {
                Image(
                    painter = painterResource(Res.drawable.ic_star_filled),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = team.name,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = textColor
        )
    }
}