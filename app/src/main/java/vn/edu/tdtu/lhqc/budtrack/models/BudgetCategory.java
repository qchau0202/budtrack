package vn.edu.tdtu.lhqc.budtrack.models;

/**
 * BudgetCategory model - represents the many-to-many relationship between Budgets and Categories.
 * Users can add multiple categories to a budget (e.g., Daily budget can include Gas, Parking, etc.).
 * Simple junction table model.
 */
public class BudgetCategory {
    private long id;
    private long budgetId; // Reference to Budget
    private long categoryId; // Reference to Category

    // Default constructor for database
    public BudgetCategory() {
    }

    // Constructor for creating new budget-category relationships
    public BudgetCategory(long budgetId, long categoryId) {
        this.budgetId = budgetId;
        this.categoryId = categoryId;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(long budgetId) {
        this.budgetId = budgetId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }
}

