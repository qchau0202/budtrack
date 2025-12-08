package vn.edu.tdtu.lhqc.budtrack.database.budget.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.budget.BudgetCategoryEntity;

@Dao
public interface BudgetCategoryDao {
    @Insert
    long insert(BudgetCategoryEntity relationship);

    @Query("SELECT * FROM budget_categories")
    List<BudgetCategoryEntity> getAllRelationships();

    @Query("SELECT * FROM budget_categories WHERE budgetId = :budgetId")
    List<BudgetCategoryEntity> getRelationshipsByBudgetId(long budgetId);

    @Query("SELECT * FROM budget_categories WHERE budgetId = :budgetId AND categoryId = :categoryId LIMIT 1")
    BudgetCategoryEntity getRelationship(long budgetId, long categoryId);

    @Query("DELETE FROM budget_categories WHERE budgetId = :budgetId AND categoryId = :categoryId")
    void deleteRelationship(long budgetId, long categoryId);

    @Query("DELETE FROM budget_categories WHERE budgetId = :budgetId")
    void deleteAllForBudget(long budgetId);

    @Query("SELECT DISTINCT categoryId FROM budget_categories WHERE budgetId != :excludeBudgetId")
    List<Long> getCategoryIdsUsedByOtherBudgets(long excludeBudgetId);
}

