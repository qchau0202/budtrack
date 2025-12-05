package vn.edu.tdtu.lhqc.budtrack.controllers.category;

import android.content.Context;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.category.CategoryConverter;
import vn.edu.tdtu.lhqc.budtrack.database.category.CategoryEntity;
import vn.edu.tdtu.lhqc.budtrack.database.category.CategoryRepository;

/**
 * Simple manager for user-defined categories used in transaction creation.
 * Uses Room database for storage.
 */
public final class CategoryManager {

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

    public static List<CategoryItem> getCategories(Context context) {
        CategoryRepository repository = new CategoryRepository(context);
        List<CategoryEntity> entities = repository.getAllCategories();
        return CategoryConverter.toCategoryItemList(entities);
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
        
        CategoryRepository repository = new CategoryRepository(context);
        CategoryEntity entity = repository.getCategoryByNameAndIcon(name, iconResId);
        return entity != null;
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
        
        CategoryRepository repository = new CategoryRepository(context);
        CategoryEntity entity = repository.getCategoryByNameAndIconExcluding(name, iconResId, excludeName, excludeIconResId);
        return entity != null;
    }

    public static void addCategory(Context context, String name, int iconResId) {
        if (name == null || name.isEmpty() || iconResId == 0) {
            return;
        }

        // Check for duplicate (both name AND icon must match)
        if (categoryExists(context, name, iconResId)) {
            throw new IllegalArgumentException("Category with this name and icon already exists");
        }

        CategoryRepository repository = new CategoryRepository(context);
        CategoryEntity entity = new CategoryEntity(name, iconResId);
        repository.insertCategory(entity);
    }

    public static void removeCategory(Context context, String name, int iconResId) {
        CategoryRepository repository = new CategoryRepository(context);
        repository.deleteCategoryByNameAndIcon(name, iconResId);
    }

    public static void updateCategory(Context context,
                                      String oldName,
                                      int oldIconResId,
                                      String newName,
                                      int newIconResId) {
        if (newName == null || newName.isEmpty() || newIconResId == 0) return;

        CategoryRepository repository = new CategoryRepository(context);
        CategoryEntity entity = repository.getCategoryByNameAndIcon(oldName, oldIconResId);
        if (entity != null) {
            entity.name = newName;
            entity.iconResId = newIconResId;
            repository.updateCategory(entity);
        }
    }
}


