package vn.edu.tdtu.lhqc.budtrack.controllers.budget;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.tdtu.lhqc.budtrack.models.Budget;

/**
 * BudgetManager manages budget data with persistence.
 * Stores budget data in SharedPreferences.
 */
public final class BudgetManager {

    private static final String PREFS_NAME = "budget_prefs";
    private static final String KEY_BUDGET_IDS = "budget_ids";
    private static final String KEY_INITIALIZED = "initialized";
    private static final String PREFIX_ID = "budget_id_";
    private static final String PREFIX_NAME = "budget_name_";
    private static final String PREFIX_AMOUNT = "budget_amount_";
    private static final String PREFIX_COLOR = "budget_color_";
    private static final String PREFIX_CUSTOM_COLOR = "budget_custom_color_";
    private static final String PREFIX_IS_CUSTOM_COLOR = "budget_is_custom_color_";
    private static final String PREFIX_PERIOD = "budget_period_";

    private static List<Budget> budgets = null;

    private BudgetManager() {
    }

    /**
     * Initialize budgets from SharedPreferences or create empty list if not initialized.
     */
    public static void initialize(Context context) {
        if (budgets != null) {
            return; // Already initialized
        }

        SharedPreferences prefs = getPrefs(context);
        boolean initialized = prefs.getBoolean(KEY_INITIALIZED, false);

        if (initialized) {
            budgets = loadBudgets(context);
        } else {
            budgets = new ArrayList<>();
            saveBudgets(context);
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply();
        }
    }

    /**
     * Get all budgets. Initializes if not already done.
     */
    public static List<Budget> getBudgets(Context context) {
        if (budgets == null) initialize(context);
        return new ArrayList<>(budgets);
    }

    /**
     * Get a budget by ID.
     */
    public static Budget getBudgetById(Context context, long id) {
        List<Budget> allBudgets = getBudgets(context);
        for (Budget budget : allBudgets) {
            if (budget.getId() == id) {
                return budget;
            }
        }
        return null;
    }

    /**
     * Add a new budget.
     */
    public static void addBudget(Context context, Budget budget) {
        if (budgets == null) {
            initialize(context);
        }

        // Assign ID if not set
        if (budget.getId() == 0) {
            long maxId = 0;
            for (Budget b : budgets) {
                if (b.getId() > maxId) {
                    maxId = b.getId();
                }
            }
            budget.setId(maxId + 1);
        }

        budgets.add(budget);
        saveBudgets(context);
    }

    /**
     * Update a budget.
     */
    public static void updateBudget(Context context, Budget budget) {
        if (budgets == null) {
            initialize(context);
        }

        for (int i = 0; i < budgets.size(); i++) {
            if (budgets.get(i).getId() == budget.getId()) {
                budgets.set(i, budget);
                saveBudgets(context);
                return;
            }
        }
    }

    /**
     * Remove a budget by ID.
     */
    public static void removeBudget(Context context, long budgetId) {
        if (budgets == null) {
            initialize(context);
        }

        budgets.removeIf(budget -> budget.getId() == budgetId);
        saveBudgets(context);
    }

    /**
     * Save budgets to SharedPreferences.
     */
    private static void saveBudgets(Context context) {
        if (budgets == null) {
            return;
        }

        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> budgetIds = new HashSet<>();
        for (Budget budget : budgets) {
            String idStr = String.valueOf(budget.getId());
            budgetIds.add(idStr);

            editor.putLong(PREFIX_ID + idStr, budget.getId());
            editor.putString(PREFIX_NAME + idStr, budget.getName());
            editor.putLong(PREFIX_AMOUNT + idStr, budget.getBudgetAmount());
            editor.putInt(PREFIX_COLOR + idStr, budget.getColorResId());
            editor.putString(PREFIX_PERIOD + idStr, budget.getPeriod());
            
            // Handle custom color
            if (budget.getCustomColor() != null) {
                editor.putInt(PREFIX_CUSTOM_COLOR + idStr, budget.getCustomColor());
                editor.putBoolean(PREFIX_IS_CUSTOM_COLOR + idStr, true);
            } else {
                editor.putBoolean(PREFIX_IS_CUSTOM_COLOR + idStr, false);
            }
        }

        // Remove old budgets that no longer exist
        Set<String> existingIds = prefs.getStringSet(KEY_BUDGET_IDS, new HashSet<>());
        for (String oldId : existingIds) {
            if (!budgetIds.contains(oldId)) {
                editor.remove(PREFIX_ID + oldId);
                editor.remove(PREFIX_NAME + oldId);
                editor.remove(PREFIX_AMOUNT + oldId);
                editor.remove(PREFIX_COLOR + oldId);
                editor.remove(PREFIX_CUSTOM_COLOR + oldId);
                editor.remove(PREFIX_IS_CUSTOM_COLOR + oldId);
                editor.remove(PREFIX_PERIOD + oldId);
            }
        }

        editor.putStringSet(KEY_BUDGET_IDS, budgetIds);
        editor.apply();
    }

    /**
     * Load budgets from SharedPreferences.
     */
    private static List<Budget> loadBudgets(Context context) {
        SharedPreferences prefs = getPrefs(context);
        Set<String> budgetIds = prefs.getStringSet(KEY_BUDGET_IDS, new HashSet<>());
        List<Budget> loadedBudgets = new ArrayList<>();

        for (String idStr : budgetIds) {
            Budget budget = new Budget();
            budget.setId(prefs.getLong(PREFIX_ID + idStr, 0));
            budget.setName(prefs.getString(PREFIX_NAME + idStr, ""));
            budget.setBudgetAmount(prefs.getLong(PREFIX_AMOUNT + idStr, 0));
            budget.setColorResId(prefs.getInt(PREFIX_COLOR + idStr, 0));
            budget.setPeriod(prefs.getString(PREFIX_PERIOD + idStr, "monthly"));
            
            // Load custom color
            boolean isCustomColor = prefs.getBoolean(PREFIX_IS_CUSTOM_COLOR + idStr, false);
            if (isCustomColor) {
                budget.setCustomColor(prefs.getInt(PREFIX_CUSTOM_COLOR + idStr, 0));
            }
            
            loadedBudgets.add(budget);
        }

        return loadedBudgets;
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

