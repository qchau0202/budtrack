package vn.edu.tdtu.lhqc.budtrack.database.budget.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.budget.BudgetEntity;

@Dao
public interface BudgetDao {
    @Insert
    long insert(BudgetEntity budget);

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    long insertOrReplace(BudgetEntity budget);

    @Update
    void update(BudgetEntity budget);

    @Delete
    void delete(BudgetEntity budget);

    @Query("SELECT * FROM budgets ORDER BY id ASC")
    List<BudgetEntity> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    BudgetEntity getBudgetById(long id);

    @Query("DELETE FROM budgets WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT MAX(id) FROM budgets")
    Long getMaxId();
}

