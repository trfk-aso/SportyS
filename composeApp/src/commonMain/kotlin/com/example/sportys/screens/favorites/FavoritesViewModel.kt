package com.example.sportys.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportys.model.Article
import com.example.sportys.model.FavoriteItem
import com.example.sportys.model.FavoriteType
import com.example.sportys.model.League
import com.example.sportys.model.Match
import com.example.sportys.model.Team
import com.example.sportys.repository.FootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repo: FootballRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state = _state.asStateFlow()

    init {
        loadAll()

        viewModelScope.launch {
            repo.favoritesEvents.collect {
                loadAll()
            }
        }

        viewModelScope.launch {
            repo.resetEvents.collect {
                loadAll()
            }
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val favTeams = repo.getFavorites(FavoriteType.TEAM)
            val favLeagues = repo.getFavorites(FavoriteType.LEAGUE)
            val favMatches = repo.getFavorites(FavoriteType.MATCH)
            val favArticles = repo.getFavorites(FavoriteType.ARTICLE)

            val teams = favTeams.mapNotNull { repo.getTeamById(it.itemId) }
            val leagues = favLeagues.mapNotNull { repo.getLeagueById(it.itemId) }
            val matches = favMatches.mapNotNull { repo.getMatchById(it.itemId) }
            val articles = favArticles.mapNotNull { repo.getArticleById(it.itemId) }

            _state.update {
                it.copy(
                    isLoading = false,
                    teams = teams,
                    leagues = leagues,
                    matches = matches,
                    articles = articles
                )
            }
        }
    }

    fun removeFavorite(type: FavoriteType, itemId: String) {
        viewModelScope.launch {
            repo.removeFavorite(type, itemId)
            loadAll()
        }
    }

    fun setTab(tab: FavoriteTab) {
        _state.update { it.copy(tab = tab) }
    }
}

enum class FavoriteTab { TEAMS, LEAGUES, MATCHES, ARTICLES }

data class FavoritesState(
    val isLoading: Boolean = false,
    val tab: FavoriteTab = FavoriteTab.TEAMS,

    val teams: List<Team> = emptyList(),
    val leagues: List<League> = emptyList(),
    val matches: List<Match> = emptyList(),
    val articles: List<Article> = emptyList(),
)