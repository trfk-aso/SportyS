package com.example.sportys.repository

import com.example.sportys.data.SportyS
import com.example.sportys.model.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

interface FeatureRepository {
    suspend fun initializeFeatures()
    suspend fun getAllFeatures(): List<Feature>
    suspend fun markPurchased(id: String)

    val purchasedFeatures: StateFlow<Set<String>>
}

class FeatureRepositoryImpl(private val db: SportyS) : FeatureRepository {

    private val q = db.sportyQueries

    private val _purchasedFeatures = MutableStateFlow<Set<String>>(emptySet())
    override val purchasedFeatures: StateFlow<Set<String>> = _purchasedFeatures

    override suspend fun initializeFeatures() = withContext(Dispatchers.Default) {
        q.transaction {
            val count = q.selectAllFeature().executeAsList().size
            if (count == 0) {
                q.insertFeature(
                    id = "reset_data",
                    name = "Reset App Data",
                    is_paid = true,
                    purchased = false,
                    price = 1.99
                )
            }
        }

        val purchased = q.selectAllFeature()
            .executeAsList()
            .filter { it.purchased == true }
            .map { it.id }
            .toSet()

        _purchasedFeatures.value = purchased

        println("🟢 FEATURES initialized: purchased = $purchased")
    }

    override suspend fun getAllFeatures(): List<Feature> =
        q.selectAllFeature().executeAsList().map { row ->
            Feature(
                id = row.id,
                name = row.name,
                isPaid = row.is_paid ?: false,
                purchased = row.purchased ?: false,
                price = row.price
            )
        }

    override suspend fun markPurchased(id: String) {
        q.updatePurchasedFeature(id)

        _purchasedFeatures.value = _purchasedFeatures.value + id

        println("🟢 FEATURE PURCHASE SAVED: $id")
    }
}