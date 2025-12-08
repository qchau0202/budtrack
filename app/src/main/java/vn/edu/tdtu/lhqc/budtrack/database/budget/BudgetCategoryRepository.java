package vn.edu.tdtu.lhqc.budtrack.database.budget;

import android.content.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.tdtu.lhqc.budtrack.database.AppDatabase;
import vn.edu.tdtu.lhqc.budtrack.database.budget.dao.BudgetCategoryDao;

public class BudgetCategoryRepository {
    private final BudgetCategoryDao budgetCategoryDao;

    public BudgetCategoryRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        budgetCategoryDao = db.budgetCategoryDao();
    }

    public void insertRelationship(BudgetCategoryEntity relationship) {
        budgetCategoryDao.insert(relationship);
    }

    public void deleteAllForBudget(long budgetId) {
        budgetCategoryDao.deleteAllForBudget(budgetId);
    }

    public List<BudgetCategoryEntity> getRelationshipsByBudgetId(long budgetId) {
        return budgetCategoryDao.getRelationshipsByBudgetId(budgetId);
    }

    public Set<Long> getCategoryIdsUsedByOtherBudgets(long excludeBudgetId) {
        List<Long> categoryIds = budgetCategoryDao.getCategoryIdsUsedByOtherBudgets(excludeBudgetId);
        return new HashSet<>(categoryIds);
    }

}

