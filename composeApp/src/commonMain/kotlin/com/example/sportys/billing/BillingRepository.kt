package com.example.sportys.billing

import com.example.sportys.model.Feature
import com.example.sportys.model.Theme

interface BillingRepository {

    suspend fun getThemes(): List<Theme>
    suspend fun purchaseTheme(themeId: String): PurchaseResult
    suspend fun getFeatures(): List<Feature>
    suspend fun purchaseFeature(featureId: String): PurchaseResult

    suspend fun restorePurchases(): PurchaseResult
}

sealed class PurchaseResult {
    object Success : PurchaseResult()
    object Failure : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}