package com.example.google_ads_integration

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.google_ads_integration.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bannerAdView: AdView

    private lateinit var consentInformation: ConsentInformation
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private lateinit var adRequest: AdRequest
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null


    private var mInterstitialAd: InterstitialAd? = null
    val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        MobileAds.initialize(this) { initializationStatus ->
            loadRewardedInterstitialAd()
        }

        // Interstitial Ads
        binding.buttonShow.setOnClickListener {

            // Load Ad
            loadInterstitialAd()

            // Show Ad
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this)
            } else {
                Log.d("TAG", "Ad wasn't ready yet.")
            }
            val intent = Intent(this, ShowAdActivity::class.java)
            startActivity(intent)
        }

        // Rewarded Ads
        loadRewardedAd()
        binding.buttonShowRewarded.setOnClickListener {
            rewardedAd?.let { ad ->
                ad.show(this, OnUserEarnedRewardListener { rewardItem ->
                    // Handle the reward.
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                    Toast.makeText(this, "User earned the reward", Toast.LENGTH_SHORT).show()

                })

                rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Toast.makeText(applicationContext, "Ad was clicked.", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        startActivity(Intent(applicationContext, ShowAdActivity::class.java))
                        loadRewardedAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        // Called when ad fails to show.
                        rewardedAd = null
                        startActivity(Intent(applicationContext, ShowAdActivity::class.java))
                    }

                    override fun onAdImpression() {
                        // Called when an impression is recorded for an ad.
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                    }
                }

            } ?: run {
                Toast.makeText(this, "The rewarded ad wasn't ready", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ShowAdActivity::class.java)
                startActivity(intent)
            }
        }

        // Rewarded Interstitial Ads
        loadRewardedInterstitialAd()
        binding.buttonShowRewardedInterstitial.setOnClickListener {
            rewardedInterstitialAd?.let { ad ->
                ad.show(this, OnUserEarnedRewardListener { rewardItem ->
                    // Handle the reward.
                    Toast.makeText(this, "User earned the reward", Toast.LENGTH_SHORT).show()

                })
            } ?: run {
                Toast.makeText(this, "The rewarded ad wasn't ready", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ShowAdActivity::class.java)
                startActivity(intent)
            }

        }

        // Banner Ads

        // 1.
        /*bannerAdView = binding.adView
        val bannerRequest = AdRequest.Builder().build()
        bannerAdView.loadAd(bannerRequest)*/

        // 2.
        requestConsentInfoUpdate()


    }

    fun loadRewardedAd() {
        var adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            "ca-app-pub-3940256099942544/5224354917",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
            })
    }

    private fun loadRewardedInterstitialAd() {
        RewardedInterstitialAd.load(this, "ca-app-pub-3940256099942544/5354046379",
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    rewardedInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Toast.makeText(
                                    applicationContext,
                                    "Ad was clicked.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                rewardedInterstitialAd = null
                                startActivity(
                                    Intent(
                                        applicationContext,
                                        ShowAdActivity::class.java
                                    )
                                )
                                loadInterstitialAd()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                // Called when ad fails to show.
                                rewardedInterstitialAd = null
                                startActivity(
                                    Intent(
                                        applicationContext,
                                        ShowAdActivity::class.java
                                    )
                                )
                            }

                            override fun onAdImpression() {
                                // Called when an impression is recorded for an ad.
                            }

                            override fun onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                            }
                        }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedInterstitialAd = null
                }
            })
    }

    fun loadInterstitialAd() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
                Toast.makeText(applicationContext, "Ad not loaded!", Toast.LENGTH_SHORT).show()
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })
    }

    private fun requestConsentInfoUpdate() {

        val debugSettings = ConsentDebugSettings.Builder(this)
            .addTestDeviceHashedId("33BE2250B43518CCDA7DE426D04EE231")
            .build()

        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

        // Create a ConsentRequestParameters object.
        // val params = ConsentRequestParameters.Builder().build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this@MainActivity, params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this@MainActivity
                ) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        // Consent gathering failed.
                        // Log.w(TAG, "${loadAndShowError.errorCode}: ${loadAndShowError.message}")
                        Snackbar.make(
                            binding.adView, loadAndShowError.message, Snackbar.LENGTH_SHORT
                        ).apply {
                            setAction("Reload") {
                                requestConsentInfoUpdate()
                            }.setActionTextColor(getColor(R.color.black))
                            show()
                        }

                    } else {
                        isMobileAdsInitializeCalled.getAndSet(true)
                        loadAds()
                    }

                    /*// Consent has been gathered.
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAdsSdk()
                    }*/
                }
            },
            { requestConsentError ->
                // Consent gathering failed.
                //  Log.w(TAG, "${requestConsentError.errorCode}: ${requestConsentError.message}")
                Toast.makeText(this, requestConsentError.message, Toast.LENGTH_SHORT).show()
            })
    }

    private fun loadAds() {
        if (consentInformation.canRequestAds() && isMobileAdsInitializeCalled.getAndSet(true)) {
            MobileAds.initialize(this) {
                callBack()
            }

            val request = AdRequest.Builder().build()
            binding.adView.loadAd(request)
        }
    }

    private fun callBack() {
        binding.adView.adListener = object : AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
                Toast.makeText(applicationContext, adError.message, Toast.LENGTH_SHORT).show()

            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Toast.makeText(applicationContext, "Ad loaded!", Toast.LENGTH_SHORT).show()
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }
    }

}