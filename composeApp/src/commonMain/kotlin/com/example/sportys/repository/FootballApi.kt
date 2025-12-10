package com.example.sportys.repository

import com.example.sportys.data.Articles
import com.example.sportys.data.Favorites
import com.example.sportys.data.Leagues
import com.example.sportys.data.Matches
import com.example.sportys.data.Teams
import com.example.sportys.data.User_preferences
import com.example.sportys.model.Article
import com.example.sportys.model.FavoriteItem
import com.example.sportys.model.FavoriteType
import com.example.sportys.model.League
import com.example.sportys.model.LeagueShort
import com.example.sportys.model.Match
import com.example.sportys.model.MatchScore
import com.example.sportys.model.MatchStatus
import com.example.sportys.model.Team
import com.example.sportys.model.TeamShort
import com.example.sportys.model.UserPreferences
import com.example.sportys.model.dto.ArticleDTO
import com.example.sportys.model.dto.EventDTO
import com.example.sportys.model.dto.EventsResponse
import com.example.sportys.model.dto.FixtureDTO
import com.example.sportys.model.dto.GNewsResponse
import com.example.sportys.model.dto.LeagueDTO
import com.example.sportys.model.dto.LeagueDataDTO
import com.example.sportys.model.dto.LeagueResponse
import com.example.sportys.model.dto.LineupDTO
import com.example.sportys.model.dto.LineupsResponse
import com.example.sportys.model.dto.MatchDTO
import com.example.sportys.model.dto.MatchResponse
import com.example.sportys.model.dto.NewsApiResponse
import com.example.sportys.model.dto.PlayerPhotoDTO
import com.example.sportys.model.dto.PlayersResponse
import com.example.sportys.model.dto.StandingsLeagueDTO
import com.example.sportys.model.dto.StandingsResponseDTO
import com.example.sportys.model.dto.StatisticsDTO
import com.example.sportys.model.dto.StatisticsResponse
import com.example.sportys.model.dto.TeamDTO
import com.example.sportys.model.dto.TeamResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface FootballApi {

    suspend fun getLatestArticles(): List<ArticleDTO>

    suspend fun getMatches(date: String): List<MatchDTO>
    suspend fun getLiveMatches(): List<MatchDTO>

    suspend fun getLeagues(): List<LeagueDataDTO>

    suspend fun getTeams(leagueId: String): List<TeamDTO>

    suspend fun getStandings(leagueId: String): StandingsLeagueDTO?
    suspend fun getMatchEvents(matchId: Int): List<EventDTO>
    suspend fun getMatchLineups(matchId: Int): List<LineupDTO>
    suspend fun getMatchStatistics(matchId: Int): List<StatisticsDTO>
    suspend fun getMatchesByLeague(leagueId: Int, season: Int = 2024): List<MatchDTO>
    suspend fun getMatchesByLeagueAndDate(leagueId: Int, date: String): List<MatchDTO>
    suspend fun getPlayersByTeam(teamId: Int): List<PlayerPhotoDTO>
}

class FootballApiImpl(
    private val client: HttpClient,
    private val newsApiKey: String,
    private val footballApiKey: String
) : FootballApi {

    override suspend fun getMatchesByLeagueAndDate(leagueId: Int, date: String): List<MatchDTO> {

        val respText = client.get("https://v3.football.api-sports.io/fixtures") {
            parameter("league", leagueId)
            parameter("season", "2022")
            parameter("date", date)
            header("x-apisports-key", footballApiKey)
        }.bodyAsText()

        println("🔵 API RESPONSE for league=$leagueId date=$date\n$respText")

        val resp = Json.decodeFromString<MatchResponse>(respText)
        return resp.response
    }

    override suspend fun getMatchesByLeague(leagueId: Int, season: Int): List<MatchDTO> {
        val resp: MatchResponse = client.get("https://v3.football.api-sports.io/fixtures") {
            parameter("league", leagueId)
            parameter("season", season)
            header("x-apisports-key", footballApiKey)
        }.body()

        return resp.response
    }

    override suspend fun getLatestArticles(): List<ArticleDTO> {
        val response: GNewsResponse = client.get("https://gnews.io/api/v4/search") {
            parameter("q", "football OR soccer OR premier league OR champions league")
            parameter("lang", "en")
            parameter("max", 20)
            parameter("sortby", "publishedAt")
            parameter("token", newsApiKey)
        }.body()

        println("Articles count = ${response.articles.size}")

        return response.articles
    }

    override suspend fun getMatches(date: String): List<MatchDTO> {

        val response = client.get("https://v3.football.api-sports.io/fixtures") {
            parameter("date", date)
            header("x-apisports-key", footballApiKey)
        }

        val raw = response.bodyAsText()
        println("📄 RAW API MATCHES ($date): $raw")

        val resp: MatchResponse = response.body()

        println("📌 Parsed API MATCHES ($date) → count=${resp.response.size}")

        return resp.response
    }

    override suspend fun getPlayersByTeam(teamId: Int): List<PlayerPhotoDTO> {
        val resp: PlayersResponse = client.get("https://v3.football.api-sports.io/players") {
            parameter("team", teamId)
            parameter("season", "2024")
            header("x-apisports-key", footballApiKey)
        }.body()

        return resp.response.map { it.player }
    }

    override suspend fun getLiveMatches(): List<MatchDTO> {
        val resp: MatchResponse = client.get("https://v3.football.api-sports.io/fixtures") {
            parameter("live", "all")
            header("x-apisports-key", footballApiKey)
        }.body()

        return resp.response
    }

    override suspend fun getLeagues(): List<LeagueDataDTO> {
        println("🟦 [API] Fetching leagues...")

        val raw = client.get("https://v3.football.api-sports.io/leagues") {
            header("x-apisports-key", footballApiKey)
        }.bodyAsText()

        println("🟪 RAW LEAGUES RESPONSE:\n$raw")

        return try {
            val resp = Json {
                ignoreUnknownKeys = true
            }.decodeFromString<LeagueResponse>(raw)

            println("🟩 Parsed leagues count = ${resp.response.size}")

            resp.response.forEach { item ->
                println("   • leagueId=${item.league.id}  name=${item.league.name}  country=${item.country?.name}")
            }

            resp.response

        } catch (e: Exception) {
            println("🟥 ERROR PARSING LEAGUES: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getTeams(leagueId: String): List<TeamDTO> {
        println("🟦 [API] Fetching teams for league=$leagueId...")

        val raw = client.get("https://v3.football.api-sports.io/teams") {
            parameter("league", leagueId)
            parameter("season", "2022")
            header("x-apisports-key", footballApiKey)
        }.bodyAsText()

        println("🟪 RAW TEAMS RESPONSE (league=$leagueId):\n$raw")

        return try {
            val resp = Json { ignoreUnknownKeys = true }
                .decodeFromString<TeamResponse>(raw)

            val teams = resp.response.map { it.team }

            println("🟩 Parsed ${teams.size} teams → taking max 9")

            teams.take(9)

        } catch (e: Exception) {
            println("🟥 ERROR PARSING TEAMS (league=$leagueId): ${e.message}")
            emptyList()
        }
    }

    override suspend fun getStandings(leagueId: String): StandingsLeagueDTO? {
        val raw = client.get("https://v3.football.api-sports.io/standings") {
            parameter("league", leagueId)
            parameter("season", "2023")
            header("x-apisports-key", footballApiKey)
        }.bodyAsText()

        println("🔵 RAW STANDINGS RESPONSE for league=$leagueId\n$raw")

        val resp = try {
            Json { ignoreUnknownKeys = true }
                .decodeFromString<StandingsResponseDTO>(raw)
        } catch (e: Exception) {
            println("❌ ERROR PARSING STANDINGS (leagueId=$leagueId): ${e.message}")
            return null
        }

        val league = resp.response.firstOrNull()?.league

        if (league == null) {
            println("❌ No standings found for leagueId=$leagueId")
            println("Parsed RESP.response = ${resp.response}")
            return null
        }

        return league
    }

    override suspend fun getMatchEvents(matchId: Int): List<EventDTO> {
        val resp: EventsResponse = client.get("https://v3.football.api-sports.io/fixtures/events") {
            parameter("fixture", matchId)
            header("x-apisports-key", footballApiKey)
        }.body()

        return resp.response
    }

    override suspend fun getMatchLineups(matchId: Int): List<LineupDTO> {
        val resp: LineupsResponse = client.get("https://v3.football.api-sports.io/fixtures/lineups") {
            parameter("fixture", matchId)
            header("x-apisports-key", footballApiKey)
        }.body()

        return resp.response
    }

    override suspend fun getMatchStatistics(matchId: Int): List<StatisticsDTO> {
        val resp: StatisticsResponse = client.get("https://v3.football.api-sports.io/fixtures/statistics") {
            parameter("fixture", matchId)
            header("x-apisports-key", footballApiKey)
        }.body()

        return resp.response
    }
}

class FootballDbMapper {

    private val json = Json
    fun articleDtoToDb(dto: ArticleDTO) = Articles(
        id = dto.title.hashCode().toString(),
        title = dto.title,
        summary = dto.description,
        content = dto.content,
        image_url = dto.image,
        published_at = dto.publishedAt,
        source = dto.source.name,
        tags_json = "[]"
    )

    fun articleDbToDomain(row: Articles): Article =
        Article(
            id = row.id,
            title = row.title,
            summary = row.summary,
            content = row.content,
            imageUrl = row.image_url,
            publishedAt = Instant.parse(row.published_at),
            source = row.source,
            tags = emptyList()
        )

    fun matchDtoToDomain(dto: MatchDTO): Match =
        Match(
            id = dto.fixture.id.toString(),

            homeTeam = TeamShort(
                id = dto.teams.home.id.toString(),
                name = dto.teams.home.name,
                logoUrl = dto.teams.home.logo
            ),

            awayTeam = TeamShort(
                id = dto.teams.away.id.toString(),
                name = dto.teams.away.name,
                logoUrl = dto.teams.away.logo
            ),

            league = LeagueShort(
                id = dto.league.id.toString(),
                name = dto.league.name,
                country = dto.league.country,
                logoUrl = dto.league.logo
            ),

            status = when (dto.fixture.status.short) {
                "NS" -> MatchStatus.SCHEDULED
                "1H", "2H" -> MatchStatus.LIVE
                "FT" -> MatchStatus.FINISHED
                "PST" -> MatchStatus.POSTPONED
                "CANC" -> MatchStatus.CANCELED
                else -> MatchStatus.SCHEDULED
            },

            startTime = parseInstantSafe(dto.fixture.date),

            score = if (dto.goals.home != null)
                MatchScore(
                    home = dto.goals.home,
                    away = dto.goals.away ?: 0
                )
            else null,

            events = emptyList(),
            lineups = emptyList(),
            statistics = emptyList()
        )

    fun matchDtoToDb(dto: MatchDTO): Matches {

        val utcMatchDate = Instant.parse(dto.fixture.date)
            .toLocalDateTime(TimeZone.UTC)
            .date
            .toString()

        return Matches(
            id = dto.fixture.id.toString(),

            home_team_id = dto.teams.home.id.toString(),
            home_team_name = dto.teams.home.name,
            home_team_logo = dto.teams.home.logo,

            away_team_id = dto.teams.away.id.toString(),
            away_team_name = dto.teams.away.name,
            away_team_logo = dto.teams.away.logo,

            league_id = dto.league.id.toString(),
            league_name = dto.league.name,
            league_country = dto.league.country,

            status = dto.fixture.status.short,

            start_time = dto.fixture.date,
            match_date = utcMatchDate,

            score_home = dto.goals.home?.toLong(),
            score_away = dto.goals.away?.toLong()
        )
    }

    fun matchDbToDomain(row: Matches): Match =
        Match(
            id = row.id,
            homeTeam = TeamShort(
                id = row.home_team_id,
                name = row.home_team_name,
                logoUrl = row.home_team_logo
            ),
            awayTeam = TeamShort(
                id = row.away_team_id,
                name = row.away_team_name,
                logoUrl = row.away_team_logo
            ),
            league = LeagueShort(
                id = row.league_id,
                name = row.league_name,
                country = row.league_country,
                logoUrl = null
            ),
            status = when (row.status) {
                "NS" -> MatchStatus.SCHEDULED
                "1H", "2H" -> MatchStatus.LIVE
                "FT" -> MatchStatus.FINISHED
                "PST" -> MatchStatus.POSTPONED
                "CANC" -> MatchStatus.CANCELED
                else -> MatchStatus.SCHEDULED
            },

            startTime = parseInstantSafe(row.start_time),

            score = if (row.score_home != null)
                MatchScore(
                    home = row.score_home.toInt(),
                    away = row.score_away?.toInt() ?: 0
                )
            else null
        )

    private fun parseInstantSafe(raw: String): Instant {
        return try {
            Instant.parse(raw)
        } catch (e: Exception) {
            Instant.parse(raw.replace("+00:00", "Z"))
        }
    }

    fun leagueDtoToDb(dto: LeagueDataDTO) = Leagues(
        id = dto.league.id.toString(),
        name = dto.league.name,
        country = dto.country?.name,
        logo_url = dto.league.logo
    )

    fun leagueDbToDomain(row: Leagues): League =
        League(
            id = row.id,
            name = row.name,
            country = row.country,
            logoUrl = row.logo_url
        )

    fun teamDtoToDb(dto: TeamDTO, leagueId: String, leagueName: String?) = Teams(
        id = dto.id.toString(),
        name = dto.name,
        logo_url = dto.logo,
        league_id = leagueId,
        league_name = leagueName,
        position = null
    )

    fun teamDbToDomain(row: Teams): Team =
        Team(
            id = row.id,
            name = row.name,
            logoUrl = row.logo_url,
            leagueId = row.league_id,
            leagueName = row.league_name,
            position = row.position?.toInt()
        )

    fun favoriteDbToDomain(row: Favorites): FavoriteItem =
        FavoriteItem(
            id = row.id,
            type = FavoriteType.valueOf(row.type),
            itemId = row.item_id,
            createdAt = Instant.parse(row.created_at)
        )

    fun userPrefsDbToDomain(row: User_preferences): UserPreferences =
        UserPreferences(
            favoriteLeagueIds = json.decodeFromString(row.favorite_league_ids),
            favoriteTeamIds = json.decodeFromString(row.favorite_team_ids),
            isDarkThemeEnabled = row.is_dark_theme_enabled == 1L,
            isDarkThemePurchased = row.is_dark_theme_purchased == 1L,
            isDataResetPurchased = row.is_data_reset_purchased == 1L,
            language = row.language,
            primaryCountry = row.primary_country,
            lastMatchesRefresh = row.last_matches_refresh
        )

    fun userPrefsDomainToDb(prefs: UserPreferences) = User_preferences(
        id = 0,
        favorite_league_ids = json.encodeToString(prefs.favoriteLeagueIds),
        favorite_team_ids = json.encodeToString(prefs.favoriteTeamIds),
        is_dark_theme_enabled = if (prefs.isDarkThemeEnabled) 1 else 0,
        is_dark_theme_purchased = if (prefs.isDarkThemePurchased) 1 else 0,
        is_data_reset_purchased = if (prefs.isDataResetPurchased) 1 else 0,
        language = prefs.language,
        primary_country = prefs.primaryCountry,
        last_matches_refresh = prefs.lastMatchesRefresh
    )

}