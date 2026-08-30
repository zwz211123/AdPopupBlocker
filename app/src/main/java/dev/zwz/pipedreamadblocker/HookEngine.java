package dev.zwz.pipedreamadblocker;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

final class HookEngine {
    private static final String TAG = "PipeAdBlocker";

    private final XposedModule module;
    private final ClassLoader initialClassLoader;
    private final TargetSpec target;
    private final Set<Method> hookedMethods = ConcurrentHashMap.newKeySet();
    private final Set<String> waitingClassNames = ConcurrentHashMap.newKeySet();
    private volatile boolean classLoaderWatcherInstalled;

    HookEngine(XposedModule module, ClassLoader initialClassLoader, TargetSpec target) {
        this.module = module;
        this.initialClassLoader = initialClassLoader;
        this.target = target;
        for (HookRule rule : target.rules) waitingClassNames.add(rule.className);
    }

    void install() {
        for (HookRule rule : target.rules) {
            tryInstallRule(rule, initialClassLoader);
        }
        if (!waitingClassNames.isEmpty()) installClassLoaderWatcher();
    }

    private void tryInstallRule(HookRule rule, ClassLoader classLoader) {
        try {
            Class<?> clazz = Class.forName(rule.className, false, classLoader);
            installRuleOnClass(rule, clazz, classLoader);
        } catch (ClassNotFoundException ignored) {
            // SecNeo-protected builds can expose the real DEX only after the app finishes unpacking.
        } catch (Throwable t) {
            Log.e(TAG, "install rule failed for " + rule.className, t);
        }
    }

    private synchronized void installRuleOnClass(HookRule rule, Class<?> clazz, ClassLoader classLoader) {
        int installed = 0;
        for (String methodName : rule.methodNames) {
            for (Method method : ReflectionUtils.declaredMethodsNamed(clazz, methodName)) {
                if (!hookedMethods.add(method)) continue;

                try {
                    module.hook(method)
                            .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                            .intercept(chain -> {
                                Object receiver = chain.getThisObject();
                                Object result = ReflectionUtils.defaultReturnValue(method);
                                Log.i(TAG, "blocked ad: " + clazz.getName() + "#" + method.getName());
                                if (rule.resumeGameAfterBlock) {
                                    GameRecovery.resumeBestEffort(clazz, receiver, classLoader);
                                }
                                return result;
                            });
                    installed++;
                } catch (Throwable t) {
                    hookedMethods.remove(method);
                    Log.e(TAG, "hook failed: " + clazz.getName() + "#" + methodName, t);
                }
            }
        }

        if (installed > 0) {
            waitingClassNames.remove(rule.className);
            Log.i(TAG, "installed " + installed + " ad hook(s) on " + clazz.getName());
        }
    }

    private synchronized void installClassLoaderWatcher() {
        if (classLoaderWatcherInstalled) return;
        classLoaderWatcherInstalled = true;

        try {
            Method loadClass = ClassLoader.class.getDeclaredMethod("loadClass", String.class, boolean.class);
            loadClass.setAccessible(true);
            module.hook(loadClass)
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Object result = chain.proceed();
                        if (!(result instanceof Class<?> loaded)) return result;

                        String name = loaded.getName();
                        if (!waitingClassNames.contains(name)) return result;

                        for (HookRule rule : target.rules) {
                            if (rule.className.equals(name)) {
                                installRuleOnClass(rule, loaded, loaded.getClassLoader());
                            }
                        }
                        return result;
                    });
            Log.i(TAG, "installed deferred class-loader watcher for protected DEX");
        } catch (Throwable t) {
            Log.e(TAG, "unable to install deferred class-loader watcher", t);
        }
    }
}
