package com.example.core.ads

import android.content.Context

object AdManager {
    // Google Mobile Ads Sample / Test IDs for development
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var isAdmobInitialized = false

    fun initialize(context: Context) {
        // MobileAds.initialize(context)
        isAdmobInitialized = true
    }

    fun shouldShowAds(isProUser: Boolean): Boolean {
        return !isProUser
    }
}
