package vn.edu.tdtu.lhqc.budtrack.mockdata;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.models.BudgetCategory;

/**
 * Mock data generator for BudgetCategory objects.
 * Creates relationships between budgets and categories.
 */
public class MockBudgetCategoryData {

    /**
     * Generates sample budget-category relationships.
     * 
     * Daily Budget -> Gas, Parking Fee categories
     * Personal Budget -> Food, Shopping categories
     * Others Budget -> Various categories
     * 
     * @return List of sample BudgetCategory objects
     */
    public static List<BudgetCategory> getSampleBudgetCategories() {
        List<BudgetCategory> budgetCategories = new ArrayList<>();

        // Daily Budget (ID: 1) includes Transport category (ID: 3)
        BudgetCategory dailyTransport = new BudgetCategory(1L, 3L);
        dailyTransport.setId(1);
        budgetCategories.add(dailyTransport);

        // Personal Budget (ID: 2) includes Food (ID: 1) and Shopping (ID: 2)
        BudgetCategory personalFood = new BudgetCategory(2L, 1L);
        personalFood.setId(2);
        budgetCategories.add(personalFood);

        BudgetCategory personalShopping = new BudgetCategory(2L, 2L);
        personalShopping.setId(3);
        budgetCategories.add(personalShopping);

        // Others Budget (ID: 3) includes Home category (ID: 4)
        BudgetCategory othersHome = new BudgetCategory(3L, 4L);
        othersHome.setId(4);
        budgetCategories.add(othersHome);

        return budgetCategories;
    }

    /**
     * Gets all category IDs linked to a specific budget.
     * 
     * @param budgetId The budget ID
     * @return List of category IDs
     */
    public static List<Long> getCategoryIdsForBudget(long budgetId) {
        List<Long> categoryIds = new ArrayList<>();
        for (BudgetCategory bc : getSampleBudgetCategories()) {
            if (bc.getBudgetId() == budgetId) {
                categoryIds.add(bc.getCategoryId());
            }
        }
        return categoryIds;
    }
}

