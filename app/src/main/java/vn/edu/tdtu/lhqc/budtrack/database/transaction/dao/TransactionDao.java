package vn.edu.tdtu.lhqc.budtrack.database.transaction.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.transaction.TransactionEntity;

@Dao
public interface TransactionDao {
    @Insert
    long insert(TransactionEntity transaction);

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    long insertOrReplace(TransactionEntity transaction);

    @Update
    void update(TransactionEntity transaction);

    @Delete
    void delete(TransactionEntity transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    List<TransactionEntity> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    TransactionEntity getTransactionById(long id);

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC, id DESC")
    List<TransactionEntity> getTransactionsByType(String type);

    @Query("SELECT * FROM transactions WHERE (:startDate IS NULL OR date >= :startDate) AND (:endDate IS NULL OR date <= :endDate) ORDER BY date DESC, id DESC")
    List<TransactionEntity> getTransactionsInRange(Long startDate, Long endDate);

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC, id DESC")
    List<TransactionEntity> getTransactionsByCategoryId(Long categoryId);

    @Query("SELECT * FROM transactions WHERE walletId = :walletId ORDER BY date DESC, id DESC")
    List<TransactionEntity> getTransactionsByWalletId(long walletId);

    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT MAX(id) FROM transactions")
    Long getMaxId();
}

