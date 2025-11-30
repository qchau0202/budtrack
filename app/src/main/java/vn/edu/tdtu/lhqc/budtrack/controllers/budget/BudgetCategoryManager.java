package vn.edu.tdtu.lhqc.budtrack.controllers.budget;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.tdtu.lhqc.budtrack.models.BudgetCategory;

/**
 * BudgetCategoryManager manages budget-category relationships with persistence.
 * Stores relationships in SharedPreferences using JSON serialization.
 */
public final class BudgetCategoryManager {

    private static final String PREFS_NAME = "budget_category_prefs";
    private static final String KEY_RELATIONSHIPS = "relationships";
    private static final String KEY_NEXT_ID = "next_id";

    private BudgetCategoryManager() {
    }

    /**
     * Get all budget-category relationships.
     */
    public static List<BudgetCategory> getRelationships(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String relationshipsJson = prefs.getString(KEY_RELATIONSHIPS, "[]");
        
        List<BudgetCategory> relationships = new ArrayList<>();
        
        try {
            JSONArray jsonArray = new JSONArray(relationshipsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                BudgetCategory relationship = new BudgetCategory();
                relationship.setId(jsonObject.getLong("id"));
                relationship.setBudgetId(jsonObject.getLong("budgetId"));
                relationship.setCategoryId(jsonObject.getLong("categoryId"));
                relationships.add(relationship);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return relationships;
    }

    /**
     * Get category IDs for a budget.
     */
    public static List<Long> getCategoryIdsForBudget(Context context, long budgetId) {
        List<BudgetCategory> relationships = getRelationships(context);
        List<Long> categoryIds = new ArrayList<>();
        
        for (BudgetCategory relationship : relationships) {
            if (relationship.getBudgetId() == budgetId) {
                categoryIds.add(relationship.getCategoryId());
            }
        }
        
        return categoryIds;
    }

    /**
     * Get budget IDs for a category.
     */
    public static List<Long> getBudgetIdsForCategory(Context context, long categoryId) {
        List<BudgetCategory> relationships = getRelationships(context);
        List<Long> budgetIds = new ArrayList<>();
        
        for (BudgetCategory relationship : relationships) {
            if (relationship.getCategoryId() == categoryId) {
                budgetIds.add(relationship.getBudgetId());
            }
        }
        
        return budgetIds;
    }

    /**
     * Add a budget-category relationship.
     */
    public static void addRelationship(Context context, long budgetId, long categoryId) {
        List<BudgetCategory> relationships = getRelationships(context);
        
        // Check if relationship already exists
        for (BudgetCategory relationship : relationships) {
            if (relationship.getBudgetId() == budgetId && relationship.getCategoryId() == categoryId) {
                return; // Already exists
            }
        }
        
        // Create new relationship
        BudgetCategory relationship = new BudgetCategory(budgetId, categoryId);
        
        // Assign ID
        SharedPreferences prefs = getPrefs(context);
        long nextId = prefs.getLong(KEY_NEXT_ID, 1);
        relationship.setId(nextId);
        prefs.edit().putLong(KEY_NEXT_ID, nextId + 1).apply();
        
        relationships.add(relationship);
        saveRelationships(context, relationships);
    }

    /**
     * Remove a budget-category relationship.
     */
    public static void removeRelationship(Context context, long budgetId, long categoryId) {
        List<BudgetCategory> relationships = getRelationships(context);
        relationships.removeIf(r -> r.getBudgetId() == budgetId && r.getCategoryId() == categoryId);
        saveRelationships(context, relationships);
    }

    /**
     * Set categories for a budget (replaces all existing relationships for this budget).
     */
    public static void setCategoriesForBudget(Context context, long budgetId, List<Long> categoryIds) {
        List<BudgetCategory> relationships = getRelationships(context);
        
        // Remove existing relationships for this budget
        relationships.removeIf(r -> r.getBudgetId() == budgetId);
        
        // Add new relationships
        SharedPreferences prefs = getPrefs(context);
        long nextId = prefs.getLong(KEY_NEXT_ID, 1);
        
        for (Long categoryId : categoryIds) {
            BudgetCategory relationship = new BudgetCategory(budgetId, categoryId);
            relationship.setId(nextId++);
            relationships.add(relationship);
        }
        
        prefs.edit().putLong(KEY_NEXT_ID, nextId).apply();
        saveRelationships(context, relationships);
    }

    /**
     * Remove all relationships for a budget (when budget is deleted).
     */
    public static void removeAllForBudget(Context context, long budgetId) {
        List<BudgetCategory> relationships = getRelationships(context);
        relationships.removeIf(r -> r.getBudgetId() == budgetId);
        saveRelationships(context, relationships);
    }

    /**
     * Save relationships to SharedPreferences.
     */
    private static void saveRelationships(Context context, List<BudgetCategory> relationships) {
        SharedPreferences prefs = getPrefs(context);
        JSONArray jsonArray = new JSONArray();
        
        for (BudgetCategory relationship : relationships) {
            try {
                JSONObject json = new JSONObject();
                json.put("id", relationship.getId());
                json.put("budgetId", relationship.getBudgetId());
                json.put("categoryId", relationship.getCategoryId());
                jsonArray.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        prefs.edit().putString(KEY_RELATIONSHIPS, jsonArray.toString()).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

