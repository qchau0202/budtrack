package vn.edu.tdtu.lhqc.budtrack.database.category;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.controllers.category.CategoryManager;

public class CategoryConverter {
    public static CategoryEntity toEntity(CategoryManager.CategoryItem item) {
        if (item == null) {
            return null;
        }
        CategoryEntity entity = new CategoryEntity();
        entity.name = item.name;
        entity.iconResId = item.iconResId;
        return entity;
    }

    public static CategoryManager.CategoryItem toCategoryItem(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CategoryManager.CategoryItem(entity.name, entity.iconResId);
    }

    public static List<CategoryManager.CategoryItem> toCategoryItemList(List<CategoryEntity> entities) {
        List<CategoryManager.CategoryItem> items = new ArrayList<>();
        if (entities != null) {
            for (CategoryEntity entity : entities) {
                items.add(toCategoryItem(entity));
            }
        }
        return items;
    }
}

