package com.example.sportys.billing

import com.example.sportys.model.Feature
import com.example.sportys.model.Theme
import com.example.sportys.repository.FeatureRepository
import com.example.sportys.repository.ThemeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.StoreKit.SKPayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.darwin.NSObject
import kotlin.collections.forEach

class IOSBillingRepository(
    private val themeRepository: ThemeRepository,
    private val featureRepository: FeatureRepository
) : BillingRepository {

    private val delegate = BillingDelegate(themeRepository, featureRepository)

    override suspend fun getThemes(): List<Theme> =
        themeRepository.getAllThemes()

    override suspend fun purchaseTheme(themeId: String): PurchaseResult =
        delegate.purchase(themeId)

    override suspend fun getFeatures(): List<Feature> =
        featureRepository.getAllFeatures()

    override suspend fun purchaseFeature(featureId: String): PurchaseResult =
        delegate.purchase(featureId)

    override suspend fun restorePurchases(): PurchaseResult =
        delegate.restorePurchases()
}

private class BillingDelegate(
    private val themeRepository: ThemeRepository,
    private val featureRepository: FeatureRepository
) : NSObject(),
    SKProductsRequestDelegateProtocol,
    SKPaymentTransactionObserverProtocol {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val productIds = setOf("theme_dark", "reset_data")

    private val products = mutableMapOf<String, SKProduct>()
    private val continuations = mutableMapOf<String, (PurchaseResult) -> Unit>()

    init {
        SKPaymentQueue.defaultQueue().addTransactionObserver(this)
        fetchProducts()

        PromotionConnector.onPromotionReceived = { productId ->
            println("🍏 Promotion received → $productId")
            scope.launch { autoPurchaseFromPromotion(productId) }
        }
    }

    private fun fetchProducts() {
        val request = SKProductsRequest(productIdentifiers = productIds)
        request.delegate = this
        request.start()
    }

    suspend fun purchase(productId: String): PurchaseResult =
        suspendCancellableCoroutine { cont ->

            val product = products[productId]
            if (product == null) {
                cont.resume(PurchaseResult.Error("Product $productId not found")) {}
                return@suspendCancellableCoroutine
            }

            continuations[productId] = { result ->
                cont.resume(result) {}
            }

            SKPaymentQueue.defaultQueue()
                .addPayment(SKPayment.paymentWithProduct(product))
        }

    private suspend fun autoPurchaseFromPromotion(productId: String) {
        val p = products[productId] ?: return
        SKPaymentQueue.defaultQueue().addPayment(
            SKPayment.paymentWithProduct(p)
        )
    }

    override fun productsRequest(
        request: SKProductsRequest,
        didReceiveResponse: SKProductsResponse
    ) {
        val list = didReceiveResponse.products ?: emptyList<Any>()

        list.forEach { any ->
            val product = any as? SKProduct ?: return@forEach
            val id = product.productIdentifier ?: return@forEach

            println("🍏 Loaded product: $id")

            products[id] = product
        }

        if (products.isEmpty()) {
            println("⚠️ No valid IAP products loaded. Check App Store Connect IDs!")
        }
    }

    override fun paymentQueue(
        queue: SKPaymentQueue,
        updatedTransactions: List<*>
    ) {
        updatedTransactions.forEach { any ->
            val tx = any as? SKPaymentTransaction ?: return@forEach

            when (tx.transactionState) {
                SKPaymentTransactionState.SKPaymentTransactionStatePurchased,
                SKPaymentTransactionState.SKPaymentTransactionStateRestored ->
                    handlePurchased(tx)

                SKPaymentTransactionState.SKPaymentTransactionStateFailed ->
                    handleFailed(tx)

                else -> {}
            }
        }
    }

    private fun handlePurchased(transaction: SKPaymentTransaction) {
        val id = transaction.payment.productIdentifier ?: return

        val callback = continuations.remove(id)

        scope.launch {
            try {
                if (id.startsWith("theme_")) {
                    themeRepository.markPurchased(id)
                    themeRepository.setCurrentTheme(id)
                }
                if (id == "reset_data") {
                    featureRepository.markPurchased(id)
                }

                callback?.invoke(PurchaseResult.Success)

            } catch (t: Throwable) {
                callback?.invoke(PurchaseResult.Error(t.message ?: "Unknown error"))
            } finally {
                SKPaymentQueue.defaultQueue().finishTransaction(transaction)
            }
        }
    }

    private fun handleFailed(transaction: SKPaymentTransaction) {
        val id = transaction.payment.productIdentifier
        continuations.remove(id)?.invoke(PurchaseResult.Failure)

        SKPaymentQueue.defaultQueue().finishTransaction(transaction)
    }

    suspend fun restorePurchases(): PurchaseResult =
        suspendCancellableCoroutine { cont ->

            continuations["restore"] = { result ->
                cont.resume(result) {}
            }

            SKPaymentQueue.defaultQueue().restoreCompletedTransactions()

            scope.launch {
                delay(4000)
                if (continuations.containsKey("restore")) {
                    continuations.remove("restore")?.invoke(PurchaseResult.Failure)
                }
            }
        }
}