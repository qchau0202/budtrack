package vn.edu.tdtu.lhqc.budtrack.database.budget;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class BudgetEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    public long budgetAmount; // Budget limit in VND

    public int colorResId; // Color resource ID (0 if custom color is used)

    public Integer customColor; // Custom color as ARGB integer (nullable)

    @NonNull
    public String period; // "daily", "weekly", "monthly", "yearly"

    public BudgetEntity() {
        this.period = "monthly"; // Default
    }
}

