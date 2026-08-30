package dev.zwz.pipedreamadblocker;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class GameRecovery {
    private static final String TAG = "PipeAdBlocker";

    private GameRecovery() {}

    static void resumeBestEffort(Class<?> managerClass, Object managerInstance, ClassLoader classLoader) {
        // Primary path: the target's own AdsManager exposes the matching resume bridge.
        try {
            ReflectionUtils.invokeBestEffortNoArg(managerClass, managerInstance, "StarActResume");
        } catch (Throwable t) {
            Log.d(TAG, "StarActResume failed", t);
        }

        // Fallback: find an Activity held by AdsManager, then its UnityPlayer instance and resume it.
        // This only runs after a blocked forced-ad call, never on normal app lifecycle callbacks.
        try {
            Object activityObj = ReflectionUtils.findFieldAssignableTo(managerInstance, Activity.class);
            if (!(activityObj instanceof Activity activity)) return;

            Class<?> unityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer", false, classLoader);
            Object unityPlayer = findUnityPlayer(activity, unityPlayerClass);
            if (unityPlayer == null) return;

            Method resume = unityPlayerClass.getDeclaredMethod("resume");
            resume.setAccessible(true);
            resume.invoke(unityPlayer);
        } catch (Throwable ignored) {
            // Not every Unity build exposes the same field/method shape. Failing closed is intentional.
        }
    }

    private static Object findUnityPlayer(Activity activity, Class<?> unityPlayerClass) {
        for (Class<?> c = activity.getClass(); c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(activity);
                    if (value != null && unityPlayerClass.isAssignableFrom(value.getClass())) return value;
                } catch (Throwable ignored) {}
            }
        }
        return null;
    }
}
