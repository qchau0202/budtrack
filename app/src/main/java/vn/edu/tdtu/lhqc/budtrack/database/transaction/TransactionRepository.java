package vn.edu.tdtu.lhqc.budtrack.database.transaction;

import android.content.Context;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.AppDatabase;
import vn.edu.tdtu.lhqc.budtrack.database.transaction.dao.TransactionDao;

public class TransactionRepository {
    private TransactionDao transactionDao;

    public TransactionRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        transactionDao = db.transactionDao();
    }

    public long insertTransaction(TransactionEntity transaction) {
        return transactionDao.insert(transaction);
    }

    public long insertOrReplaceTransaction(TransactionEntity transaction) {
        return transactionDao.insertOrReplace(transaction);
    }

    public void updateTransaction(TransactionEntity transaction) {
        transactionDao.update(transaction);
    }

    public void deleteTransaction(TransactionEntity transaction) {
        transactionDao.delete(transaction);
    }

    public void deleteTransactionById(long id) {
        transactionDao.deleteById(id);
    }

    public List<TransactionEntity> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    public TransactionEntity getTransactionById(long id) {
        return transactionDao.getTransactionById(id);
    }

    public List<TransactionEntity> getTransactionsByType(String type) {
        return transactionDao.getTransactionsByType(type);
    }

    public List<TransactionEntity> getTransactionsInRange(Long startDate, Long endDate) {
        return transactionDao.getTransactionsInRange(startDate, endDate);
    }

    public List<TransactionEntity> getTransactionsByCategoryId(Long categoryId) {
        return transactionDao.getTransactionsByCategoryId(categoryId);
    }

    public List<TransactionEntity> getTransactionsByWalletId(long walletId) {
        return transactionDao.getTransactionsByWalletId(walletId);
    }

    public Long getMaxId() {
        Long maxId = transactionDao.getMaxId();
        return maxId != null ? maxId : 0L;
    }
}

