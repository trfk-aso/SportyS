package com.example.sportys.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportys.model.Article
import com.example.sportys.model.FavoriteType
import com.example.sportys.model.HistoryType
import com.example.sportys.model.Match
import com.example.sportys.model.Team
import com.example.sportys.model.TeamLeagueStats
import com.example.sportys.repository.FootballRepository
import com.example.sportys.share.ShareManager
import com.example.sportys.share.ktorClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.Throwable
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val repo: FootballRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val type: String = savedStateHandle["type"] ?: ""
    private val id: String = savedStateHandle["id"] ?: ""

    private val _state = MutableStateFlow(DetailsState())
    val state = _state.asStateFlow()

    init {
        loadDetails()
    }

  fun loadDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            runCatching {

                when (type) {

                    "article" -> {
                        val article = repo.getArticleById(id)
                        val isFav = repo.isFavorite(FavoriteType.ARTICLE, id)

                        repo.addHistory(HistoryType.ARTICLE, id)

                        _state.update {
                            it.copy(
                                isLoading = false,
                                article = article,
                                mode = DetailMode.Article,
                                isFavorite = isFav
                            )
                        }
                    }

                    "match" -> {
                        val match = repo.getMatchDetails(id)
                        val isFav = repo.isFavorite(FavoriteType.MATCH, id)

                        repo.addHistory(HistoryType.MATCH, id)

                        _state.update {
                            it.copy(
                                isLoading = false,
                                match = match,
                                mode = DetailMode.Match,
                                isFavorite = isFav
                            )
                        }

                    }
                    "team" -> {
                        val team = repo.getTeamById(id)
                        val stats = repo.getTeamLeagueStats(id)

                        repo.addHistory(HistoryType.MATCH, id)

                        _state.update {
                            it.copy(
                                isLoading = false,
                                mode = DetailMode.Team,
                                team = team,
                                teamStats = stats
                            )
                        }
                    }
                }

            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun share(manager: ShareManager) {
        viewModelScope.launch {

            val st = state.value

            val imageUrl: String? = when (st.mode) {

                DetailMode.Article ->
                    st.article?.imageUrl

                DetailMode.Match ->
                    st.match?.homeTeam?.logoUrl

                DetailMode.Team ->
                    st.team?.logoUrl
            }

            val imageBytes: ByteArray? = imageUrl?.let { url ->
                try {
                    ktorClient.get(url).body<ByteArray>()
                } catch (e: Exception) {
                    null
                }
            }

            val (title, content) = when (st.mode) {
                DetailMode.Article -> {
                    val t = st.article!!.title
                    val c = st.article.content ?: ""
                    t to c
                }

                DetailMode.Match -> {
                    val t = "${st.match!!.homeTeam.name} vs ${st.match.awayTeam.name}"
                    val c = "Match details..."
                    t to c
                }

                DetailMode.Team -> {
                    val t = st.team!!.name
                    val c = "Team stats: ${st.teamStats}"
                    t to c
                }
            }

            val pdf = manager.exportPdf(
                title = title,
                content = content,
                imageBytes = imageBytes
            )

            manager.sharePdf("${title.take(20)}.pdf", pdf)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val st = state.value

            when (st.mode) {

                DetailMode.Article -> {
                    st.article?.let {
                        if (st.isFavorite)
                            repo.removeFavorite(FavoriteType.ARTICLE, it.id)
                        else
                            repo.addFavorite(FavoriteType.ARTICLE, it.id)
                    }
                }

                DetailMode.Match -> {
                    st.match?.let {
                        if (st.isFavorite)
                            repo.removeFavorite(FavoriteType.MATCH, it.id)
                        else
                            repo.addFavorite(FavoriteType.MATCH, it.id)
                    }
                }

                DetailMode.Team -> {
                    st.team?.let {
                        if (st.isFavorite)
                            repo.removeFavorite(FavoriteType.TEAM, it.id)
                        else
                            repo.addFavorite(FavoriteType.TEAM, it.id)
                    }
                }
            }

            _state.update { it.copy(isFavorite = !it.isFavorite) }
        }
    }
}

data class DetailsState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val article: Article? = null,
    val match: Match? = null,
    val team: Team? = null,
    val teamStats: TeamLeagueStats? = null,

    val mode: DetailMode = DetailMode.Article,
    val isFavorite: Boolean = false
)

enum class DetailMode { Article, Match, Team }

enum class LineupTab { HOME, AWAY }