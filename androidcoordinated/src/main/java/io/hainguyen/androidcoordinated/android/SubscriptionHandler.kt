package org.de_studio.diary.android

import android.app.Activity
import com.android.billingclient.api.*
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Completable
import io.reactivex.Single
import org.de_studio.diary.android.crashReporter.CrashReporter
import org.de_studio.diary.data.AnalyticsManager
import org.de_studio.diary.data.BillingException
import org.de_studio.diary.utils.Cons
import org.de_studio.diary.utils.extensionFunction.currentTime
import org.de_studio.diary.utils.extensionFunction.getAppContext
import timber.log.Timber


/**
 * Created by HaiNguyen on 11/2/17.
 */
class SubscriptionHandler(val preference: Preference, val analyticsManager: AnalyticsManager, val appEventRL: PublishRelay<AppEvent>): PurchasesUpdatedListener {
    var clientInstant: BillingClient? = null

    fun checkSubscriptionAndUpdatePreference(): Completable {

        return Completable.create { emitter ->
            getBillingClient().startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                }

                override fun onBillingSetupFinished(responseCode: Int) {
                    val result =  getBillingClient().queryPurchases(BillingClient.SkuType.SUBS)
                    if (responseOk(result)) {
                        Timber.e("checkSubscriptionAndUpdatePreference ok")
                        if (isPremium(result)) preference.setUserAsPremium() else preference.setUserAsFree()
                    } else {
                        CrashReporter.log("Can't check subscription")
                    }
                    emitter.onComplete()
                }
            })
        }
    }

    fun queryItemsToBuy(): Single<List<ItemToBuy>> {
        return Single.create<List<ItemToBuy>> { emitter ->
            val timeStart = currentTime()
            Timber.e("queryItemsToBuy time start = ${currentTime()}")
            getBillingClient()
                    .startConnection(object : BillingClientStateListener {
                        override fun onBillingSetupFinished(setupResponse: Int) {
                            if (isResponseOk(setupResponse)) {
                                getBillingClient()
                                        .querySkuDetailsAsync(
                                                SkuDetailsParams.newBuilder()
                                                        .setType(BillingClient.SkuType.SUBS)
                                                        .setSkusList(listOf(SKU_SUBSCRIPTION_MONTHLY, SKU_SUBSCRIPTION_YEARLY))
                                                        .build(),
                                                { queryResponse, skuDetailsList ->
                                                    if (isResponseOk(queryResponse) && skuDetailsList != null) {
                                                        skuDetailsList.map {
                                                            when (it.sku) {
                                                                SKU_SUBSCRIPTION_MONTHLY -> ItemToBuy.MonthlySubscription(it.price)
                                                                SKU_SUBSCRIPTION_YEARLY -> ItemToBuy.YearlySubscription(it.price)
                                                                else -> throw BillingException("querySkuDetailsAsync wrong sku ${it.sku}")
                                                            }
                                                        }.apply {
                                                            Timber.e("onBillingSetupFinished time finish ${currentTime()}, time spent = ${currentTime() - timeStart}")
                                                            emitter.onSuccess(this)
                                                        }
                                                    } else emitter.onError(BillingException("queryResponseCode not ok $queryResponse"))
                                                }
                                        )
                            } else {
                                BillingException("When queryItemsToBuy, responseCode not ok $setupResponse")
                                        .apply {
                                            emitter.onError(this)
                                            CrashReporter.logException(this)
                                        }
                            }
                        }

                        override fun onBillingServiceDisconnected() {
                            emitter.onError(BillingException("onBillingServiceDisconnected can't connect"))
                        }
                    })
        }
    }

    fun launchPurchaseFlow(activity: Activity, sku: String): Completable {
        return Completable.fromAction {
            val builder = BillingFlowParams.newBuilder()
                    .setSku(sku).setType(BillingClient.SkuType.SUBS)
            val responseCode = getBillingClient().launchBillingFlow(activity, builder.build())
            Timber.e("showPurchaseDialog responseCode = $responseCode")
        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.sku == Cons.SKU_SUBSCRIPTION_MONTHLY || purchase.sku == Cons.SKU_SUBSCRIPTION_YEARLY) {
            preference.setUserAsPremium()
            analyticsManager.setUserPropertyAsPayUser()
            appEventRL.accept(PremiumUserConfirmed)
        } else
            throw IllegalArgumentException("what is this: " + purchase)
    }

    fun getBillingClient(): BillingClient {
        if (clientInstant == null) clientInstant = BillingClient.newBuilder(getAppContext()).setListener(this).build()
        return clientInstant!!
    }

    private fun isPremium(purchasesResult: Purchase.PurchasesResult): Boolean =
            purchasesResult.purchasesList.any { it.sku == SKU_SUBSCRIPTION_MONTHLY || it.sku == SKU_SUBSCRIPTION_YEARLY }

    private fun responseOk(purchasesResult: Purchase.PurchasesResult) =
            purchasesResult.responseCode == BillingClient.BillingResponse.OK

    private fun isResponseOk(responseCode: Int): Boolean =
            responseCode == BillingClient.BillingResponse.OK

    companion object {
        const val SKU_SUBSCRIPTION_MONTHLY = "subscription_monthly"
        const val SKU_SUBSCRIPTION_YEARLY = "subscription_yearly"
    }
}

sealed class ItemToBuy(val sku: String, val price: String){
    class MonthlySubscription( price: String): ItemToBuy(SubscriptionHandler.SKU_SUBSCRIPTION_MONTHLY, price)
    class YearlySubscription( price: String): ItemToBuy(SubscriptionHandler.SKU_SUBSCRIPTION_YEARLY, price)
}
