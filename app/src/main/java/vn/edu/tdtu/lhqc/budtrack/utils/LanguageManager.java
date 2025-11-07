package vn.edu.tdtu.lhqc.budtrack.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * Central place to persist and apply the app language.
 */
public final class LanguageManager {

    public enum Language {
        ENGLISH("en"),
        VIETNAMESE("vi");

        private final String localeCode;

        Language(String code) {
            this.localeCode = code;
        }

        public String getLocaleCode() {
            return localeCode;
        }

        public static Language fromCode(String code) {
            if (code == null) {
                return ENGLISH;
            }
            for (Language value : values()) {
                if (value.localeCode.equals(code)) {
                    return value;
                }
            }
            return ENGLISH;
        }
    }

    private static final String PREFS_NAME = "language_prefs";
    private static final String KEY_LOCALE = "locale_code";

    private LanguageManager() {
    }

    public static void applySavedLanguage(Context context) {
        Language language = getCurrentLanguage(context);
        updateResources(context, language);
    }

    public static void setLanguage(Context context, Language language) {
        if (language == null) {
            return;
        }
        getPrefs(context).edit().putString(KEY_LOCALE, language.getLocaleCode()).apply();
        updateResources(context, language);
    }

    public static Language getCurrentLanguage(Context context) {
        String stored = getPrefs(context).getString(KEY_LOCALE, Language.ENGLISH.getLocaleCode());
        return Language.fromCode(stored);
    }

    public static Context wrapContext(Context context) {
        Language language = getCurrentLanguage(context);
        return updateResources(context, language);
    }

    private static Context updateResources(Context context, Language language) {
        Locale locale = new Locale(language.getLocaleCode());
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        Context updatedContext = context.createConfigurationContext(config);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        return updatedContext;
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

