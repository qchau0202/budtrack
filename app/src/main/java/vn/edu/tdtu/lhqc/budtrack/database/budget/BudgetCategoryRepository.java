package vn.edu.tdtu.lhqc.budtrack.database.budget;

import android.content.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.tdtu.lhqc.budtrack.database.AppDatabase;
import vn.edu.tdtu.lhqc.budtrack.database.budget.dao.BudgetCategoryDao;

public class BudgetCategoryRepository {
    private BudgetCategoryDao budgetCategoryDao;

    public BudgetCategoryRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        budgetCategoryDao = db.budgetCategoryDao();
    }

    public long insertRelationship(BudgetCategoryEntity relationship) {
        return budgetCategoryDao.insert(relationship);
    }

    public long insertOrReplaceRelationship(BudgetCategoryEntity relationship) {
        return budgetCategoryDao.insertOrReplace(relationship);
    }

    public void deleteRelationship(BudgetCategoryEntity relationship) {
        budgetCategoryDao.delete(relationship);
    }

    public void deleteRelationship(long budgetId, long categoryId) {
        budgetCategoryDao.deleteRelationship(budgetId, categoryId);
    }

    public void deleteAllForBudget(long budgetId) {
        budgetCategoryDao.deleteAllForBudget(budgetId);
    }

    public List<BudgetCategoryEntity> getAllRelationships() {
        return budgetCategoryDao.getAllRelationships();
    }

    public List<BudgetCategoryEntity> getRelationshipsByBudgetId(long budgetId) {
        return budgetCategoryDao.getRelationshipsByBudgetId(budgetId);
    }

    public List<BudgetCategoryEntity> getRelationshipsByCategoryId(long categoryId) {
        return budgetCategoryDao.getRelationshipsByCategoryId(categoryId);
    }

    public BudgetCategoryEntity getRelationship(long budgetId, long categoryId) {
        return budgetCategoryDao.getRelationship(budgetId, categoryId);
    }

    public Set<Long> getCategoryIdsUsedByOtherBudgets(long excludeBudgetId) {
        List<Long> categoryIds = budgetCategoryDao.getCategoryIdsUsedByOtherBudgets(excludeBudgetId);
        return new HashSet<>(categoryIds);
    }

}

