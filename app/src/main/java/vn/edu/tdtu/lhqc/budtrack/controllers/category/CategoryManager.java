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

    public static void addCategory(Context context, String name, int iconResId) {
        if (name == null || name.isEmpty() || iconResId == 0) {
            return;
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


