package com.example.sportys.billing

import com.example.sportys.model.Feature
import com.example.sportys.model.Theme
import com.example.sportys.repository.FeatureRepository
import com.example.sportys.repository.ThemeRepository
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
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
import kotlin.coroutines.resume

class IOSBillingRepository(
    private val themeRepository: ThemeRepository,
    private val featureRepository: FeatureRepository
) : BillingRepository {

    private val delegate = IOSBillingDelegate(themeRepository, featureRepository)

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

@OptIn(ExperimentalForeignApi::class)
class IOSBillingDelegate(
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
                cont.resume(PurchaseResult.Error("Product not found"))
                return@suspendCancellableCoroutine
            }

            continuations[productId] = { result ->
                cont.resume(result)
            }

            SKPaymentQueue.defaultQueue()
                .addPayment(SKPayment.paymentWithProduct(product))
        }

    suspend fun restorePurchases(): PurchaseResult =
        suspendCancellableCoroutine { cont ->
            continuations["restore"] = { result ->
                cont.resume(result)
            }
            SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
        }

    private suspend fun autoPurchaseFromPromotion(productId: String) {
        val product = products[productId] ?: return
        SKPaymentQueue.defaultQueue()
            .addPayment(SKPayment.paymentWithProduct(product))
    }

    override fun productsRequest(
        request: SKProductsRequest,
        didReceiveResponse: SKProductsResponse
    ) {
        didReceiveResponse.products?.forEach { any ->
            val product = any as? SKProduct ?: return@forEach
            val id = product.productIdentifier ?: return@forEach
            products[id] = product
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

                SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                    val id = tx.payment.productIdentifier ?: return@forEach
                    continuations.remove(id)?.invoke(PurchaseResult.Failure)
                    SKPaymentQueue.defaultQueue().finishTransaction(tx)
                }

                else -> {}
            }
        }
    }

    private fun handlePurchased(transaction: SKPaymentTransaction) {
        val id = transaction.payment.productIdentifier ?: return
        val callback =
            continuations.remove(id) ?: continuations.remove("restore")

        scope.launch {
            when {
                id.startsWith("theme_") -> {
                    themeRepository.markPurchased(id)
                    themeRepository.setCurrentTheme(id)
                }
                id == "reset_data" -> {
                    featureRepository.markPurchased(id)
                }
            }

            callback?.invoke(PurchaseResult.Success)
        }

        SKPaymentQueue.defaultQueue().finishTransaction(transaction)
    }
}