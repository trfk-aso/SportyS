package com.example.sportys.repository

import com.example.sportys.data.SportyS
import com.example.sportys.model.Article
import com.example.sportys.model.Event
import com.example.sportys.model.ExportData
import com.example.sportys.model.FavoriteItem
import com.example.sportys.model.FavoriteType
import com.example.sportys.model.HistoryItem
import com.example.sportys.model.HistoryType
import com.example.sportys.model.League
import com.example.sportys.model.Lineup
import com.example.sportys.model.Match
import com.example.sportys.model.PlayerShort
import com.example.sportys.model.StatItem
import com.example.sportys.model.Team
import com.example.sportys.model.TeamLeagueStats
import com.example.sportys.model.TeamStatistics
import com.example.sportys.model.UserPreferences
import com.example.sportys.model.dto.ArticleDTO
import com.example.sportys.model.dto.FixtureDTO
import com.example.sportys.model.dto.MatchDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.getScopeName

interface FootballRepository {
    val favoritesEvents: Flow<Unit>
    val resetEvents: SharedFlow<Unit>
    suspend fun getAllMatches(): List<Match>
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

    suspend fun isTeamsCached(leagueId: String): Boolean

    suspend fun getUserPreferences(): UserPreferences
    suspend fun updateUserPreferences(prefs: UserPreferences): Unit

    suspend fun resetAllUserData(): Unit

    suspend fun getMatchDetails(matchId: String): Match
    suspend fun getTopMatches(): List<Match>
    suspend fun getTodayTopMatches(): List<Match>
    suspend fun addHistory(type: HistoryType, itemId: String)
    suspend fun getHistory(type: HistoryType? = null): List<HistoryItem>
    suspend fun clearHistory()
    suspend fun deleteHistoryItem(id: Long)
    suspend fun getTeamLeagueStats(teamId: String): TeamLeagueStats?
    suspend fun exportUserDataAsJson(): String
//    suspend fun getTeamMatches(teamId: String): List<FixtureDTO>
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

    private val topLeagues = listOf(39, 140, 135, 78, 61, 2)
    private val _favoritesEvents = MutableSharedFlow<Unit>(replay = 0)
    override val favoritesEvents: Flow<Unit> = _favoritesEvents

    private val _resetEvents = MutableSharedFlow<Unit>(replay = 0)
    override val resetEvents = _resetEvents.asSharedFlow()

    override suspend fun getAllMatches(): List<Match> =
        withContext(io) {
            db.sportyQueries.selectAllMatches()
                .executeAsList()
                .map { mapper.matchDbToDomain(it) }
        }

    override suspend fun getTopMatches(): List<Match> = withContext(io) {
        topLeagues
            .flatMap { leagueId -> api.getMatchesByLeague(leagueId) }
            .map { dto -> mapper.matchDtoToDomain(dto) }
    }

    override suspend fun getTodayTopMatches(): List<Match> = withContext(io) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date.toString()

        topLeagues
            .flatMap { leagueId ->
                api.getMatchesByLeagueAndDate(leagueId, today)
            }
            .map { mapper.matchDtoToDomain(it) }
            .sortedBy { it.startTime }
    }

    override suspend fun isTeamsCached(leagueId: String): Boolean =
        withContext(io) {
            db.sportyQueries.selectTeamsByLeague(leagueId).executeAsList().isNotEmpty()
        }

    override suspend fun refreshArticles(): Result<Unit> = withContext(io) {
        runCatching {
            val remoteArticles = api.getLatestArticles()

            db.transaction {

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

    override suspend fun exportUserDataAsJson(): String {
        val favorites = getFavorites(null)
        val history = getHistory(null)
        val prefs = getUserPreferences()

        val data = ExportData(
            favorites = favorites,
            history = history,
            preferences = prefs
        )

        return Json.encodeToString(data)
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

                val dateStr = date
                    .toLocalDateTime(TimeZone.UTC)
                    .date
                    .toString()

                val remote = api.getMatches(dateStr)

                if (remote.isEmpty()) {
                    println("⚠️ API returned empty matches, keeping previous cache")
                    return@runCatching
                }

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
                            match_date = e.match_date,

                            score_home = e.score_home,
                            score_away = e.score_away
                        )
                    }
                }
            }
        }

    override suspend fun getMatchesForDate(date: Instant): List<Match> =
        withContext(io) {

            val all = db.sportyQueries.selectAllMatches().executeAsList()
            println("📦 MATCHES IN DB = ${all.size}")
            all.forEach {
                println("id=${it.id}")
                println("start_time=${it.start_time}")
                println("match_date=${it.match_date}")
                println("----------------------")
            }

            val utcDate = date
                .toLocalDateTime(TimeZone.UTC)
                .date
                .toString()

            println("🔎 Loading matches from DB for UTC date = $utcDate")

            db.sportyQueries
                .selectMatchesForDay(utcDate)
                .executeAsList()
                .map { mapper.matchDbToDomain(it) }
        }

    override suspend fun refreshLeagues(): Result<Unit> =
        withContext(io) {
            runCatching {

                val existing = db.sportyQueries.selectAllLeagues().executeAsList()

                if (existing.isNotEmpty()) {
                    println("⏳ Leagues already cached → using local DB")
                    return@runCatching
                }

                println("🌐 Loading leagues from API…")
                val remote = api.getLeagues()

                if (remote.isEmpty()) {
                    println("❌ API returned EMPTY → keeping DB empty but not overwriting")
                    return@runCatching
                }

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

                println("✅ Leagues saved: ${remote.size}")
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
                .take(20)
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

                val existing = db.sportyQueries
                    .selectTeamsByLeague(id)
                    .executeAsList()

                if (existing.isNotEmpty()) {
                    println("⏳ Teams already cached for league=$leagueId → skipping API")
                    return@runCatching
                }

                val remote = api.getTeams(leagueId.toString())

                println("⚽ Fetching teams for league=$leagueId → received ${remote.size} teams")

                if (remote.isEmpty()) {
                    println("❌ API returned empty team list → nothing saved")
                    return@runCatching
                }

                val limited = remote.take(9)

                db.transaction {
                    limited.forEach { dto ->
                        val e = mapper.teamDtoToDb(
                            dto = dto,
                            leagueId = id,
                            leagueName = null
                        )

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

                val total = db.sportyQueries.selectTeamsByLeague(id).executeAsList().size
                println("🔥 Teams cached → total for league=$leagueId = $total")
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

            _favoritesEvents.emit(Unit)
        }
    }

    override suspend fun clearAllFavorites() {
        withContext(io) {
            db.sportyQueries.deleteAllFavorites()
            _favoritesEvents.emit(Unit)
        }
    }

    override suspend fun removeFavorite(type: FavoriteType, itemId: String) {
        withContext(io) {
            db.sportyQueries.deleteFavorite(type.name, itemId)
            _favoritesEvents.emit(Unit)
        }
    }
    override suspend fun getMatchDetails(matchId: String): Match {
        val base = getMatchById(matchId) ?: error("Match not found")

        val events = api.getMatchEvents(matchId.toInt()).map {
            Event(
                team = it.team.name,
                player = it.player?.name,
                assist = it.assist?.name,
                type = it.type,
                detail = it.detail,
                minute = it.time.elapsed ?: 0
            )
        }

        val lineupsDto = api.getMatchLineups(matchId.toInt())

        val teamIds = lineupsDto.mapNotNull { it.team.id }

        val players = teamIds.flatMap { teamId ->
            api.getPlayersByTeam(teamId)
        }

        fun findPhoto(name: String?): String? {
            if (name == null) return null
            return players.firstOrNull { it.name.equals(name, ignoreCase = true) }?.photo
        }

        val lineups = lineupsDto.map { dto ->
            Lineup(
                team = dto.team.name ?: "",
                coach = dto.coach?.name,
                formation = dto.formation,

                startXI = dto.startXI?.map { p ->
                    PlayerShort(
                        name = p.player.name ?: "",
                        number = p.player.number,
                        position = p.player.pos,
                        photo = findPhoto(p.player.name)
                    )
                } ?: emptyList(),

                substitutes = dto.substitutes?.map { p ->
                    PlayerShort(
                        name = p.player.name ?: "",
                        number = p.player.number,
                        position = p.player.pos,
                        photo = findPhoto(p.player.name)
                    )
                } ?: emptyList()
            )
        }

        val stats = api.getMatchStatistics(matchId.toInt()).map {
            TeamStatistics(
                team = it.team.name ?: "",
                stats = it.statistics.map { s -> StatItem(s.type, s.value) }
            )
        }

        return base.copy(
            events = events,
            lineups = lineups,
            statistics = stats
        )
    }

    override suspend fun addHistory(type: HistoryType, itemId: String) =
        withContext(io) {
            val now = Clock.System.now()
            db.sportyQueries.insertHistory(
                type = type.name,
                item_id = itemId,
                opened_at = now.toString()
            )
            Unit
        }

    override suspend fun getHistory(type: HistoryType?): List<HistoryItem> =
        withContext(io) {
            val rows = if (type == null) {
                db.sportyQueries.selectAllHistory().executeAsList()
            } else {
                db.sportyQueries.selectHistoryByType(type.name).executeAsList()
            }

            rows.map { row ->
                val hType = HistoryType.valueOf(row.type)
                val opened = Instant.parse(row.opened_at)

                when (hType) {
                    HistoryType.ARTICLE -> {
                        val article = getArticleById(row.item_id)
                        HistoryItem(
                            id = row.id,
                            type = hType,
                            itemId = row.item_id,
                            openedAt = opened,
                            article = article
                        )
                    }
                    HistoryType.MATCH -> {
                        val match = getMatchById(row.item_id)
                        HistoryItem(
                            id = row.id,
                            type = hType,
                            itemId = row.item_id,
                            openedAt = opened,
                            match = match
                        )
                    }
                }
            }.filter { it.article != null || it.match != null }
        }

    override suspend fun clearHistory() =
        withContext(io) {
            db.sportyQueries.deleteAllHistory()
            Unit
        }

    override suspend fun deleteHistoryItem(id: Long) =
        withContext(io) {
            db.sportyQueries.deleteHistoryById(id)
            Unit
        }

    override suspend fun getUserPreferences(): UserPreferences {
        return UserPreferences(
            favoriteLeagueIds = emptyList(),
            favoriteTeamIds = emptyList(),
            isDarkThemeEnabled = false,
            isDarkThemePurchased = false,
            isDataResetPurchased = false, 
            language = "en",
            primaryCountry = null,
            lastMatchesRefresh = null
        )
    }

    override suspend fun updateUserPreferences(prefs: UserPreferences) {

    }

    override suspend fun resetAllUserData() {
        db.sportyQueries.deleteAllFavorites()
        db.sportyQueries.deleteAllHistory()

        _resetEvents.emit(Unit)
    }

    override suspend fun getTeamLeagueStats(teamId: String): TeamLeagueStats? =
        withContext(io) {

            getCachedTeamStats(teamId)?.let {
                println("📌 Using cached team stats for team=$teamId")
                return@withContext it
            }

            println("🌐 Cache empty → loading stats from API for team=$teamId")

            val team = getTeamById(teamId) ?: return@withContext null
            val leagueId = team.leagueId ?: return@withContext null

            val dto = api.getStandings(leagueId) ?: return@withContext null

            val row = dto.standings
                .flatten()
                .firstOrNull { it.team.id.toString() == teamId }
                ?: return@withContext null

            val stats = TeamLeagueStats(
                teamId = teamId,
                name = row.team.name,
                logoUrl = row.team.logo,
                position = row.rank,
                played = row.all.played,
                points = row.points
            )
            
            saveTeamStats(stats)

            println("💾 Saved stats to DB for team=$teamId")

            return@withContext stats
        }

    private fun getCachedTeamStats(teamId: String): TeamLeagueStats? {
        return db.sportyQueries
            .selectTeamStatsById(teamId)
            .executeAsOneOrNull()
            ?.let {
                TeamLeagueStats(
                    teamId = it.team_id,
                    name = it.name,
                    logoUrl = it.logo_url,
                    position = it.position.toInt(),
                    played = it.played.toInt(),
                    points = it.points.toInt()
                )
            }
    }

    private fun saveTeamStats(stats: TeamLeagueStats) {
        db.sportyQueries.insertTeamStats(
            team_id = stats.teamId,
            name = stats.name,
            logo_url = stats.logoUrl,
            position = stats.position.toLong(),
            played = stats.played.toLong(),
            points = stats.points.toLong()
        )
    }
}