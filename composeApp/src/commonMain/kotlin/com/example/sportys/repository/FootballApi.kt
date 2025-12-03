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
import com.example.sportys.model.dto.GNewsResponse
import com.example.sportys.model.dto.LeagueDTO
import com.example.sportys.model.dto.LeagueResponse
import com.example.sportys.model.dto.MatchDTO
import com.example.sportys.model.dto.MatchResponse
import com.example.sportys.model.dto.NewsApiResponse
import com.example.sportys.model.dto.StandingsLeagueDTO
import com.example.sportys.model.dto.StandingsResponseDTO
import com.example.sportys.model.dto.TeamDTO
import com.example.sportys.model.dto.TeamResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface FootballApi {

    suspend fun getLatestArticles(): List<ArticleDTO>

    suspend fun getMatches(date: String): List<MatchDTO>
    suspend fun getLiveMatches(): List<MatchDTO>

    suspend fun getLeagues(): List<LeagueDTO>

    suspend fun getTeams(leagueId: String): List<TeamDTO>

    suspend fun getStandings(leagueId: String): StandingsLeagueDTO
}

class FootballApiImpl(
    private val client: HttpClient,
    private val newsApiKey: String,
    private val footballApiKey: String
) : FootballApi {

    override suspend fun getLatestArticles(): List<ArticleDTO> {
        val response: GNewsResponse = client.get("https://gnews.io/api/v4/search") {
            parameter("q", "premier league OR champions league OR la liga OR bundesliga OR serie a OR ligue 1 OR uefa OR fifa OR messi OR ronaldo OR real madrid OR barcelona")
            parameter("lang", "en")
            parameter("max", 20)
            parameter("token", newsApiKey)
        }.body()

        response.articles.forEach { article ->
            println("IMAGE_URL => ${article.image}")
        }

        return response.articles
    }

    override suspend fun getMatches(date: String): List<MatchDTO> {
        val resp: MatchResponse = client.get("https://v3.football.api-sports.io/fixtures") {
            parameter("date", date)
            header("x-apisports-key", footballApiKey)
        }.body()

        return resp.response
    }

    override suspend fun getLiveMatches(): List<MatchDTO> {
        val resp: MatchResponse = client.get("https://v3.football.api-sports.io/fixtures") {
            parameter("live", "all")
            header("x-apisports-key", footballApiKey)
        }.body()

        return resp.response
    }

    override suspend fun getLeagues(): List<LeagueDTO> {
        val resp: LeagueResponse = client.get("https://v3.football.api-sports.io/leagues") {
            header("x-apisports-key", footballApiKey)
        }.body()

        return resp.response.map { it.league }
    }

    override suspend fun getTeams(leagueId: String): List<TeamDTO> {
        val resp: TeamResponse = client.get("https://v3.football.api-sports.io/teams") {
            parameter("league", leagueId)
            parameter("season", "2024")
            header("x-apisports-key", footballApiKey)
        }.body()

        return resp.response.map { it.team }
    }

    override suspend fun getStandings(leagueId: String): StandingsLeagueDTO {
        val resp: StandingsResponseDTO = client.get("https://v3.football.api-sports.io/standings") {
            parameter("league", leagueId)
            parameter("season", "2024")
            header("x-apisports-key", footballApiKey)
        }.body()

        return resp.response.first().league
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

    fun matchDtoToDb(dto: MatchDTO) = Matches(
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

        score_home = dto.goals.home?.toLong(),
        score_away = dto.goals.away?.toLong()
    )

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
            startTime = Instant.parse(row.start_time),
            score = if (row.score_home != null)
                MatchScore(
                    home = row.score_home.toInt(),
                    away = row.score_away?.toInt() ?: 0
                )
            else null
        )

    fun leagueDtoToDb(dto: LeagueDTO) = Leagues(
        id = dto.id.toString(),
        name = dto.name,
        country = dto.country,
        logo_url = dto.logo
    )

    fun leagueDbToDomain(row: Leagues): League =
        League(
            id = row.id,
            name = row.name,
            country = row.country,
            logoUrl = row.logo_url
        )

    fun teamDtoToDb(dto: TeamDTO) = Teams(
        id = dto.id.toString(),
        name = dto.name,
        logo_url = dto.logo,
        league_id = dto.leagueId?.toString(),
        league_name = dto.leagueName,
        position = dto.position?.toLong()
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
            primaryCountry = row.primary_country
        )

    fun userPrefsDomainToDb(prefs: UserPreferences) = User_preferences(
        id = 0,
        favorite_league_ids = json.encodeToString(prefs.favoriteLeagueIds),
        favorite_team_ids = json.encodeToString(prefs.favoriteTeamIds),
        is_dark_theme_enabled = if (prefs.isDarkThemeEnabled) 1 else 0,
        is_dark_theme_purchased = if (prefs.isDarkThemePurchased) 1 else 0,
        is_data_reset_purchased = if (prefs.isDataResetPurchased) 1 else 0,
        language = prefs.language,
        primary_country = prefs.primaryCountry
    )
}