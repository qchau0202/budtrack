package vn.edu.tdtu.lhqc.budtrack.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import vn.edu.tdtu.lhqc.budtrack.database.budget.BudgetCategoryEntity;
import vn.edu.tdtu.lhqc.budtrack.database.budget.BudgetEntity;
import vn.edu.tdtu.lhqc.budtrack.database.budget.dao.BudgetCategoryDao;
import vn.edu.tdtu.lhqc.budtrack.database.budget.dao.BudgetDao;
import vn.edu.tdtu.lhqc.budtrack.database.category.CategoryEntity;
import vn.edu.tdtu.lhqc.budtrack.database.category.dao.CategoryDao;
import vn.edu.tdtu.lhqc.budtrack.database.transaction.TransactionEntity;
import vn.edu.tdtu.lhqc.budtrack.database.transaction.dao.TransactionDao;
import vn.edu.tdtu.lhqc.budtrack.database.wallet.WalletEntity;
import vn.edu.tdtu.lhqc.budtrack.database.wallet.dao.WalletDao;

@Database(entities = {WalletEntity.class, TransactionEntity.class, CategoryEntity.class, BudgetEntity.class, BudgetCategoryEntity.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract WalletDao walletDao();

    public abstract TransactionDao transactionDao();

    public abstract CategoryDao categoryDao();

    public abstract BudgetDao budgetDao();

    public abstract BudgetCategoryDao budgetCategoryDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "app_db"
                            )
                            .allowMainThreadQueries() // For simplicity; consider moving off main thread later
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

