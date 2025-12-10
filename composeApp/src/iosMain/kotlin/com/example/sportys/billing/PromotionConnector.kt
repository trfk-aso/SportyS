package com.example.sportys.billing

object PromotionConnector {
    var onPromotionReceived: ((String) -> Unit)? = null

    fun triggerPromotion(productId: String) {
        println(" Kotlin Connector received: $productId")
        onPromotionReceived?.invoke(productId)
    }
}