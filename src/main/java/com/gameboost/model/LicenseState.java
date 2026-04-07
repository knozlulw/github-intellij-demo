package com.gameboost.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LicenseState {

    public static final int FREE_TIER_LIMIT = 15;

    public boolean isPro;
    public String licenseKey;
    public int optimizationsUsed;   // incremented each time an optimization runs (free tier)

    public LicenseState() {
        this.isPro = false;
        this.optimizationsUsed = 0;
    }

    public boolean canOptimize() {
        return isPro || optimizationsUsed < FREE_TIER_LIMIT;
    }

    public int remainingFree() {
        return Math.max(0, FREE_TIER_LIMIT - optimizationsUsed);
    }

    /**
     * Simple key validation — replace with real crypto later.
     * Format: GB-XXXX-XXXX-XXXX where X is alphanumeric.
     */
    public static boolean validateKey(String key) {
        if (key == null) return false;
        return key.toUpperCase().matches("GB-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}");
    }
}
