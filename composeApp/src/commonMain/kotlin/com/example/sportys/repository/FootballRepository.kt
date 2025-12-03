package com.example.sportys.repository

import com.example.sportys.data.SportyS
import com.example.sportys.model.Article
import com.example.sportys.model.FavoriteItem
import com.example.sportys.model.FavoriteType
import com.example.sportys.model.League
import com.example.sportys.model.Match
import com.example.sportys.model.Team
import com.example.sportys.model.UserPreferences
import com.example.sportys.model.dto.ArticleDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface FootballRepository {

    suspend fun refreshArticles(): Result<Unit>
    suspend fun getArticles(limit: Int? = null): List<Article>
    suspend fun getArticleById(id: String): Article?

    suspend fun refreshMatchesForDate(date: Instant): Result<Unit>
    suspend fun getMatchesForDate(date: Instant): List<Match>
    suspend fun getMatchById(id: String): Match?

    suspend fun refreshTeam(id: String): Result<Unit>
    suspend fun getTeamById(id: String): Team?
    suspend fun getTeamsByLeague(leagueId: String): List<Team>

    suspend fun refreshLeagues(): Result<Unit>
    suspend fun getAllLeagues(): List<League>
    suspend fun getLeagueById(id: String): League?

    suspend fun getFavorites(type: FavoriteType? = null): List<FavoriteItem>
    suspend fun isFavorite(type: FavoriteType, itemId: String): Boolean
    suspend fun addFavorite(type: FavoriteType, itemId: String): Unit
    suspend fun removeFavorite(type: FavoriteType, itemId: String): Unit
    suspend fun clearAllFavorites(): Unit

    suspend fun getUserPreferences(): UserPreferences
    suspend fun updateUserPreferences(prefs: UserPreferences): Unit

    suspend fun resetAllUserData(): Unit
}

interface NewsApi {
    suspend fun getLatestNews(): List<ArticleDTO>
    suspend fun searchNews(query: String): List<ArticleDTO>
}

class FootballRepositoryImpl(
    private val api: FootballApi,
    private val db: SportyS,
    private val mapper: FootballDbMapper,
) : FootballRepository {

    private val io = Dispatchers.Default

    override suspend fun refreshArticles(): Result<Unit> = withContext(io) {
        runCatching {
            val remoteArticles = api.getLatestArticles()

            db.transaction {
                db.sportyQueries.deleteAllArticles()

                remoteArticles.forEach { dto ->
                    val e = mapper.articleDtoToDb(dto)
                    db.sportyQueries.insertArticle(
                        id = e.id,
                        title = e.title,
                        summary = e.summary,
                        content = e.content,
                        image_url = e.image_url,
                        published_at = e.published_at,
                        source = e.source,
                        tags_json = e.tags_json
                    )
                }
            }
        }
    }

    override suspend fun getArticles(limit: Int?): List<Article> = withContext(io) {
        val rows = db.sportyQueries
            .selectAllArticles()
            .executeAsList()

        val list = rows.map { mapper.articleDbToDomain(it) }
        if (limit != null) list.take(limit) else list
    }

    override suspend fun getArticleById(id: String): Article? = withContext(io) {
        db.sportyQueries
            .selectArticleById(id)
            .executeAsOneOrNull()
            ?.let { mapper.articleDbToDomain(it) }
    }

    override suspend fun refreshMatchesForDate(date: Instant): Result<Unit> =
        withContext(io) {
            runCatching {
                val dateStr = date.toString().substringBefore("T")
                val remote = api.getMatches(dateStr)

                db.transaction {
                    remote.forEach { dto ->
                        val e = mapper.matchDtoToDb(dto)
                        db.sportyQueries.insertMatch(
                            id = e.id,
                            home_team_id = e.home_team_id,
                            home_team_name = e.home_team_name,
                            home_team_logo = e.home_team_logo,
                            away_team_id = e.away_team_id,
                            away_team_name = e.away_team_name,
                            away_team_logo = e.away_team_logo,
                            league_id = e.league_id,
                            league_name = e.league_name,
                            league_country = e.league_country,
                            status = e.status,
                            start_time = e.start_time,
                            score_home = e.score_home,
                            score_away = e.score_away
                        )
                    }
                }
            }
        }

    override suspend fun getMatchesForDate(date: Instant): List<Match> =
        withContext(io) {
            db.sportyQueries
                .selectMatchesForDay(date.toString())
                .executeAsList()
                .map { mapper.matchDbToDomain(it) }
        }

    override suspend fun refreshLeagues(): Result<Unit> =
        withContext(io) {
            runCatching {
                val remote = api.getLeagues()
                db.transaction {
                    remote.forEach { dto ->
                        val e = mapper.leagueDtoToDb(dto)
                        db.sportyQueries.insertLeague(
                            id = e.id,
                            name = e.name,
                            country = e.country,
                            logo_url = e.logo_url
                        )
                    }
                }
            }
        }

    override suspend fun getMatchById(id: String): Match? =
        withContext(io) {
            db.sportyQueries
                .selectMatchById(id)
                .executeAsOneOrNull()
                ?.let { mapper.matchDbToDomain(it) }
        }

    override suspend fun getAllLeagues(): List<League> =
        withContext(io) {
            db.sportyQueries.selectAllLeagues()
                .executeAsList()
                .map { mapper.leagueDbToDomain(it) }
        }

    override suspend fun getLeagueById(id: String): League? =
        withContext(io) {
            db.sportyQueries.selectLeagueById(id)
                .executeAsOneOrNull()
                ?.let { mapper.leagueDbToDomain(it) }
        }

    override suspend fun refreshTeam(id: String): Result<Unit> =
        withContext(io) {
            runCatching {
                val leagueId = id.toIntOrNull() ?: return@runCatching
                val remote = api.getTeams(leagueId.toString())

                db.transaction {
                    remote.forEach { dto ->
                        val e = mapper.teamDtoToDb(dto)
                        db.sportyQueries.insertTeam(
                            id = e.id,
                            name = e.name,
                            logo_url = e.logo_url,
                            league_id = e.league_id,
                            league_name = e.league_name,
                            position = e.position
                        )
                    }
                }
            }
        }

    override suspend fun getTeamsByLeague(leagueId: String): List<Team> =
        withContext(io) {
            db.sportyQueries.selectAllTeams()
                .executeAsList()
                .filter { it.league_id == leagueId }
                .map { mapper.teamDbToDomain(it) }
        }

    override suspend fun getTeamById(id: String): Team? =
        withContext(io) {
            db.sportyQueries.selectTeamById(id)
                .executeAsOneOrNull()
                ?.let { mapper.teamDbToDomain(it) }
        }

    override suspend fun getFavorites(type: FavoriteType?): List<FavoriteItem> =
        withContext(io) {
            val rows = if (type == null) {
                db.sportyQueries
                    .selectAllFavorites()
                    .executeAsList()
            } else {
                db.sportyQueries
                    .selectFavoritesByType(type.name)
                    .executeAsList()
            }

            rows.map { mapper.favoriteDbToDomain(it) }
        }

    override suspend fun isFavorite(type: FavoriteType, itemId: String): Boolean =
        withContext(io) {
            db.sportyQueries
                .selectFavoriteByTypeAndItem(type.name, itemId)
                .executeAsOneOrNull() != null
        }

    override suspend fun addFavorite(type: FavoriteType, itemId: String) {
        withContext(io) {
            val now = Clock.System.now()

            db.sportyQueries.insertFavorite(
                type = type.name,
                item_id = itemId,
                created_at = now.toString()
            )
        }
    }

    override suspend fun clearAllFavorites() =
        withContext(io) {
            db.sportyQueries.deleteAllFavorites()
            Unit
        }

    override suspend fun removeFavorite(type: FavoriteType, itemId: String) {
        withContext(io) {
            db.sportyQueries.deleteFavorite(type.name, itemId)
        }
    }

    override suspend fun getUserPreferences(): UserPreferences {
        // TODO
        TODO("Not yet implemented")
    }

    override suspend fun updateUserPreferences(prefs: UserPreferences) {
        // TODO
    }

    override suspend fun resetAllUserData() {
        // TODO: очистка всех таблиц + prefs
    }
}