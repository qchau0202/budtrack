package vn.edu.tdtu.lhqc.budtrack.utils;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import vn.edu.tdtu.lhqc.budtrack.R;

/**
 * Utility class for styling MaterialButton tabs.
 * Provides methods to apply consistent tab styling across the app.
 */
public final class TabStyleUtils {

    private TabStyleUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Applies selected tab styling (green background, white text).
     * 
     * @param context The context to get color resources
     * @param button The MaterialButton to style
     */
    public static void applySelectedStyle(Context context, MaterialButton button) {
        if (button == null || context == null) {
            return;
        }
        button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_green));
        button.setTextColor(ContextCompat.getColor(context, R.color.primary_white));
    }

    /**
     * Applies unselected tab styling (grey background, black text).
     * 
     * @param context The context to get color resources
     * @param button The MaterialButton to style
     */
    public static void applyUnselectedStyle(Context context, MaterialButton button) {
        if (button == null || context == null) {
            return;
        }
        button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_grey));
        button.setTextColor(ContextCompat.getColor(context, R.color.primary_black));
    }

    /**
     * Applies tab style based on selection state.
     * 
     * @param context The context to get color resources
     * @param button The MaterialButton to style
     * @param selected Whether the tab is selected
     */
    public static void applyStyle(Context context, MaterialButton button, boolean selected) {
        if (selected) {
            applySelectedStyle(context, button);
        } else {
            applyUnselectedStyle(context, button);
        }
    }

    /**
     * Selects one tab and unselects another (for two-tab scenarios like Income/Expense).
     * 
     * @param context The context to get color resources
     * @param selectedButton The button to mark as selected
     * @param unselectedButton The button to mark as unselected
     */
    public static void selectTab(Context context, MaterialButton selectedButton, MaterialButton unselectedButton) {
        applySelectedStyle(context, selectedButton);
        applyUnselectedStyle(context, unselectedButton);
    }
}

