package vn.edu.tdtu.lhqc.budtrack.database.budget;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.models.BudgetCategory;

public class BudgetCategoryConverter {
    public static BudgetCategoryEntity toEntity(BudgetCategory relationship) {
        if (relationship == null) {
            return null;
        }
        BudgetCategoryEntity entity = new BudgetCategoryEntity();
        entity.id = relationship.getId();
        entity.budgetId = relationship.getBudgetId();
        entity.categoryId = relationship.getCategoryId();
        return entity;
    }

    public static BudgetCategory toModel(BudgetCategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        BudgetCategory relationship = new BudgetCategory();
        relationship.setId(entity.id);
        relationship.setBudgetId(entity.budgetId);
        relationship.setCategoryId(entity.categoryId);
        return relationship;
    }

    public static List<BudgetCategory> toModelList(List<BudgetCategoryEntity> entities) {
        List<BudgetCategory> relationships = new ArrayList<>();
        if (entities != null) {
            for (BudgetCategoryEntity entity : entities) {
                relationships.add(toModel(entity));
            }
        }
        return relationships;
    }
}

