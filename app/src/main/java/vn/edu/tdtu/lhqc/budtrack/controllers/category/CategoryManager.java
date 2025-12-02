package vn.edu.tdtu.lhqc.budtrack.controllers.category;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple manager for user-defined categories used in transaction creation.
 * Stores data in SharedPreferences (name + iconResId only).
 */
public final class CategoryManager {

    private static final String PREFS_NAME = "category_prefs";
    private static final String KEY_CATEGORIES = "categories";

    private CategoryManager() {
    }

    public static class CategoryItem {
        public final String name;
        public final int iconResId;

        public CategoryItem(String name, int iconResId) {
            this.name = name;
            this.iconResId = iconResId;
        }
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static List<CategoryItem> getCategories(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String json = prefs.getString(KEY_CATEGORIES, "[]");
        List<CategoryItem> items = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.optString("name", null);
                int iconResId = obj.optInt("iconResId", 0);
                if (name != null && !name.isEmpty() && iconResId != 0) {
                    items.add(new CategoryItem(name, iconResId));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Check if a category with the same name AND icon already exists.
     * @param context Application context
     * @param name Category name
     * @param iconResId Category icon resource ID
     * @return true if a category with both matching name and icon exists, false otherwise
     */
    public static boolean categoryExists(Context context, String name, int iconResId) {
        if (name == null || name.isEmpty() || iconResId == 0) {
            return false;
        }
        
        List<CategoryItem> categories = getCategories(context);
        for (CategoryItem item : categories) {
            // Match by BOTH name AND icon - both must match for it to be considered a duplicate
            if (item.name.equals(name) && item.iconResId == iconResId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a category with the same name AND icon already exists, excluding a specific category.
     * Used for edit mode to allow keeping the same category but detect if editing to match another.
     * @param context Application context
     * @param name Category name
     * @param iconResId Category icon resource ID
     * @param excludeName Category name to exclude from check (the category being edited)
     * @param excludeIconResId Category icon to exclude from check (the category being edited)
     * @return true if another category with both matching name and icon exists, false otherwise
     */
    public static boolean categoryExistsExcluding(Context context, String name, int iconResId, 
                                                  String excludeName, int excludeIconResId) {
        if (name == null || name.isEmpty() || iconResId == 0) {
            return false;
        }
        
        List<CategoryItem> categories = getCategories(context);
        for (CategoryItem item : categories) {
            // Skip the category being edited
            if (item.name.equals(excludeName) && item.iconResId == excludeIconResId) {
                continue;
            }
            // Match by BOTH name AND icon
            if (item.name.equals(name) && item.iconResId == iconResId) {
                return true;
            }
        }
        return false;
    }

    public static void addCategory(Context context, String name, int iconResId) {
        if (name == null || name.isEmpty() || iconResId == 0) {
            return;
        }

        // Check for duplicate (both name AND icon must match)
        if (categoryExists(context, name, iconResId)) {
            throw new IllegalArgumentException("Category with this name and icon already exists");
        }

        List<CategoryItem> current = getCategories(context);
        current.add(new CategoryItem(name, iconResId));

        JSONArray array = new JSONArray();
        for (CategoryItem item : current) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", item.name);
                obj.put("iconResId", item.iconResId);
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        getPrefs(context).edit().putString(KEY_CATEGORIES, array.toString()).apply();
    }

    public static void removeCategory(Context context, String name, int iconResId) {
        List<CategoryItem> current = getCategories(context);
        List<CategoryItem> updated = new ArrayList<>();
        for (CategoryItem item : current) {
            // Match by both name and icon to reduce accidental removal
            if (!(item.name.equals(name) && item.iconResId == iconResId)) {
                updated.add(item);
            }
        }
        saveCategories(context, updated);
    }

    public static void updateCategory(Context context,
                                      String oldName,
                                      int oldIconResId,
                                      String newName,
                                      int newIconResId) {
        if (newName == null || newName.isEmpty() || newIconResId == 0) return;

        List<CategoryItem> current = getCategories(context);
        for (int i = 0; i < current.size(); i++) {
            CategoryItem item = current.get(i);
            if (item.name.equals(oldName) && item.iconResId == oldIconResId) {
                current.set(i, new CategoryItem(newName, newIconResId));
                break;
            }
        }
        saveCategories(context, current);
    }

    private static void saveCategories(Context context, List<CategoryItem> items) {
        JSONArray array = new JSONArray();
        for (CategoryItem item : items) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", item.name);
                obj.put("iconResId", item.iconResId);
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        getPrefs(context).edit().putString(KEY_CATEGORIES, array.toString()).apply();
    }
}


