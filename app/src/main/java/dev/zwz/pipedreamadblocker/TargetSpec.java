package dev.zwz.pipedreamadblocker;

import java.util.List;
import java.util.Set;

final class TargetSpec {
    private static final HookRule COMMON_FORCED_AD_RULE = HookRule.blockAndResume(
            "com.adv.core.AdsManager",
            Set.of(
                    "showGameTimeInterstitial",
                    "showEventInterstitial",
                    "showInterstitalView",
                    "TimeShowHomeInterstitial",
                    "fixTimerInterTask",
                    "showHotSplash",
                    "openHotSplash",
                    "showSplash"
            )
    );

    final String packageName;
    final List<HookRule> rules;

    TargetSpec(String packageName, List<HookRule> rules) {
        this.packageName = packageName;
        this.rules = List.copyOf(rules);
    }

    static TargetSpec pipeDreamFactory() {
        return new TargetSpec(
                "com.bzdjl.lyxn.tg",
                List.of(COMMON_FORCED_AD_RULE)
        );
    }

    static TargetSpec wonderfulCardWorld() {
        return new TargetSpec(
                "com.cyjl.qmkpsj.tg",
                List.of(COMMON_FORCED_AD_RULE)
        );
    }
}
