package com.example.sportys.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.example.sportys.repository.FeatureRepository
import com.example.sportys.repository.ThemeRepository
import kotlinx.coroutines.CancellableContinuation
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.example.sportys.CurrentActivityHolder
import com.example.sportys.model.Feature
import com.example.sportys.model.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidBillingRepository(
    private val context: Context,
    private val themeRepository: ThemeRepository,
    private val featureRepository: FeatureRepository
) : BillingRepository {

    @Volatile private var isPurchaseInProgress = false
    private var continuation: CancellableContinuation<PurchaseResult>? = null
    private var isBillingReady = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val productIds = setOf("theme_dark", "reset_data")

    private val billingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases()
        .setListener(::onPurchaseUpdated)
        .build()

    init { startConnection() }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingServiceDisconnected() {
                isBillingReady = false
                Handler(Looper.getMainLooper()).postDelayed({
                    startConnection()
                }, 2000)
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    isBillingReady = true
                    restorePurchasesSilently()
                }
            }
        })
    }

    override suspend fun getThemes(): List<Theme> =
        themeRepository.getAllThemes()

    override suspend fun getFeatures(): List<Feature> =
        featureRepository.getAllFeatures()

    override suspend fun purchaseTheme(themeId: String): PurchaseResult =
        purchase(themeId)

    override suspend fun purchaseFeature(featureId: String): PurchaseResult =
        purchase(featureId)

    override suspend fun restorePurchases(): PurchaseResult =
        suspendCancellableCoroutine { cont ->

            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases.isNotEmpty()) {
                    handlePurchases(purchases)
                    cont.resume(PurchaseResult.Success) {}
                } else {
                    cont.resume(PurchaseResult.Failure) {}
                }
            }
        }

    private suspend fun purchase(productId: String): PurchaseResult =
        suspendCancellableCoroutine { cont ->

            if (!isBillingReady) {
                cont.resume(PurchaseResult.Error("Billing not ready")) {}
                return@suspendCancellableCoroutine
            }

            if (isPurchaseInProgress) {
                cont.resume(PurchaseResult.Error("Purchase already running")) {}
                return@suspendCancellableCoroutine
            }

            continuation = cont
            isPurchaseInProgress = true

            scope.launch {
                val details = queryProduct(productId)

                if (details == null) {
                    cont.resume(PurchaseResult.Error("Product not found")) {}
                    isPurchaseInProgress = false
                    return@launch
                }

                val params = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams
                                .newBuilder()
                                .setProductDetails(details)
                                .build()
                        )
                    )
                    .build()

                val activity = CurrentActivityHolder.current()
                if (activity == null) {
                    cont.resume(PurchaseResult.Error("No activity")) {}
                    isPurchaseInProgress = false
                    return@launch
                }

                billingClient.launchBillingFlow(activity, params)
            }
        }
    private fun onPurchaseUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) {
            continuation?.resume(PurchaseResult.Error("Billing error")) {}
            continuation = null
            isPurchaseInProgress = false
            return
        }

        handlePurchases(purchases)
    }
    private fun handlePurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->

            if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return@forEach

            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) { result ->

                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        grantPurchase(purchase)
                        continuation?.resume(PurchaseResult.Success) {}
                        continuation = null
                        isPurchaseInProgress = false
                    }
                }
            }
        }
    }

    private suspend fun grantPurchase(purchase: Purchase) {
        val id = purchase.products.firstOrNull() ?: return

        if (id == "theme_dark") {
            themeRepository.markPurchased(id)
            themeRepository.setCurrentTheme(id)
        }

        if (id == "reset_data") {
            featureRepository.markPurchased(id)
        }
    }

    // ─────────────────────────────────────────────────────────────
    private suspend fun queryProduct(productId: String): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        return billingClient.queryProductDetails(params)
            .productDetailsList
            ?.firstOrNull()
    }

    private fun restorePurchasesSilently() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { _, purchases ->
            if (purchases.isNotEmpty()) handlePurchases(purchases)
        }
    }
}