package dev.zwz.pipedreamadblocker;

import java.util.List;
import java.util.Set;

final class TargetSpec {
    final String packageName;
    final List<HookRule> rules;

    TargetSpec(String packageName, List<HookRule> rules) {
        this.packageName = packageName;
        this.rules = List.copyOf(rules);
    }

    static TargetSpec pipeDreamFactory() {
        return new TargetSpec(
                "com.bzdjl.lyxn.tg",
                List.of(
                        HookRule.blockAndResume(
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
                        )
                )
        );
    }
}
