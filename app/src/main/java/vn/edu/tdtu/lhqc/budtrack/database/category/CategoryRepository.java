package vn.edu.tdtu.lhqc.budtrack.database.category;

import android.content.Context;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.AppDatabase;
import vn.edu.tdtu.lhqc.budtrack.database.category.dao.CategoryDao;

public class CategoryRepository {
    private CategoryDao categoryDao;

    public CategoryRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        categoryDao = db.categoryDao();
    }

    public long insertCategory(CategoryEntity category) {
        return categoryDao.insert(category);
    }

    public long insertOrReplaceCategory(CategoryEntity category) {
        return categoryDao.insertOrReplace(category);
    }

    public void updateCategory(CategoryEntity category) {
        categoryDao.update(category);
    }

    public void deleteCategory(CategoryEntity category) {
        categoryDao.delete(category);
    }

    public void deleteCategoryByNameAndIcon(String name, int iconResId) {
        categoryDao.deleteByNameAndIcon(name, iconResId);
    }

    public void deleteCategoryById(long id) {
        categoryDao.deleteById(id);
    }

    public List<CategoryEntity> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public CategoryEntity getCategoryById(long id) {
        return categoryDao.getCategoryById(id);
    }

    public CategoryEntity getCategoryByNameAndIcon(String name, int iconResId) {
        return categoryDao.getCategoryByNameAndIcon(name, iconResId);
    }

    public CategoryEntity getCategoryByNameAndIconExcluding(String name, int iconResId, String excludeName, int excludeIconResId) {
        return categoryDao.getCategoryByNameAndIconExcluding(name, iconResId, excludeName, excludeIconResId);
    }
}

