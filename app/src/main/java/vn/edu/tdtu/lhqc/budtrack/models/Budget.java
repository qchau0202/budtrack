package vn.edu.tdtu.lhqc.budtrack.models;

/**
 * Budget model - represents a budget (e.g., Daily, Personal, Others).
 * Budgets track spending limits and are linked to categories.
 */
public class Budget {
    private long id;
    private String name; // e.g., "Daily", "Personal", "Others"
    private long budgetAmount; // Budget limit in VND
    private int colorResId; // Color resource ID for UI display (0 if custom color is used)
    private Integer customColor; // Custom color as ARGB integer (null if using colorResId)
    private String period; // Optional: "daily", "weekly", "monthly", "yearly"

    // Default constructor for database
    public Budget() {
    }

    // Constructor for creating new budgets
    public Budget(String name, long budgetAmount, int colorResId) {
        this.name = name;
        this.budgetAmount = budgetAmount;
        this.colorResId = colorResId;
        this.period = "monthly"; // Default period
    }

    // Constructor with period
    public Budget(String name, long budgetAmount, int colorResId, String period) {
        this.name = name;
        this.budgetAmount = budgetAmount;
        this.colorResId = colorResId;
        this.period = period;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(long budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public int getColorResId() {
        return colorResId;
    }

    public void setColorResId(int colorResId) {
        this.colorResId = colorResId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Integer getCustomColor() {
        return customColor;
    }

    public void setCustomColor(Integer customColor) {
        this.customColor = customColor;
    }
    
    /**
     * Get the actual color value (either from resource or custom color).
     * Returns the custom color if set, otherwise returns 0 (caller should use colorResId).
     */
    public int getActualColorValue() {
        return customColor != null ? customColor : 0;
    }
}

