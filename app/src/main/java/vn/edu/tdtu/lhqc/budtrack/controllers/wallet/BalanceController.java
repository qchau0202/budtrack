package vn.edu.tdtu.lhqc.budtrack.controllers.wallet;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * BalanceService encapsulates balance-related UI logic and state.
 * - Persists hidden/visible preference
 * - Formats balance for display (masked vs visible)
 */
public final class BalanceController {

    private static final String PREFS_NAME = "balance_prefs";
    private static final String KEY_HIDDEN = "hidden";

    private BalanceController() { }

    public static boolean isHidden(Context context) {
        return getPrefs(context).getBoolean(KEY_HIDDEN, false);
    }

    public static void setHidden(Context context, boolean hidden) {
        getPrefs(context).edit().putBoolean(KEY_HIDDEN, hidden).apply();
    }

    public static boolean toggleHidden(Context context) {
        boolean newValue = !isHidden(context);
        setHidden(context, newValue);
        return newValue;
    }

    // Format a balance string. If hidden, mask numeric characters and common separators with '*', preserving spaces and currency letters.
    
    public static String formatDisplay(String rawText, boolean hidden) {
        if (rawText == null) return "";
        if (!hidden) return rawText;

        StringBuilder sb = new StringBuilder(rawText.length());
        for (int i = 0; i < rawText.length(); i++) {
            char c = rawText.charAt(i);
            if (Character.isDigit(c) || c == '.' || c == ',' || c == '-') {
                sb.append('*');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}


