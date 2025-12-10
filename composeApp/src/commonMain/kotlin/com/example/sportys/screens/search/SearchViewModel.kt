package com.example.sportys.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportys.model.Article
import com.example.sportys.model.FavoriteType
import com.example.sportys.model.League
import com.example.sportys.model.Match
import com.example.sportys.model.Team
import com.example.sportys.repository.FootballRepository
import com.example.sportys.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

class SearchViewModel(
    private val repo: FootballRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()
    private val io = Dispatchers.Default

    init {
        viewModelScope.launch {
            preloadTeams()
        }

        viewModelScope.launch {
            val saved = settingsRepository.getRecentSearches()
            _state.update { it.copy(recent = saved) }
        }

        viewModelScope.launch {
            repo.resetEvents.collect { onReset() }
        }
    }

    fun preloadTeams() {
        viewModelScope.launch {
            repo.refreshLeagues()

            val leagues = repo.getAllLeagues().take(5)

            leagues.forEach { league ->
                if (!repo.isTeamsCached(league.id)) {
                    repo.refreshTeam(league.id)
                }
            }
        }
    }

    fun onQueryChange(q: String) {
        _state.update { it.copy(query = q) }
    }

    fun deleteRecentQuery(q: String) {
        val updated = _state.value.recent.toMutableList().apply {
            remove(q)
        }

        _state.update { it.copy(recent = updated) }

        viewModelScope.launch {
            settingsRepository.saveRecentSearches(updated)
        }
    }

    fun setContentType(type: ContentType) {
        _state.update { it.copy(contentType = type) }
    }

    fun setTimeRange(range: TimeRange) {
        _state.update { it.copy(timeRange = range) }
    }

    fun clearFilters() {
        _state.update {
            it.copy(
                query = "",
                contentType = ContentType.ARTICLES,
                timeRange = TimeRange.TODAY,
                error = null
            )
        }
    }

    private fun onReset() {
        _state.update { st ->
            st.copy(
                recent = emptyList(),
                favoriteLeagues = emptySet(),
                favoriteTeams = emptySet()
            )
        }

        viewModelScope.launch {
            settingsRepository.saveRecentSearches(emptyList())
        }
    }

    fun useRecentQuery(q: String) {
        _state.update { it.copy(query = q) }
    }

    fun applySearch() {
        val s = _state.value

        viewModelScope.launch {

            val startTime = Clock.System.now().toEpochMilliseconds()
            val minDelay = 1000L

            _state.update { it.copy(isLoading = true, error = null) }

            val result = runCatching {

                if (s.contentType == ContentType.ARTICLES) {
                    repo.refreshArticles()
                }

                when (s.contentType) {
                    ContentType.ARTICLES -> searchArticles(s)
                    ContentType.MATCHES  -> searchMatches(s)
                    ContentType.TEAMS    -> searchTeams(s)
                    ContentType.LEAGUES  -> searchLeagues(s)
                }

            }

            val elapsed = Clock.System.now().toEpochMilliseconds() - startTime
            if (elapsed < minDelay) delay(minDelay - elapsed)

            result.onSuccess { data ->

                saveRecentQueryIfNeeded(s.query)

                _state.update {
                    when (s.contentType) {
                        ContentType.ARTICLES -> it.copy(
                            articles = data as List<Article>,
                            isLoading = false
                        )
                        ContentType.MATCHES -> it.copy(
                            matches = data as List<Match>,
                            isLoading = false
                        )
                        ContentType.TEAMS -> it.copy(
                            teams = data as List<Team>,
                            isLoading = false
                        )
                        ContentType.LEAGUES -> it.copy(
                            leagues = data as List<League>,
                            isLoading = false
                        )
                    }
                }
            }

            result.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun searchArticles(s: SearchState): List<Article> =
        withContext(io) {
            val all = repo.getArticles()

            val q = s.query.trim().lowercase()

            val filtered =
                if (q.isBlank()) all
                else all.filter { a ->
                    a.title.lowercase().contains(q) ||
                            (a.summary ?: "").lowercase().contains(q) ||
                            (a.source ?: "").lowercase().contains(q)
                }

            if (q.isBlank()) {
                return@withContext filtered.sortedByDescending { it.publishedAt }
            }

            val now = Clock.System.now()

            filtered.filter { article ->
                filterByRange(now, article.publishedAt, s.timeRange)
            }.sortedByDescending { it.publishedAt }
        }

    private suspend fun searchMatches(s: SearchState): List<Match> =
        withContext(io) {
            val allMatches = repo.getAllMatches()

            val filteredByQuery =
                if (s.query.isBlank()) allMatches
                else {
                    val q = s.query.trim().lowercase()
                    allMatches.filter { m ->
                        m.homeTeam.name.lowercase().contains(q) ||
                                m.awayTeam.name.lowercase().contains(q) ||
                                m.league.name.lowercase().contains(q)
                    }
                }

            val now = Clock.System.now()

            filteredByQuery
                .filter { match -> filterByRange(now, match.startTime, s.timeRange) }
                .sortedBy { it.startTime }
        }

    private suspend fun searchTeams(s: SearchState): List<Team> =
        withContext(io) {
            val leagues = repo.getAllLeagues()

            val allTeams = leagues.flatMap { league ->
                repo.getTeamsByLeague(league.id.toString())
            }.distinctBy { it.id }

            if (s.query.isBlank()) {
                return@withContext allTeams
            }

            val q = s.query.trim().lowercase()

            allTeams.filter { team ->
                team.name.lowercase().contains(q) ||
                        (team.leagueName ?: "").lowercase().contains(q)
            }
        }

    private suspend fun searchLeagues(s: SearchState): List<League> =
        withContext(io) {
            val all = repo.getAllLeagues()
            if (s.query.isBlank()) return@withContext all

            val q = s.query.trim().lowercase()
            all.filter { l ->
                l.name.lowercase().contains(q) ||
                        (l.country ?: "").lowercase().contains(q)
            }
        }

    private fun filterByRange(now: Instant, date: Instant, range: TimeRange): Boolean {
        val nowDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val d       = date.toLocalDateTime(TimeZone.currentSystemDefault()).date

        return when (range) {
            TimeRange.TODAY -> d == nowDate
            TimeRange.LAST_7_DAYS -> d >= nowDate.minus(DatePeriod(days = 7))
            TimeRange.LAST_MONTH  -> d >= nowDate.minus(DatePeriod(days = 30))
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            val favs = repo.getFavorites(FavoriteType.LEAGUE)
            _state.update {
                it.copy(favoriteLeagues = favs.map { f -> f.itemId }.toSet())
            }
        }
    }

    fun loadFavoriteTeams() {
        viewModelScope.launch {
            val favs = repo.getFavorites(FavoriteType.TEAM)
            _state.update {
                it.copy(favoriteTeams = favs.map { f -> f.itemId }.toSet())
            }
        }
    }

    fun toggleTeamFavorite(team: Team) {
        viewModelScope.launch {
            val isFav = repo.isFavorite(FavoriteType.TEAM, team.id)

            if (isFav) {
                repo.removeFavorite(FavoriteType.TEAM, team.id)
            } else {
                repo.addFavorite(FavoriteType.TEAM, team.id)
            }

            loadFavoriteTeams()
        }
    }

    fun toggleLeagueFavorite(league: League) {
        viewModelScope.launch {
            val isFav = repo.isFavorite(FavoriteType.LEAGUE, league.id)

            if (isFav) {
                repo.removeFavorite(FavoriteType.LEAGUE, league.id)
            } else {
                repo.addFavorite(FavoriteType.LEAGUE, league.id)
            }

            loadFavorites()
        }
    }

    private fun saveRecentQueryIfNeeded(q: String) {
        val cleaned = q.trim()
        if (cleaned.isEmpty()) return

        if (!_state.value.recent.contains(cleaned)) {
            val updated = listOf(cleaned) + _state.value.recent.take(9)
            _state.update { it.copy(recent = updated) }

            viewModelScope.launch {
                settingsRepository.saveRecentSearches(updated)
            }
        }
    }
}
enum class ContentType { ARTICLES, MATCHES, TEAMS, LEAGUES }
enum class TimeRange { TODAY, LAST_7_DAYS, LAST_MONTH }

data class SearchState(
    val query: String = "",
    val contentType: ContentType = ContentType.ARTICLES,
    val timeRange: TimeRange = TimeRange.TODAY,

    val recent: List<String> = emptyList(),

    val isLoading: Boolean = false,
    val error: String? = null,

    val articles: List<Article> = emptyList(),
    val matches: List<Match> = emptyList(),
    val teams: List<Team> = emptyList(),
    val leagues: List<League> = emptyList(),

    val favoriteLeagues: Set<String> = emptySet(),
    val favoriteTeams: Set<String> = emptySet()
)