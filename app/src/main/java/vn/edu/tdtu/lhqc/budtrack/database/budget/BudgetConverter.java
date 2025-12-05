package vn.edu.tdtu.lhqc.budtrack.database.budget;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.models.Budget;

public class BudgetConverter {
    public static BudgetEntity toEntity(Budget budget) {
        if (budget == null) {
            return null;
        }
        BudgetEntity entity = new BudgetEntity();
        entity.id = budget.getId();
        entity.name = budget.getName() != null ? budget.getName() : "";
        entity.budgetAmount = budget.getBudgetAmount();
        entity.colorResId = budget.getColorResId();
        entity.customColor = budget.getCustomColor();
        entity.period = budget.getPeriod() != null ? budget.getPeriod() : "monthly";
        return entity;
    }

    public static Budget toModel(BudgetEntity entity) {
        if (entity == null) {
            return null;
        }
        Budget budget = new Budget();
        budget.setId(entity.id);
        budget.setName(entity.name);
        budget.setBudgetAmount(entity.budgetAmount);
        budget.setColorResId(entity.colorResId);
        budget.setCustomColor(entity.customColor);
        budget.setPeriod(entity.period);
        return budget;
    }

    public static List<Budget> toModelList(List<BudgetEntity> entities) {
        List<Budget> budgets = new ArrayList<>();
        if (entities != null) {
            for (BudgetEntity entity : entities) {
                budgets.add(toModel(entity));
            }
        }
        return budgets;
    }
}

