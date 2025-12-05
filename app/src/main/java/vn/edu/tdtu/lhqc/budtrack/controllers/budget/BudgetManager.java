package vn.edu.tdtu.lhqc.budtrack.controllers.budget;

import android.content.Context;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.budget.BudgetConverter;
import vn.edu.tdtu.lhqc.budtrack.database.budget.BudgetEntity;
import vn.edu.tdtu.lhqc.budtrack.database.budget.BudgetRepository;
import vn.edu.tdtu.lhqc.budtrack.models.Budget;

/**
 * BudgetManager manages budget data with persistence.
 * Uses Room database for storage.
 */
public final class BudgetManager {

    private BudgetManager() {
    }

    /**
     * Get all budgets.
     */
    public static List<Budget> getBudgets(Context context) {
        BudgetRepository repository = new BudgetRepository(context);
        List<BudgetEntity> entities = repository.getAllBudgets();
        return BudgetConverter.toModelList(entities);
    }

    /**
     * Get a budget by ID.
     */
    public static Budget getBudgetById(Context context, long id) {
        BudgetRepository repository = new BudgetRepository(context);
        BudgetEntity entity = repository.getBudgetById(id);
        return BudgetConverter.toModel(entity);
    }

    /**
     * Add a new budget.
     */
    public static void addBudget(Context context, Budget budget) {
        BudgetRepository repository = new BudgetRepository(context);
        
        // Assign ID if not set
        if (budget.getId() == 0) {
            long nextId = repository.getMaxId() + 1;
            budget.setId(nextId);
        }
        
        BudgetEntity entity = BudgetConverter.toEntity(budget);
        repository.insertBudget(entity);
    }

    /**
     * Update a budget.
     */
    public static void updateBudget(Context context, Budget budget) {
        BudgetRepository repository = new BudgetRepository(context);
        BudgetEntity entity = BudgetConverter.toEntity(budget);
        repository.updateBudget(entity);
    }

    /**
     * Remove a budget by ID.
     */
    public static void removeBudget(Context context, long budgetId) {
        BudgetRepository repository = new BudgetRepository(context);
        repository.deleteBudgetById(budgetId);
    }
}

