package dev.zwz.pipedreamadblocker;

import android.util.Log;

import io.github.libxposed.api.XposedModule;

public final class ModuleMain extends XposedModule {
    private static final String TAG = "PipeAdBlocker";

    @Override
    public void onPackageReady(PackageReadyParam param) {
        TargetSpec target = TargetRegistry.find(param.getPackageName());
        if (target == null) return;

        try {
            ClassLoader classLoader = param.getClassLoader();
            Log.i(TAG, "target ready: " + param.getPackageName() + " / " + classLoader);
            new HookEngine(this, classLoader, target).install();
        } catch (Throwable t) {
            Log.e(TAG, "module initialization failed", t);
        }
    }
}
