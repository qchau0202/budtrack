package vn.edu.tdtu.lhqc.budtrack.controllers.transaction;

import android.content.Context;

import java.util.Date;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.transaction.TransactionConverter;
import vn.edu.tdtu.lhqc.budtrack.database.transaction.TransactionEntity;
import vn.edu.tdtu.lhqc.budtrack.database.transaction.TransactionRepository;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;

/**
 * TransactionManager manages transaction data with persistence.
 * Uses Room database for storage.
 */
public final class TransactionManager {

    private TransactionManager() {
    }

    /**
     * Get all transactions.
     */
    public static List<Transaction> getTransactions(Context context) {
        TransactionRepository repository = new TransactionRepository(context);
        List<TransactionEntity> entities = repository.getAllTransactions();
        return TransactionConverter.toModelList(entities);
    }

    /**
     * Get transactions filtered by type.
     */
    public static List<Transaction> getTransactionsByType(Context context, TransactionType type) {
        TransactionRepository repository = new TransactionRepository(context);
        List<TransactionEntity> entities = repository.getTransactionsByType(type.name());
        return TransactionConverter.toModelList(entities);
    }

    /**
     * Get transactions within a date range.
     */
    public static List<Transaction> getTransactionsInRange(Context context, Date startDate, Date endDate) {
        TransactionRepository repository = new TransactionRepository(context);
        Long startTimestamp = startDate != null ? startDate.getTime() : null;
        Long endTimestamp = endDate != null ? endDate.getTime() : null;
        List<TransactionEntity> entities = repository.getTransactionsInRange(startTimestamp, endTimestamp);
        return TransactionConverter.toModelList(entities);
    }

    /**
     * Get transactions by category ID.
     */
    public static List<Transaction> getTransactionsByCategory(Context context, long categoryId) {
        TransactionRepository repository = new TransactionRepository(context);
        List<TransactionEntity> entities = repository.getTransactionsByCategoryId(categoryId);
        return TransactionConverter.toModelList(entities);
    }

    /**
     * Get a transaction by ID.
     */
    public static Transaction getTransactionById(Context context, long transactionId) {
        TransactionRepository repository = new TransactionRepository(context);
        TransactionEntity entity = repository.getTransactionById(transactionId);
        return TransactionConverter.toModel(entity);
    }

    /**
     * Add a new transaction.
     */
    public static void addTransaction(Context context, Transaction transaction) {
        TransactionRepository repository = new TransactionRepository(context);
        
        // Assign ID if not set
        if (transaction.getId() == 0) {
            long nextId = repository.getMaxId() + 1;
            transaction.setId(nextId);
        }
        
        TransactionEntity entity = TransactionConverter.toEntity(transaction);
        repository.insertTransaction(entity);
    }

    /**
     * Update a transaction.
     */
    public static void updateTransaction(Context context, Transaction transaction) {
        TransactionRepository repository = new TransactionRepository(context);
        TransactionEntity entity = TransactionConverter.toEntity(transaction);
        repository.updateTransaction(entity);
    }

    /**
     * Remove a transaction by ID.
     */
    public static void removeTransaction(Context context, long transactionId) {
        TransactionRepository repository = new TransactionRepository(context);
        repository.deleteTransactionById(transactionId);
    }

    /**
     * Get transactions by wallet ID.
     */
    public static List<Transaction> getTransactionsByWalletId(Context context, long walletId) {
        TransactionRepository repository = new TransactionRepository(context);
        List<TransactionEntity> entities = repository.getTransactionsByWalletId(walletId);
        return TransactionConverter.toModelList(entities);
    }
}

