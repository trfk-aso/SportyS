package com.example.sportys.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportys.billing.BillingRepository
import com.example.sportys.billing.PurchaseResult
import com.example.sportys.repository.FootballRepository
import com.example.sportys.repository.ThemeRepository
import com.example.sportys.share.getShareManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTheme(val id: String, val title: String) {
    LIGHT("theme_light", "Light Minimal"),
    DARK("theme_dark", "Dark Premium")
}

data class ThemeUi(
    val theme: AppTheme,
    val locked: Boolean,
    val selected: Boolean
)

class SettingsViewModel(
    private val repo: FootballRepository,
    private val billing: BillingRepository,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {

            val prefs = repo.getUserPreferences()
            val themes = billing.getThemes()
            val features = billing.getFeatures()

            val isDarkPurchased = themes.firstOrNull { it.id == "theme_dark" }?.purchased == true
            val isResetPurchased = features.firstOrNull { it.id == "reset_data" }?.purchased == true

            val uiThemes = listOf(
                ThemeUi(
                    theme = AppTheme.LIGHT,
                    locked = false,
                    selected = prefs.isDarkThemeEnabled.not()
                ),
                ThemeUi(
                    theme = AppTheme.DARK,
                    locked = !isDarkPurchased,
                    selected = prefs.isDarkThemeEnabled
                )
            )

            _state.update {
                it.copy(
                    isDark = prefs.isDarkThemeEnabled,
                    themes = uiThemes,
                    isPremiumThemeUnlocked = isDarkPurchased,
                    isResetPurchased = isResetPurchased
                )
            }

            println("🔥 reset_data purchased = $isResetPurchased")
        }
    }

    fun toggleTheme() {
        val newThemeValue = !_state.value.isDark

        _state.update { it.copy(isDark = newThemeValue) }

        viewModelScope.launch {
            val themeId = if (newThemeValue) "theme_dark" else "theme_light"

            repo.updateUserPreferences(
                repo.getUserPreferences().copy(isDarkThemeEnabled = newThemeValue)
            )

            themeRepository.setCurrentTheme(themeId)

            println("💾 Saved theme = $themeId")
        }
    }

    fun buyPremiumTheme() {
        viewModelScope.launch {

            println("🛒 Starting purchase for DARK theme…")

            val result = billing.purchaseTheme("theme_dark")

            when (result) {

                is PurchaseResult.Success -> {
                    println("🎉 DARK theme purchased successfully!")

                    themeRepository.markPurchased("theme_dark")

                    themeRepository.setCurrentTheme("theme_dark")

                    repo.updateUserPreferences(
                        repo.getUserPreferences().copy(
                            isDarkThemeEnabled = true
                        )
                    )

                    _state.update { state ->
                        state.copy(
                            isPremiumThemeUnlocked = true,
                            isDark = true,
                            showPaywall = false,
                            themes = state.themes.map { uiTheme ->
                                uiTheme.copy(
                                    locked = uiTheme.theme == AppTheme.DARK && false,
                                    selected = uiTheme.theme == AppTheme.DARK
                                )
                            }
                        )
                    }

                    println("🌙 DARK theme applied & saved successfully!")
                }

                is PurchaseResult.Error -> {
                    println("❌ Purchase ERROR: ${result.message}")
                    _state.update { it.copy(showPaywall = false) }
                }

                PurchaseResult.Failure -> {
                    println("❌ Purchase FAILED")
                    _state.update { it.copy(showPaywall = false) }
                }
            }
        }
    }

    fun onResetClicked() {
        if (_state.value.isResetPurchased) {
            showResetDialog(true)
        } else {
            buyResetFeature()
        }
    }

    private fun buyResetFeature() {
        viewModelScope.launch {
            when (val result = billing.purchaseFeature("reset_data")) {

                is PurchaseResult.Success -> {
                    println("🎉 Reset feature purchased!")
                    _state.update { it.copy(isResetPurchased = true) }
                    showResetDialog(true)
                }

                is PurchaseResult.Error -> {
                    println("❌ Reset feature error: ${result.message}")
                }

                PurchaseResult.Failure -> {
                    println("❌ Reset feature failed")
                }
            }
        }
    }

    fun showResetDialog(show: Boolean) {
        _state.update { it.copy(showResetDialog = show) }
    }

    fun resetAll() {
        viewModelScope.launch {
            repo.resetAllUserData()
            showResetDialog(false)
            println("🧹 App data reset!")
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            val json = repo.exportUserDataAsJson()
            val manager = getShareManager()

            val bytes = manager.exportJson(json)
            manager.shareJson("sportys_export.json", bytes)

            println("📤 JSON exported")
        }
    }

    fun selectTheme(theme: AppTheme) {

        if (theme == AppTheme.DARK && !_state.value.isPremiumThemeUnlocked) {
            _state.update { it.copy(showPaywall = true) }
            return
        }

        viewModelScope.launch {
            val enableDark = theme == AppTheme.DARK

            repo.updateUserPreferences(
                repo.getUserPreferences().copy(isDarkThemeEnabled = enableDark)
            )

            _state.update {
                it.copy(
                    isDark = enableDark,
                    themes = it.themes.map {
                        it.copy(selected = it.theme == theme)
                    }
                )
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            when (billing.restorePurchases()) {

                is PurchaseResult.Success -> {
                    println("🔄 Purchases restored successfully")
                    loadInitialData()
                }

                is PurchaseResult.Error -> {
                    println("❌ Restore error")
                }

                PurchaseResult.Failure -> {
                    println("❌ No purchases to restore")
                }
            }
        }
    }
}
data class SettingsState(
    val isDark: Boolean = false,
    val themes: List<ThemeUi> = emptyList(),
    val isPremiumThemeUnlocked: Boolean = false,
    val isResetPurchased: Boolean = false,
    val showResetDialog: Boolean = false,
    val showPaywall: Boolean = false
)