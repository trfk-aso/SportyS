package com.example.sportys.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportys.model.FavoriteType
import com.example.sportys.model.HistoryType
import com.example.sportys.model.TeamLeagueStats
import com.example.sportys.model.TeamStatistics
import com.example.sportys.repository.FootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val repo: FootballRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state = _state.asStateFlow()

    init {
        load()

        viewModelScope.launch {
            repo.resetEvents.collect {
                load()
            }
        }

        viewModelScope.launch {
            repo.favoritesEvents.collect {
                load()
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val favTeams = repo.getFavorites(FavoriteType.TEAM)

            val leagueStats = favTeams.mapNotNull {
                repo.getTeamLeagueStats(it.itemId)
            }

            val history = repo.getHistory(null)

            _state.update {
                it.copy(
                    teams = leagueStats,
                    articlesRead = history.count { it.type == HistoryType.ARTICLE },
                    matchesWatched = history.count { it.type == HistoryType.MATCH },
                    isLoading = false
                )
            }
        }
    }
}

data class StatisticsState(
    val teams: List<TeamLeagueStats> = emptyList(),
    val articlesRead: Int = 0,
    val matchesWatched: Int = 0,
    val period: StatisticsPeriod = StatisticsPeriod.WEEK,
    val isLoading: Boolean = true
)

enum class StatisticsPeriod { WEEK, MONTH, SEASON }