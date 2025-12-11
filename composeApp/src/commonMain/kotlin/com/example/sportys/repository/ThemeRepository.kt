package com.example.sportys.repository

import com.example.sportys.data.SportyS
import com.example.sportys.model.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

interface ThemeRepository {
    suspend fun initializeThemes()
    suspend fun getAllThemes(): List<Theme>
    suspend fun getCurrentThemeId(): String?
    suspend fun setCurrentTheme(themeId: String)
    suspend fun markPurchased(themeId: String)

    val currentThemeId: StateFlow<String?>
}

class ThemeRepositoryImpl(
    private val db: SportyS
) : ThemeRepository {

    private val q = db.sportyQueries

    private val _currentThemeId = MutableStateFlow<String?>(null)
    override val currentThemeId: StateFlow<String?> = _currentThemeId

    override suspend fun initializeThemes() = withContext(Dispatchers.Default) {

        q.transaction {
            val count = q.countThemes().executeAsOne()
            if (count == 0L) {
                q.insertTheme(
                    id = "theme_light",
                    name = "Light Theme",
                    is_paid = false,
                    purchased = true,
                    price = null
                )

                q.insertTheme(
                    id = "theme_dark",
                    name = "Dark Premium Theme",
                    is_paid = true,
                    purchased = false,
                    price = 1.99
                )
            }

            val cur = q.getCurrentThemeId().executeAsOneOrNull()
            if (cur == null) {
                q.upsertCurrentTheme("theme_light")
            }
        }

        _currentThemeId.value = q.getCurrentThemeId().executeAsOneOrNull()
    }

    override suspend fun getAllThemes(): List<Theme> = withContext(Dispatchers.Default) {
        q.selectAllTheme().executeAsList().map { row ->
            Theme(
                id = row.id,
                name = row.name,
                isPaid = row.is_paid ?: false,
                purchased = row.purchased ?: false,
                price = row.price
            )
        }
    }

    override suspend fun getCurrentThemeId(): String? = withContext(Dispatchers.Default) {
        val id = q.getCurrentThemeId().executeAsOneOrNull()
        _currentThemeId.value = id
        id
    }

    override suspend fun setCurrentTheme(themeId: String) = withContext(Dispatchers.Default) {
        q.upsertCurrentTheme(themeId)
        _currentThemeId.value = themeId
    }

    override suspend fun markPurchased(themeId: String) = withContext(Dispatchers.Default) {
        q.updatePurchased(themeId)
        q.upsertCurrentTheme(themeId)
        _currentThemeId.value = themeId
    }
}