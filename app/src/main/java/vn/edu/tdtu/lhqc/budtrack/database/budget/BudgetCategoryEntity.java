package vn.edu.tdtu.lhqc.budtrack.database.budget;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "budget_categories", indices = {@Index(value = {"budgetId", "categoryId"}, unique = true)})
public class BudgetCategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long budgetId;

    public long categoryId;

    public BudgetCategoryEntity() {
    }

    public BudgetCategoryEntity(long budgetId, long categoryId) {
        this.budgetId = budgetId;
        this.categoryId = categoryId;
    }
}

