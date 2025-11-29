package vn.edu.tdtu.lhqc.budtrack.mockdata;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.models.Budget;

/**
 * Mock data generator for Budget objects.
 * Provides sample budget data for testing and development.
 */
public class MockBudgetData {

    /**
     * Generates a list of sample budgets.
     * 
     * @return List of sample Budget objects
     */
    public static List<Budget> getSampleBudgets() {
        List<Budget> budgets = new ArrayList<>();

        // Daily budget: 10,000,000 VND
        Budget daily = new Budget("Daily", 10000000L, R.color.primary_green, "monthly");
        daily.setId(1);
        budgets.add(daily);

        // Personal budget: 5,000,000 VND
        Budget personal = new Budget("Personal", 5000000L, R.color.primary_yellow, "monthly");
        personal.setId(2);
        budgets.add(personal);

        // Others budget: 1,000,000 VND
        Budget others = new Budget("Others", 1000000L, R.color.primary_red, "monthly");
        others.setId(3);
        budgets.add(others);

        return budgets;
    }

    /**
     * Gets a budget by name.
     * 
     * @param name Budget name
     * @return Budget object or null if not found
     */
    public static Budget getBudgetByName(String name) {
        for (Budget budget : getSampleBudgets()) {
            if (budget.getName().equals(name)) {
                return budget;
            }
        }
        return null;
    }
}

