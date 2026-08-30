package dev.zwz.pipedreamadblocker;

import java.util.Set;

final class HookRule {
    final String className;
    final Set<String> methodNames;
    final boolean resumeGameAfterBlock;

    private HookRule(String className, Set<String> methodNames, boolean resumeGameAfterBlock) {
        this.className = className;
        this.methodNames = Set.copyOf(methodNames);
        this.resumeGameAfterBlock = resumeGameAfterBlock;
    }

    static HookRule blockAndResume(String className, Set<String> methodNames) {
        return new HookRule(className, methodNames, true);
    }
}
