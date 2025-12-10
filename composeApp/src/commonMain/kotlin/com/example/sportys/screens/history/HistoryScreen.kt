package com.example.sportys.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sportys.model.HistoryType
import com.example.sportys.screens.bottombar.AppBottomBar
import com.example.sportys.screens.settings.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.bg_settings_dark
import sportys.composeapp.generated.resources.bg_settings_light

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = koinInject(),
    theme: AppTheme
) {
    val state by viewModel.state.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

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
                HistoryTopBar(
                    showClear = state.groups.isNotEmpty(),
                    onClear = { showClearDialog = true },
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
                HistoryFilterRow(
                    selected = state.filter,
                    onSelect = viewModel::setFilter,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    theme = theme
                )

                when {
                    state.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    state.groups.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "You haven’t viewed anything yet.",
                                color = if (theme == AppTheme.LIGHT) Color.Black else Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            state.groups.forEach { group ->
                                item {
                                    Text(
                                        text = group.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = if (theme == AppTheme.LIGHT) Color.Black else Color.White,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }

                                items(group.items, key = { it.id }) { item ->
                                    HistoryRow(
                                        item = item,
                                        onClick = { viewModel.onItemClicked(item, navController) },
                                        onDelete = { viewModel.deleteItem(item.id) },
                                        theme = theme
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showClearDialog) {
            ClearHistoryDialog(
                theme = theme,
                onCancel = { showClearDialog = false },
                onConfirm = {
                    showClearDialog = false
                    viewModel.clearHistory()
                }
            )
        }
    }
}

@Composable
fun ClearHistoryDialog(
    theme: AppTheme,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val backgroundColor = when (theme) {
        AppTheme.LIGHT -> Color.White
        AppTheme.DARK  -> Color(0xFF2A2A2A)
    }

    val titleColor = if (theme == AppTheme.LIGHT) Color.Black else Color.White
    val textColor  = if (theme == AppTheme.LIGHT) Color.DarkGray else Color(0xFFBBBBBB)

    val cancelButtonColor = if (theme == AppTheme.LIGHT)
        Color(0xFFE6E6E6) else Color(0xFF444444)

    val cancelTextColor = if (theme == AppTheme.LIGHT)
        Color.Black else Color.White

    val clearButtonColor = Color(0xFFE74C3C)
    val clearTextColor = Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = false) {}
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(26.dp))
                .background(backgroundColor)
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .widthIn(max = 340.dp)
        ) {

            Text(
                "Clear History",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = titleColor
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Are you sure you want to clear all history?\nThis action cannot be undone.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(cancelButtonColor)
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Cancel",
                        color = cancelTextColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(clearButtonColor)
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Clear",
                        color = clearTextColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryTopBar(
    showClear: Boolean,
    onClear: () -> Unit,
    theme: AppTheme
) {
    val bgColor = when (theme) {
        AppTheme.LIGHT -> Color(0xFF0DA160)
        AppTheme.DARK  -> Color.Black
    }

    val titleColor = when (theme) {
        AppTheme.LIGHT -> Color.White
        AppTheme.DARK  -> Color.White
    }

    val clearBg = when (theme) {
        AppTheme.LIGHT -> Color.White
        AppTheme.DARK  -> Color(0xFF2A2A2A)
    }

    val clearTextColor = when (theme) {
        AppTheme.LIGHT -> Color.Black
        AppTheme.DARK  -> Color(0xFFBBBBBB)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                start = 16.dp,
                end = 16.dp,
                bottom = 12.dp
            )
    ) {

        Text(
            text = "History",
            color = titleColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )

        if (showClear) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(14.dp))
                    .background(clearBg)
                    .border(
                        width = if (theme == AppTheme.LIGHT) 1.dp else 0.dp,
                        color = if (theme == AppTheme.LIGHT) Color(0xFFE0E0E0) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onClear() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Clear",
                    color = clearTextColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HistoryFilterRow(
    selected: HistoryFilter,
    onSelect: (HistoryFilter) -> Unit,
    modifier: Modifier = Modifier,
    theme: AppTheme
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HistoryFilterChip(
            text = "All",
            selected = selected == HistoryFilter.ALL,
            onClick = { onSelect(HistoryFilter.ALL) },
            theme = theme
        )
        HistoryFilterChip(
            text = "Matches",
            selected = selected == HistoryFilter.MATCHES,
            onClick = { onSelect(HistoryFilter.MATCHES) },
            theme = theme
        )
        HistoryFilterChip(
            text = "Articles",
            selected = selected == HistoryFilter.ARTICLES,
            onClick = { onSelect(HistoryFilter.ARTICLES) },
            theme = theme
        )
    }
}

@Composable
private fun HistoryFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    theme: AppTheme
) {
    val backgroundColor: Color
    val borderColor: Color
    val textColor: Color

    when (theme) {
        AppTheme.LIGHT -> {
            backgroundColor = if (selected) Color.White else Color.White
            borderColor = if (selected) Color.Black else Color(0xFFE0E0E0)
            textColor = if (selected) Color.Black else Color.Black
        }

        AppTheme.DARK -> {
            backgroundColor = if (selected) Color(0xFF3A3A3A) else Color(0xFF2A2A2A)
            borderColor = if (selected) Color.White else Color(0xFF5A5A5A)
            textColor = if (selected) Color.White else Color(0xFFBBBBBB)
        }
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .height(44.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryRow(
    item: HistoryUiItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    theme: AppTheme
) {
    val dismissState = rememberSwipeToDismissBoxState()

    val shape = RoundedCornerShape(18.dp)

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

    val swipeBg = when (theme) {
        AppTheme.LIGHT -> Color.Red.copy(alpha = 0.15f)
        AppTheme.DARK  -> Color(0xFF440000)
    }

    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
        LaunchedEffect(Unit) {
            onDelete()
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,

        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(swipeBg)
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("Delete", color = Color.Red)
            }
        }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(cardColor)
                .border(
                    width = if (theme == AppTheme.LIGHT) 1.dp else 0.dp,
                    color = borderColor,
                    shape = shape
                )
                .clickable { onClick() }
                .padding(16.dp)
        ) {

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = item.subtitle,
                    color = subtitleColor,
                    fontSize = 13.sp
                )
            }
        }
    }
}