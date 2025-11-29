package vn.edu.tdtu.lhqc.budtrack.mockdata;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.models.Budget;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;

/**
 * Helper class for budget-related calculations using mock data.
 * Calculates spent amounts from actual transactions.
 */
public class MockBudgetHelper {

    /**
     * Calculates the total spent amount for a budget based on transactions.
     * Only counts expenses that match the budget's categories.
     * 
     * @param budget The budget to calculate spent amount for
     * @param transactions List of all transactions
     * @return Total spent amount in VND
     */
    public static long calculateSpentAmount(Budget budget, List<Transaction> transactions) {
        long totalSpent = 0;
        
        // Get category IDs linked to this budget
        List<Long> categoryIds = MockBudgetCategoryData.getCategoryIdsForBudget(budget.getId());
        
        // Sum up expenses that match budget categories
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.EXPENSE 
                    && transaction.getCategoryId() != null
                    && categoryIds.contains(transaction.getCategoryId())) {
                totalSpent += transaction.getAmount();
            }
        }
        
        return totalSpent;
    }

    /**
     * Gets a budget's spent amount calculated from mock transactions.
     * This uses pre-calculated values based on the mock transaction data.
     * 
     * Daily Budget (ID: 1) -> Transport category (ID: 3)
     *   Transport expenses: 50,000 + 100,000 + 30,000 + 200,000 = 380,000 VND
     * 
     * Personal Budget (ID: 2) -> Food (ID: 1) + Shopping (ID: 2)
     *   Food expenses: 75,000 + 150,000 + 120,000 + 80,000 = 425,000 VND
     *   Shopping expenses: 500,000 + 300,000 = 800,000 VND
     *   Total: 1,225,000 VND
     * 
     * Others Budget (ID: 3) -> Home category (ID: 4)
     *   No Home expenses in mock data: 0 VND
     * 
     * @param budget The budget
     * @return Spent amount in VND (calculated from mock transactions)
     */
    public static long getMockSpentAmount(Budget budget) {
        // Return calculated spent amounts based on mock transaction data
        switch (budget.getName()) {
            case "Daily":
                // Daily Budget: Transport category expenses
                // 50,000 + 100,000 + 30,000 + 200,000 = 380,000 VND
                return 380000L;
                
            case "Personal":
                // Personal Budget: Food + Shopping category expenses
                // Food: 75,000 + 150,000 + 120,000 + 80,000 = 425,000 VND
                // Shopping: 500,000 + 300,000 = 800,000 VND
                // Total: 1,225,000 VND
                return 1225000L;
                
            case "Others":
                // Others Budget: Home category expenses
                // No Home expenses in mock data
                return 0L;
                
            default:
                return 0L;
        }
    }

    /**
     * Calculates spent amount for all budgets from actual transactions.
     * This is the recommended method - calculates from actual transaction data.
     * 
     * @param budget The budget
     * @param transactions List of all transactions
     * @return Calculated spent amount in VND
     */
    public static long getSpentAmountFromTransactions(Budget budget, List<Transaction> transactions) {
        return calculateSpentAmount(budget, transactions);
    }
}
