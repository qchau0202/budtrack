package vn.edu.tdtu.lhqc.budtrack.mockdata;

import vn.edu.tdtu.lhqc.budtrack.models.Budget;

/**
 * Wrapper class for Budget with calculated spent amount.
 * Used for displaying budget information in UI.
 */
public class BudgetDisplayData {
    private Budget budget;
    private long spentAmount; // Calculated from transactions

    public BudgetDisplayData(Budget budget, long spentAmount) {
        this.budget = budget;
        this.spentAmount = spentAmount;
    }

    public Budget getBudget() {
        return budget;
    }

    public long getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(long spentAmount) {
        this.spentAmount = spentAmount;
    }

    // Helper methods for UI display
    public long getRemaining() {
        return budget.getBudgetAmount() - spentAmount;
    }

    public int getPercentage() {
        if (budget.getBudgetAmount() == 0) return 0;
        return (int) Math.round(((double) spentAmount / budget.getBudgetAmount()) * 100);
    }

    public boolean isOverspending() {
        return spentAmount > budget.getBudgetAmount();
    }

    // Delegate methods to Budget
    public String getName() {
        return budget.getName();
    }

    public long getBudgetAmount() {
        return budget.getBudgetAmount();
    }

    public int getColorResId() {
        return budget.getColorResId();
    }
}

