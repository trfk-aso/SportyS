package com.example.sportys.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportys.model.Article
import com.example.sportys.model.Match
import com.example.sportys.repository.FootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class HomeViewModel(
    private val repo: FootballRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    init {
        refreshAll()
        viewModelScope.launch {
            repo.refreshLeagues()
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            runCatching {
                repo.refreshArticles()
                repo.refreshMatchesForDate(Clock.System.now())
            }.onFailure {
                _state.update { s -> s.copy(isLoading = false, error = it.message) }
                return@launch
            }

            val news = repo.getArticles(limit = 20)
            val matches = repo.getMatchesForDate(Clock.System.now())

            _state.update {
                it.copy(
                    isLoading = false,
                    topArticles = news.take(2),
                    latestNews = news,
                    todayMatches = matches,
                    recommended = news.shuffled().take(5)
                )
            }
        }
    }
}

data class HomeScreenState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val topArticles: List<Article> = emptyList(),
    val latestNews: List<Article> = emptyList(),
    val todayMatches: List<Match> = emptyList(),
    val recommended: List<Article> = emptyList(),
)