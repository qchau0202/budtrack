package vn.edu.tdtu.lhqc.budtrack.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Centralized theme manager to control light/dark mode across the app.
 * Stores user preference and applies it using AppCompatDelegate.
 */
public final class ThemeManager {

    public enum ThemeMode {
        FOLLOW_SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES);

        public final int appCompatValue;

        ThemeMode(int value) {
            this.appCompatValue = value;
        }
    }

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_MODE = "mode"; // stored as AppCompatDelegate constant

    private ThemeManager() { }

    public static void applySavedTheme(Context context) {
        int saved = getPrefs(context).getInt(KEY_MODE, ThemeMode.FOLLOW_SYSTEM.appCompatValue);
        AppCompatDelegate.setDefaultNightMode(saved);
    }

    public static void setTheme(Context context, ThemeMode mode) {
        getPrefs(context).edit().putInt(KEY_MODE, mode.appCompatValue).apply();
        AppCompatDelegate.setDefaultNightMode(mode.appCompatValue);
    }

    public static boolean isDarkEnabled(Context context) {
        int saved = getPrefs(context).getInt(KEY_MODE, ThemeMode.FOLLOW_SYSTEM.appCompatValue);
        return saved == ThemeMode.DARK.appCompatValue;
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}