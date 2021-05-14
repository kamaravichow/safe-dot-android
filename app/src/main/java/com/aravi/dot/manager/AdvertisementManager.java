package com.aravi.dot.manager;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.facebook.ads.NativeBannerAdView;

public class AdvertisementManager {
    private static AdvertisementManager instance;
    private Context context;
    private AnalyticsManager analyticsManager;
    private NativeBannerAd mNativeBannerAd;


    public static AdvertisementManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdvertisementManager(context);
        }
        return instance;
    }

    public AdvertisementManager(Context context) {
        this.context = context;
        this.analyticsManager = AnalyticsManager.getInstance(context);
        initAdProviders();
    }

    private void initAdProviders() {
        AudienceNetworkAds.initialize(context);
    }

    public void setBannerAd(LinearLayout adLayout) {
        mNativeBannerAd = new NativeBannerAd(context, "244358406678589_244358583345238");
        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                analyticsManager.setAdvertisementEvent(adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                analyticsManager.setAdvertisementEvent("LOADED");
                View adView = NativeBannerAdView.render(context, mNativeBannerAd, NativeBannerAdView.Type.HEIGHT_120);
                adLayout.addView(adView);
            }

            @Override
            public void onAdClicked(Ad ad) {
                analyticsManager.setAdvertisementEvent("CLICKED");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                analyticsManager.setAdvertisementEvent("IMPRESSION");
            }

            @Override
            public void onMediaDownloaded(Ad ad) {

            }
        };

        mNativeBannerAd.loadAd(mNativeBannerAd.buildLoadAdConfig()
                .withAdListener(nativeAdListener)
                .build());
    }


}
