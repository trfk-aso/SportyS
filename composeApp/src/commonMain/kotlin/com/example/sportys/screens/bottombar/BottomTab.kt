package com.example.sportys.screens.bottombar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sportys.screens.Screen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import sportys.composeapp.generated.resources.Res
import sportys.composeapp.generated.resources.ic_favorites
import sportys.composeapp.generated.resources.ic_favorites_filled
import sportys.composeapp.generated.resources.ic_history
import sportys.composeapp.generated.resources.ic_history_filled
import sportys.composeapp.generated.resources.ic_home
import sportys.composeapp.generated.resources.ic_home_filled
import sportys.composeapp.generated.resources.ic_search
import sportys.composeapp.generated.resources.ic_search_filled
import sportys.composeapp.generated.resources.ic_stats
import sportys.composeapp.generated.resources.ic_stats_filled

sealed class BottomTab(
    val screen: Screen,
    val activeIcon: DrawableResource,
    val inactiveIcon: DrawableResource
) {
    data object Home : BottomTab(
        screen = Screen.Home,
        activeIcon = Res.drawable.ic_home_filled,
        inactiveIcon = Res.drawable.ic_home
    )

    data object Search : BottomTab(
        screen = Screen.Search,
        activeIcon = Res.drawable.ic_search_filled,
        inactiveIcon = Res.drawable.ic_search
    )

    data object History : BottomTab(
        screen = Screen.History,
        activeIcon = Res.drawable.ic_history_filled,
        inactiveIcon = Res.drawable.ic_history
    )

    data object Statistics : BottomTab(
        screen = Screen.Statistics,
        activeIcon = Res.drawable.ic_stats_filled,
        inactiveIcon = Res.drawable.ic_stats
    )

    data object Favorites : BottomTab(
        screen = Screen.Favorites,
        activeIcon = Res.drawable.ic_favorites_filled,
        inactiveIcon = Res.drawable.ic_favorites
    )
}

val bottomTabs = listOf(
    BottomTab.Home,
    BottomTab.Search,
    BottomTab.History,
    BottomTab.Statistics,
    BottomTab.Favorites
)

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Color.White)
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomTabs.forEach { tab ->
                BottomBarItem(
                    tab = tab,
                    selected = currentRoute == tab.screen.route,
                    onClick = { onTabSelected(tab.screen.route) }
                )
            }
        }
    }
}

@Composable
fun BottomBarItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconRes = if (selected) tab.activeIcon else tab.inactiveIcon

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(70.dp)
        )
    }
}