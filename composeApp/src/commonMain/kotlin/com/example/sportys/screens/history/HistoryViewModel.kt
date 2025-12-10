package com.example.sportys.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sportys.model.HistoryType
import com.example.sportys.repository.FootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

enum class HistoryFilter { ALL, MATCHES, ARTICLES }

data class HistoryUiItem(
    val id: Long,
    val contentId: String,
    val title: String,
    val subtitle: String,
    val type: HistoryType,
    val openedAt: Instant
)

data class HistoryGroup(
    val title: String,
    val items: List<HistoryUiItem>
)

data class HistoryState(
    val filter: HistoryFilter = HistoryFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val groups: List<HistoryGroup> = emptyList()
)

class HistoryViewModel(
    private val repo: FootballRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state = _state.asStateFlow()

    init {
        load()

        viewModelScope.launch {
            repo.resetEvents.collect {
                load()
            }
        }
    }

    fun setFilter(filter: HistoryFilter) {
        _state.update { it.copy(filter = filter) }
        load()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repo.clearHistory()
            load()
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repo.deleteHistoryItem(id)
            load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val type = when (_state.value.filter) {
                    HistoryFilter.ALL -> null
                    HistoryFilter.MATCHES -> HistoryType.MATCH
                    HistoryFilter.ARTICLES -> HistoryType.ARTICLE
                }

                val items = repo.getHistory(type)

                val uiItems = items.mapNotNull { h ->
                    when (h.type) {
                        HistoryType.ARTICLE -> h.article?.let { a ->
                            HistoryUiItem(
                                id = h.id,
                                contentId = a.id,
                                title = a.title,
                                subtitle = "Viewed: " + h.openedAt.toLocalTimeString(),
                                type = h.type,
                                openedAt = h.openedAt
                            )
                        }
                        HistoryType.MATCH -> h.match?.let { m ->
                            val title = "${m.homeTeam.name} — ${m.awayTeam.name}"
                            HistoryUiItem(
                                id = h.id,
                                contentId = m.id,
                                title = "${m.homeTeam.name} — ${m.awayTeam.name}",
                                subtitle = "Opened: " + h.openedAt.toLocalTimeString(),
                                type = h.type,
                                openedAt = h.openedAt
                            )
                        }
                    }
                }

                buildGroups(uiItems)
            }.onSuccess { groups ->
                _state.update { it.copy(isLoading = false, groups = groups) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onItemClicked(item: HistoryUiItem, navController: NavController) {
        when (item.type) {

            HistoryType.ARTICLE -> {
                navController.navigate("details/article/${item.contentId}")
            }

            HistoryType.MATCH -> {
                navController.navigate("details/match/${item.contentId}")
            }
        }
    }

    private fun buildGroups(list: List<HistoryUiItem>): List<HistoryGroup> {
        val todayDate = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date

        fun dateOf(i: HistoryUiItem) =
            i.openedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date

        val today = mutableListOf<HistoryUiItem>()
        val yesterday = mutableListOf<HistoryUiItem>()
        val earlier = mutableListOf<HistoryUiItem>()

        list.forEach { item ->
            val d = dateOf(item)
            when {
                d == todayDate -> today += item
                d == todayDate.minus(DatePeriod(days = 1)) -> yesterday += item
                else -> earlier += item
            }
        }

        val groups = mutableListOf<HistoryGroup>()
        if (today.isNotEmpty()) groups += HistoryGroup("Today", today)
        if (yesterday.isNotEmpty()) groups += HistoryGroup("Yesterday", yesterday)
        if (earlier.isNotEmpty()) groups += HistoryGroup("Earlier", earlier)
        return groups
    }
}

private fun Instant.toLocalTimeString(): String {
    val t = this.toLocalDateTime(TimeZone.currentSystemDefault()).time
    val hh = t.hour.toString().padStart(2, '0')
    val mm = t.minute.toString().padStart(2, '0')
    return "$hh:$mm"
}