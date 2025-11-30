package vn.edu.tdtu.lhqc.budtrack.controllers.budget;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.models.Budget;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;

/**
 * BudgetCalculator calculates spent amounts for budgets based on transactions.
 */
public final class BudgetCalculator {

    private BudgetCalculator() {
    }

    /**
     * Calculate spent amount for a budget based on its period and categories.
     */
    public static long calculateSpentAmount(Context context, Budget budget) {
        // Get category IDs for this budget
        List<Long> categoryIds = BudgetCategoryManager.getCategoryIdsForBudget(context, budget.getId());
        
        if (categoryIds.isEmpty()) {
            return 0; // No categories, no spending
        }

        // Get date range based on period
        Date[] dateRange = getDateRangeForPeriod(budget.getPeriod());
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        // Get transactions in date range
        List<Transaction> transactions = TransactionManager.getTransactionsInRange(context, startDate, endDate);

        // Calculate total spent for matching categories
        long totalSpent = 0;
        for (Transaction transaction : transactions) {
            // Only count expenses
            if (transaction.getType() != TransactionType.EXPENSE) {
                continue;
            }

            // Check if transaction category matches budget categories
            if (transaction.getCategoryId() != null && categoryIds.contains(transaction.getCategoryId())) {
                totalSpent += transaction.getAmount();
            }
        }

        return totalSpent;
    }

    /**
     * Get date range for a budget period.
     * Returns [startDate, endDate] array.
     */
    private static Date[] getDateRangeForPeriod(String period) {
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        switch (period) {
            case "daily":
                // Today
                break;
            case "weekly":
                // This week (start of week)
                int dayOfWeek = startCalendar.get(Calendar.DAY_OF_WEEK);
                int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
                startCalendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
                break;
            case "monthly":
                // This month
                startCalendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "yearly":
                // This year
                startCalendar.set(Calendar.DAY_OF_YEAR, 1);
                break;
            default:
                // Default to monthly
                startCalendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
        }

        Date startDate = startCalendar.getTime();
        return new Date[]{startDate, endDate};
    }
}

