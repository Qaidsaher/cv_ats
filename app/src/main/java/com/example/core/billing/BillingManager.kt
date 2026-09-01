package com.example.core.billing

import android.content.Context
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PurchaseState {
    object Idle : PurchaseState
    object Loading : PurchaseState
    object Success : PurchaseState
    data class Error(val message: String) : PurchaseState
}

class BillingManager(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val scope: CoroutineScope
) {
    companion object {
        const val PRODUCT_ID_PRO_LIFETIME = "resume_pro_lifetime"
    }

    val isProUser: StateFlow<Boolean> = preferencesRepository.isProUser.stateIn(
        scope = scope,
        started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
        initialValue = false
    )

    fun purchaseLifetimePro(onResult: (Boolean, String?) -> Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                // In production, initiate Google Play Billing client flow:
                // BillingClient.launchBillingFlow(activity, billingFlowParams)
                // For test & development environment, we grant pro and persist locally:
                preferencesRepository.setProUser(true)
                launch(Dispatchers.Main) {
                    onResult(true, null)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onResult(false, e.localizedMessage)
                }
            }
        }
    }

    fun restorePurchases(onResult: (Boolean, String) -> Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                // In production, query purchases from BillingClient.queryPurchasesAsync(...)
                // For demo/dev restore, check active entitlement:
                val isCurrentlyPro = isProUser.value
                if (!isCurrentlyPro) {
                    preferencesRepository.setProUser(true)
                }
                launch(Dispatchers.Main) {
                    onResult(true, "Purchases restored successfully!")
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onResult(false, e.localizedMessage ?: "Failed to restore purchases")
                }
            }
        }
    }
}
