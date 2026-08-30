package dev.zwz.pipedreamadblocker;

import java.util.List;

final class TargetRegistry {
    private static final List<TargetSpec> TARGETS = List.of(
            TargetSpec.pipeDreamFactory()
    );

    private TargetRegistry() {}

    static TargetSpec find(String packageName) {
        for (TargetSpec spec : TARGETS) {
            if (spec.packageName.equals(packageName)) return spec;
        }
        return null;
    }
}
