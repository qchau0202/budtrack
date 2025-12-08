package vn.edu.tdtu.lhqc.budtrack.database.budget;

import android.content.Context;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.AppDatabase;
import vn.edu.tdtu.lhqc.budtrack.database.budget.dao.BudgetDao;

public class BudgetRepository {
    private final BudgetDao budgetDao;

    public BudgetRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        budgetDao = db.budgetDao();
    }

    public long insertBudget(BudgetEntity budget) {
        return budgetDao.insert(budget);
    }

    public void updateBudget(BudgetEntity budget) {
        budgetDao.update(budget);
    }

    public void deleteBudgetById(long id) {
        budgetDao.deleteById(id);
    }

    public List<BudgetEntity> getAllBudgets() {
        return budgetDao.getAllBudgets();
    }

    public BudgetEntity getBudgetById(long id) {
        return budgetDao.getBudgetById(id);
    }
}

